package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.alcohol.DrinkTypes;
import ivorius.psychedelicraft.fluid.alcohol.Maturity;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;
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
        super(id, settings.drinkable().with(new FluidStateManager.FluidProperty<>(IntProperty.of("variant", 0, settings.states.get().size()), (stack, variant) -> {
            return settings.states.get().get(MathHelper.clamp(variant, 0, settings.states.get().size())).apply(stack);
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
    public void getDrugInfluencesPerLiter(ItemStack fluidStack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(fluidStack, consumer);

        double alcohol =
                  settings.fermentationAlcohol * (FERMENTATION.get(fluidStack) / (double) FERMENTATION_STEPS)
                + settings.distillationAlcohol * MathUtils.progress(DISTILLATION.get(fluidStack))
                + settings.maturationAlcohol * MathUtils.progress(MATURATION.get(fluidStack) * 0.2F);

        consumer.accept(new DrugInfluence(settings.drugType, 20, 0.003, 0.002, alcohol));
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type, @Nullable Resovoir complement) {
        if (type == ProcessType.DISTILL) {
            if (FERMENTATION.get(tank.getContents()) < FERMENTATION_STEPS || MATURATION.get(tank.getContents()) != 0) {
                return UNCONVERTABLE;
            }

            return settings.tickInfo.get().ticksPerDistillation;
        }

        if (type == ProcessType.MATURE) {
            if (FERMENTATION.get(tank.getContents()) < FERMENTATION_STEPS) {
                return UNCONVERTABLE;
            }
            return settings.tickInfo.get().ticksPerMaturation;
        }

        if (type == ProcessType.FERMENT) {
            if (FERMENTATION.get(tank.getContents()) < FERMENTATION_STEPS) {
                return settings.tickInfo.get().ticksPerFermentation;
            }
            return settings.tickInfo.get().ticksUntilAcetification;
        }

        return UNCONVERTABLE;
    }

    @Override
    public ItemStack process(Resovoir tank, ProcessType type, @Nullable Resovoir complement) {
        MutableFluidContainer contents = tank.getContents();

        if (type == ProcessType.DISTILL) {
            int fermentation = FERMENTATION.get(contents);


            if (fermentation < FERMENTATION_STEPS) {
                return ItemStack.EMPTY;
            }

            int distillation = DISTILLATION.get(contents);

            DISTILLATION.set(contents, distillation + 1);

            contents.drain(MathHelper.floor(contents.getLevel() * MathUtils.progress(distillation, 0.5F)));
            return PSFluids.SLURRY.getDefaultStack(1);
        }

        if (type == ProcessType.MATURE) {
            MATURATION.set(contents, MATURATION.get(contents) + 1);
        }

        if (type == ProcessType.FERMENT) {
            int fermentation = FERMENTATION.get(contents);

            if (fermentation < FERMENTATION_STEPS) {
                FERMENTATION.set(contents, fermentation + 1);
            } else {
                VINEGAR.set(contents, true);
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public Text getName(ItemStack stack) {
        return settings.variants.find(stack).getName(Text.translatable(getTranslationKey()));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {

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
    public int getColor(ItemStack stack) {
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
    public Identifier getSymbol(ItemStack stack) {
        return settings.variants.find(stack).getSymbol(getId());
    }

    @Override
    public void getDefaultStacks(FluidContainer container, Consumer<ItemStack> consumer) {
        settings.states.get().forEach(state -> consumer.accept(state.apply(getDefaultStack(container))));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSuitableContainer(FluidContainer container) {
        return container.asItem().getRegistryEntry().isIn(PSTags.Items.SUITABLE_ALCOHOLIC_DRINK_RECEPTICALS);
    }

    public static class Settings extends DrugFluid.Settings {
        protected DrinkTypes variants = DrinkTypes.empty();

        private double fermentationAlcohol;
        private double distillationAlcohol;
        private double maturationAlcohol;

        private int matureColor = 0xcc592518;
        private int distilledColor = 0x33ffffff;

        DrugType drugType = DrugType.ALCOHOL;

        private final Supplier<List<DrinkTypes.State>> states = Suppliers.memoize(() -> variants.streamStates().toList());

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
