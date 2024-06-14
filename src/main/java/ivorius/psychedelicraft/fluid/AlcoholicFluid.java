package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.alcohol.DrinkTypes;
import ivorius.psychedelicraft.fluid.alcohol.Maturity;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

/**
 * Created by lukas on 25.11.14.
 */
public class AlcoholicFluid extends DrugFluid implements Processable {
    public static final Attribute<Integer> DISTILLATION = Attribute.ofInt("distillation", 0, 16);
    public static final Attribute<Integer> MATURATION = Attribute.ofInt("maturation", 0, 16);

    private static final int FERMENTATION_STEPS = 2;
    public static final Attribute<Integer> FERMENTATION = Attribute.ofInt("fermentation", 0, FERMENTATION_STEPS);
    public static final Attribute<Boolean> VINEGAR = Attribute.ofBoolean("vinegar");

    final Settings settings;

    public AlcoholicFluid(Identifier id, Settings settings) {
        super(id, settings.drinkable().with(new FluidStateManager.FluidProperty<>(IntProperty.of("variant", 0, settings.states.get().size()), (properties, value) -> {
            settings.states.get().get(MathHelper.clamp(value, 0, settings.states.get().size())).apply(properties);
        }, stack -> {
            return settings.states.get().stream()
                    .filter(s -> s.entry().predicate().test(stack))
                    .findFirst()
                    .map(match -> settings.states.get().indexOf(match))
                    .orElse(0);
        })));
        this.settings = settings;
    }

    protected int getDistilledColor() {
        return settings.distilledColor;
    }

    protected int getMatureColor() {
        return settings.matureColor;
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemFluids stack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(stack, consumer);

        double alcohol =
                  settings.fermentationAlcohol * (FERMENTATION.get(stack) / (double) FERMENTATION_STEPS)
                + settings.distillationAlcohol * MathUtils.progress(DISTILLATION.get(stack))
                + settings.maturationAlcohol * MathUtils.progress(MATURATION.get(stack) * 0.2F);

        consumer.accept(new DrugInfluence(settings.drugType, 20, 0.003, 0.002, alcohol));
        settings.variants.find(stack).extraDrug().ifPresent(drug -> {
            consumer.accept(drug.clone());
        });
    }

    @Override
    public ProcessType modifyProcess(Resovoir tank, ProcessType type) {
        if (type == ProcessType.FERMENT && FERMENTATION.get(tank.getContents()) >= FERMENTATION_STEPS) {
            return ProcessType.ACETIFY;
        }
        return type;
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        return switch (type) {
            case DISTILL -> FERMENTATION.get(tank.getContents()) == 0 || MATURATION.get(tank.getContents()) != 0 ? UNCONVERTABLE : settings.tickInfo.get().ticksPerDistillation();
            case MATURE -> FERMENTATION.get(tank.getContents()) == 0 ? UNCONVERTABLE : settings.tickInfo.get().ticksPerMaturation();
            case FERMENT -> settings.tickInfo.get().ticksPerFermentation();
            case ACETIFY -> VINEGAR.get(tank.getContents()) ? settings.tickInfo.get().ticksUntilAcetification() : UNCONVERTABLE;
            default -> UNCONVERTABLE;
        };
    }

    @Override
    public void process(Resovoir tank, ProcessType type, ByProductConsumer output) {

        switch (type) {
            case DISTILL: {
                int amountDrained = MathHelper.floor(tank.getContents().amount() * MathUtils.progress(DISTILLATION.get(tank.getContents()), 0.5F));
                if (amountDrained > 0) {
                    tank.drain(1);
                    output.accept(PSFluids.SLURRY.getDefaultStack());
                }
                tank.setContents(DISTILLATION.cycle(tank.getContents()));
                break;
            }
            case MATURE:
                tank.setContents(MATURATION.cycle(tank.getContents()));
                break;
            case FERMENT:
                tank.setContents(FERMENTATION.cycle(tank.getContents()));
                break;
            case ACETIFY:
                tank.setContents(VINEGAR.cycle(tank.getContents()));
                break;
            default:
        }
    }

