package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.AlcoholicDrinkTypes.StatePredicate;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by lukas on 25.11.14.
 */
public class AlcoholicFluid extends DrugFluid implements Processable {
    public static final Attribute<Integer> DISTILLATION = Attribute.ofInt("distillation", 0, 80);
    public static final Attribute<Integer> MATURATION = Attribute.ofInt("maturation", 0, 80);
    public static final Attribute<Boolean> VINEGAR = Attribute.ofBoolean("vinegar");

    final Settings settings;

    public final Attribute<Integer> FERMENTATION;

    public AlcoholicFluid(Identifier id, Settings settings) {
        super(id, settings.drinkable());
        FERMENTATION = Attribute.ofInt("fermentation", 0, settings.fermentationSteps);
        settings.attr(DISTILLATION).attr(FERMENTATION).attr(MATURATION).attr(VINEGAR);
        this.settings = settings;
    }

    protected int getDistilledColor(ItemStack stack) {
        getColor(stack);
        return settings.distilledColor;
    }

    protected int getMatureColor(ItemStack stack) {
        return settings.matureColor;
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemStack fluidStack, List<DrugInfluence> list) {
        super.getDrugInfluencesPerLiter(fluidStack, list);

        double alcohol =
                  settings.fermentationAlcohol * (FERMENTATION.get(fluidStack) / (double) settings.fermentationSteps)
                + settings.distillationAlcohol * MathUtils.progress(DISTILLATION.get(fluidStack))
                + settings.maturationAlcohol * MathUtils.progress(MATURATION.get(fluidStack) * 0.2F);

        list.add(new DrugInfluence(DrugType.ALCOHOL, 20, 0.003, 0.002, alcohol));
    }

    @Override
    public int getProcessingTime(ItemStack stack, ProcessType type, boolean openContainer) {
        if (type == ProcessType.DISTILL) {
            if (FERMENTATION.get(stack) >= settings.fermentationSteps || MATURATION.get(stack) != 0) {
                return UNCONVERTABLE;
            }

            return settings.tickInfo.get().ticksPerDistillation;
        }

        if (type == ProcessType.FERMENT) {
            if (FERMENTATION.get(stack) >= settings.fermentationSteps) {
                return openContainer ? settings.tickInfo.get().ticksPerFermentation : UNCONVERTABLE;
            }
            return openContainer ? settings.tickInfo.get().ticksUntilAcetification : settings.tickInfo.get().ticksPerMaturation;
        }

        return UNCONVERTABLE;
    }

    @Override
    public ItemStack process(Resovoir tank, ProcessType type, boolean openContainer) {
        if (type == ProcessType.DISTILL) {
            int fermentation = FERMENTATION.get(tank.getStack());

            if (fermentation < settings.fermentationSteps) {
                return ItemStack.EMPTY;
            }

            int distillation = DISTILLATION.get(tank.getStack());

            DISTILLATION.set(tank.getStack(), distillation + 1);

            MutableFluidContainer contents = tank.getContents();

            int distilledAmount = MathHelper.floor(contents.getLevel() * MathUtils.progress(distillation, 0.5F));

            ItemStack result = tank.drain(distilledAmount, new ItemStack(Items.STONE));

            return FluidContainer.of(result).toMutable(result).withFluid(PSFluids.SLURRY).withLevel(1).asStack();
        }

        if (type == ProcessType.FERMENT) {
            ItemStack stack = tank.getStack();
            int fermentation = FERMENTATION.get(stack);

            if (openContainer) {
                if (fermentation < settings.fermentationSteps) {
                    FERMENTATION.set(stack, fermentation + 1);
                } else {
                    VINEGAR.set(stack, true);
                }
            } else {
                MATURATION.set(stack, MATURATION.get(stack) + 1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Text getName(ItemStack stack) {

        if (VINEGAR.get(stack)) {
            return Text.translatable(getTranslationKey() + ".vinegar");
        }

        int fermentation = FERMENTATION.get(stack);
        int distillation = DISTILLATION.get(stack);
        int maturation = MATURATION.get(stack);

        if (distillation > 0) {
            if (maturation > 0) {
                return Text.translatable(getTranslationKey() + ".mature.distilled", maturation, distillation);
            }

            return Text.translatable(getTranslationKey() + ".distilled", distillation);
        }

        if (maturation > 0) {
            return Text.translatable(getTranslationKey() + ".mature", maturation);
        }

        if (fermentation > 0) {
            return Text.translatable(getTranslationKey() + ".fermented." + fermentation);
        }

        return super.getName(stack);
    }

    @Override
    public int getColor(ItemStack stack) {
        return MathUtils.mixColors(
                MathUtils.mixColors(
                        super.getColor(stack),
                        getDistilledColor(stack),
                        MathUtils.progress(DISTILLATION.get(stack))
                ),
                getMatureColor(stack),
                MathUtils.progress(MATURATION.get(stack) * 0.2F)
        );
    }

    @Override
    public Identifier getSymbol(ItemStack stack) {
        var specialName = settings.types.getSpecialName(stack, this);
        if (specialName != null) {
            return getId().withPath(p -> "textures/fluid/" + specialName.value() + ".png");
        }
        return super.getSymbol(stack);
    }

    @Override
    public void getDefaultStacks(FluidContainer container, Consumer<ItemStack> consumer) {
        super.getDefaultStacks(container, consumer);
        settings.types.names().forEach(namedAlcohol -> {
            ItemStack stack = getDefaultStack(container);
            if (DISTILLATION.set(stack, StatePredicate.getUnboxedMin(namedAlcohol.predicate().distillationRange().getMin())) != 0
                && MATURATION.set(stack, StatePredicate.getUnboxedMin(namedAlcohol.predicate().maturationRange().getMin())) != 0
                && FERMENTATION.set(stack, StatePredicate.getUnboxedMin(namedAlcohol.predicate().fermentationRange().getMin())) != 0) {
                consumer.accept(stack);
            }
        });
    }

    @Override
    public boolean isSuitableContainer(FluidContainer container) {
        return container == PSItems.GLASS_CHALICE
            || container == PSItems.WOODEN_MUG
            || container.asItem().getDefaultStack().isIn(PSTags.Items.BOTTLES);
    }

    public static class Settings extends DrugFluid.Settings {
        private AlcoholicDrinkTypes types;

        int fermentationSteps;

        private double fermentationAlcohol;
        private double distillationAlcohol;
        private double maturationAlcohol;

        private int matureColor = 0xcc592518;
        private int distilledColor = 0x33ffffff;

        public Supplier<PSConfig.Balancing.FluidProperties.TickInfo> tickInfo;

        public Settings matureColor(int matureColor) {
            this.matureColor = matureColor;
            return this;
        }

        public Settings distilledColor(int distilledColor) {
            this.distilledColor = distilledColor;
            return this;
        }

        public Settings types(AlcoholicDrinkTypes types) {
            this.types = types;
            return this;
        }

        public Settings fermentation(int fermentationSteps) {
            this.fermentationSteps = fermentationSteps;
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
