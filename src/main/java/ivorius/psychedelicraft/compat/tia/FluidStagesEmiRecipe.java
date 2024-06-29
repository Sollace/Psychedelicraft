package ivorius.psychedelicraft.compat.tia;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;

/**
 * To be replaced with a more user-friendly type of recipe
 */

class FluidStagesEmiRecipe implements PSRecipe {
    public static BiConsumer<RecipeCategory, PluginContext> generate() {
        return (category, context) -> context.addGenerator(client -> FluidStageUtil.combineSimilarProcesses(SimpleFluid.REGISTRY.stream()
                .filter(fluid -> fluid instanceof Processable)
                .flatMap(fluid -> ((Processable)fluid).getProcesses().filter(process -> !process.transitions().isEmpty())),
                    process -> (TlaRecipe)new FluidStagesEmiRecipe(category, process),
                    processes -> (TlaRecipe)new FluidStagesEmiRecipe(category, List.copyOf(processes))
            ).toList());
    }
    private final Identifier id;
    private final RecipeCategory category;

    private final List<TlaIngredient> catalysts;

    private final List<Stage> stages;

    private FluidStagesEmiRecipe(RecipeCategory category, Processable.Process process) {
        this.category = category;
        this.id = process.id();
        ItemFluids def = process.fluid().getDefaultStack();
        stages = process.transitions().stream().map(transition -> {
            return new Stage(transition,
                    RecipeUtil.toIngredient(transition.input().apply(def)),
                    RecipeUtil.toIngredient(transition.output().apply(def)),
                    Stage.getStackForProcess(transition.type())
            );
        }).toList();
        catalysts = List.of(TlaIngredient.ofItemTag(process.fluid().getPreferredContainerTag()));
    }

    private FluidStagesEmiRecipe(RecipeCategory category, List<Processable.Process> processes) {
        this.category = category;
        Processable.Process process = processes.get(0);
        this.id = process.id();
        stages = IntStream.range(0, process.transitions().size()).mapToObj(i -> {
            var transition = process.transitions().get(i);
            return new Stage(transition,
                    TlaIngredient.join(processes.stream().map(p -> RecipeUtil.toIngredient(p.transitions().get(i).input().apply(p.fluid().getDefaultStack()))).distinct().toList()),
                    TlaIngredient.join(processes.stream().map(p -> RecipeUtil.toIngredient(p.transitions().get(i).output().apply(p.fluid().getDefaultStack()))).distinct().toList()),
                    Stage.getStackForProcess(transition.type())
            );
        }).toList();
        catalysts = processes.stream().map(Processable.Process::fluid).distinct().map(SimpleFluid::getPreferredContainerTag).distinct().map(TlaIngredient::ofItemTag).toList();
    }

    record Stage(Processable.Transition transition, TlaIngredient input, TlaIngredient output, TlaIngredient container) {
        public void buildGui(int x, int y, GuiBuilder widgets, boolean last) {
            ClientWorld world = MinecraftClient.getInstance().world;
            widgets.addSlot(input, x, y).markInput();
            widgets.addSlot(container, x + 19, y).markCatalyst();

            int ticks = Math.max(1, transition.time() * transition.multiplier());

            widgets.addAnimatedArrow(x + 40, y, ticks < 20 ? 1 : ticks / 20).addTooltip(
                    Text.translatable("gui.psychedelicraft.recipe.fluid_process",
                            Text.translatable("fluid.status." + transition.type().asString()),
                            StringHelper.formatTicks(ticks, world == null ? 20 : world.getTickManager().getTickRate())
                    )
            );
            widgets.addText(Text.literal(transition.multiplier() + "x"), x + 45, y + 9, -1, true);

            if (last) {
                widgets.addSlot(output, x + 65, y).markOutput();
            }
        }

        private static TlaIngredient getStackForProcess(Processable.ProcessType type) {
            return switch (type) {
                case FERMENT, ACETIFY -> TlaStack.of(PSItems.MASH_TUB).asIngredient();
                case PURIFY, REACT -> TlaStack.of(PSItems.BUNSEN_BURNER).asIngredient();
                case MATURE -> TlaIngredient.ofItemTag(PSTags.Items.BARRELS);
                case DISTILL -> TlaStack.of(PSItems.DISTILLERY).asIngredient();
                case IDLE -> TlaStack.of(PSItems.BOTTLE).asIngredient();
            };
        }
    }

    @Override
    public RecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return List.of(stages.get(0).input());
    }

    @Override
    public List<TlaStack> getOutputs() {
        return List.copyOf(stages.get(stages.size() - 1).output().getStacks());
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public void buildGui(GuiBuilder widgets) {
        int totalWidth = stages.size() * 65 + 8;
        int left = (category.getDisplayWidth() - totalWidth) / 2;

        for (int i = 0; i < stages.size(); i++) {
            Stage stage = stages.get(i);
            stage.buildGui(left + i * 65, i, widgets, i == stages.size() - 1);
        }
    }
}