    @Override
    public void getProcessStages(ProcessType type, ProcessStageConsumer consumer) {
        if (type == ProcessType.DISTILL) {
            generateRecipeConversions(settings.tickInfo.get().ticksPerDistillation(), DISTILLATION,
                    DrinkTypes.State::distillation,
                    DrinkTypes.State::maturation,
                    DrinkTypes.State::fermentation, consumer);
        }

        if (type == ProcessType.MATURE) {
            generateRecipeConversions(settings.tickInfo.get().ticksPerMaturation(), MATURATION,
                    DrinkTypes.State::maturation,
                    DrinkTypes.State::distillation,
                    DrinkTypes.State::fermentation, consumer);
        }

        if (type == ProcessType.FERMENT) {
            generateRecipeConversions(settings.tickInfo.get().ticksPerFermentation(), FERMENTATION,
                    DrinkTypes.State::fermentation,
                    DrinkTypes.State::distillation,
                    DrinkTypes.State::maturation, consumer);
        }
    }

    private void generateRecipeConversions(int time, Attribute<Integer> attribute, Function<DrinkTypes.State, Integer> valueGetter,
            Function<DrinkTypes.State, Integer> fixA,
            Function<DrinkTypes.State, Integer> fixB,
            ProcessStageConsumer consumer) {
        List<DrinkTypes.State> states = settings.states.get();

        for (int i = 0; i < states.size(); i++) {
            var state = states.get(i);

            if (!state.vinegar()) {
                states.stream()
                        .filter(s -> {
                            return fixA.apply(s) == fixA.apply(state)
                                    && fixB.apply(s) == fixB.apply(state)
                                    && !s.vinegar();
                        })
                        .forEach(s -> {
                    int difference = valueGetter.apply(state) - valueGetter.apply(s);
                    if (difference > 0) {
                        consumer.accept(
                            time,
                            difference,
                            stack -> List.of(s.apply(stack)),
                            stack -> List.of(state.apply(stack))
                        );
                    }
                });
            } else if (attribute == FERMENTATION) {
                states.stream().filter(s -> !s.vinegar()).forEach(s -> {
                    consumer.accept(
                        time,
                        (FERMENTATION_STEPS + 1) - valueGetter.apply(s),
                        stack -> List.of(s.apply(stack)),
                        stack -> List.of(state.apply(stack))
                    );
                });
            }
        }
    }

    @Override
    public int getHash(ItemStack stack) {
        return Objects.hash(this, settings.variants.find(ItemFluids.of(stack)));
    }

    @Override
    public Text getName(ItemFluids stack) {
        return settings.variants.find(stack).getName(Text.translatable(getTranslationKey()));
    }

    @Override
    public void appendTooltip(ItemFluids stack, List<Text> tooltip, TooltipType type) {

        int distillation = DISTILLATION.get(stack);
        int maturation = MATURATION.get(stack);
        int fermentation = FERMENTATION.get(stack);

        if (distillation > 0) {
            tooltip.add(Text.translatable("psychedelicraft.alcohol.distillations", distillation).formatted(Formatting.GRAY));
        }

        if (fermentation > 0) {
            tooltip.add(Text.translatable("psychedelicraft.alcohol.fermentations", fermentation).formatted(Formatting.GRAY));
        }

        if (maturation > 0) {
            tooltip.add(Text.translatable("psychedelicraft.alcohol.maturations", maturation, Maturity.getMaturity(maturation).getName()).formatted(Formatting.GRAY));
        }

        //if (distillation > 0 || maturation > 0 || fermentation > 0) {
        //    tooltip.add(Text.empty());
        //    tooltip.add(settings.profile.getFlavour(distillation, fermentation, maturation));
        //}
    }

    @Override
    public void appendTankTooltip(ItemFluids stack, @Nullable World world, List<Text> tooltip, FluidProcessingBlockEntity tank) {
        int ticksProcessed = tank.getTimeProcessed();
        int ticksNeeded = tank.getTimeNeeded();
        String timeRemaining = StringHelper.formatTicks(ticksNeeded - ticksProcessed, world == null ? 20 : world.getTickManager().getTickRate());
        ProcessType processType = tank.getActiveProcess();
        tooltip.add(Text.translatable("fluid.status", processType.getStatus()));

        if (processType == ProcessType.FERMENT && FERMENTATION.get(stack) >= FERMENTATION_STEPS) {
            processType = ProcessType.ACETIFY;
        }
        if (processType != ProcessType.IDLE) {
            tooltip.add(Text.translatable(processType.getTimeLabelTranslationKey(), timeRemaining));
        }
        if (tank.getProcessType() == ProcessType.DISTILL) {
            if (FERMENTATION.get(stack) == 0) {
                tooltip.add(Text.translatable("* Must be fermented").formatted(Formatting.RED, Formatting.ITALIC));
            } else {
                tooltip.add(Text.translatable("* Is fermented").formatted(Formatting.GRAY));
            }
            if (MATURATION.get(stack) > 0) {
                tooltip.add(Text.translatable("* Must be unmatured").formatted(Formatting.RED, Formatting.ITALIC));
            } else {
                tooltip.add(Text.translatable("* Is unmatured").formatted(Formatting.GRAY));
            }
        } else if (tank.getProcessType() == ProcessType.MATURE) {
            if (FERMENTATION.get(stack) == 0) {
                tooltip.add(Text.translatable("* Must be fermented").formatted(Formatting.GRAY));
            } else {
                tooltip.add(Text.translatable("* Is fermented").formatted(Formatting.RED, Formatting.ITALIC));
            }
        }
    }

    @Override
    public int getColor(ItemFluids stack) {
        return MathUtils.mixColors(
                MathUtils.mixColors(
                        super.getColor(stack),
                        getDistilledColor(),
                        MathUtils.progress(DISTILLATION.get(stack))
                ),
                getMatureColor(),
                MathUtils.progress(MATURATION.get(stack) * 0.2F)
        );
    }

    @Override
    public Identifier getSymbol(ItemFluids stack) {
        return settings.variants.find(stack).getSymbol(getId());
    }

    @Override
    public void getDefaultStacks(ItemStack stack, Consumer<ItemStack> consumer) {
        int capacity = FluidCapacity.get(stack);
        if (capacity > 0) {
            ItemFluids fluids = getDefaultStack(capacity);
            settings.states.get().forEach(state -> {
                consumer.accept(ItemFluids.set(stack.copy(), state.apply(fluids)));
            });
        }
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.isIn(PSTags.Items.SUITABLE_ALCOHOLIC_DRINK_RECEPTICALS);
    }

    public static class Settings extends DrugFluid.Settings {
        protected DrinkTypes variants = DrinkTypes.empty();

        private double fermentationAlcohol;
        private double distillationAlcohol;
        private double maturationAlcohol;

        private int matureColor = 0xcc592518;
        private int distilledColor = 0x33ffffff;

        DrugType drugType = DrugType.ALCOHOL;

        final Supplier<List<DrinkTypes.State>> states = Suppliers.memoize(() -> variants.streamStates().toList());

        public Supplier<PSConfig.Balancing.FluidProperties.TickInfo> tickInfo;

        public Settings() {
            this.appearance = stack -> variants.find(stack).appearance();
        }

        public Settings drug(DrugType drug) {
            this.drugType = drug;
            return this;
        }

        public Settings matureColor(int matureColor) {
            this.matureColor = matureColor;
            return this;
        }

        public Settings distilledColor(int distilledColor) {
            this.distilledColor = distilledColor;
            return this;
        }

        public Settings variants(DrinkTypes variants) {
            this.variants = variants;
            return this;
        }

        public Settings alcohol(double fermentationAlcohol, double distillationAlcohol, double maturationAlcohol) {
            this.fermentationAlcohol = fermentationAlcohol;
            this.distillationAlcohol = distillationAlcohol;
            this.maturationAlcohol = maturationAlcohol;
            return this;
        }

        public Settings tickRate(Supplier<PSConfig.Balancing.FluidProperties.TickInfo> tickInfo) {
            this.tickInfo = tickInfo;
            return this;
        }
    }
}
