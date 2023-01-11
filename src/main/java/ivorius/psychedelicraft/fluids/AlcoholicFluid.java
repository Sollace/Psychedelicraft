package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import ivorius.psychedelicraft.items.FluidContainerItem;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by lukas on 25.11.14.
 */
public class AlcoholicFluid extends DrugFluid implements Processable {
    final Settings settings;

    public AlcoholicFluid(Identifier id, Settings settings) {
        super(id, settings.drinkable());
        this.settings = settings;
    }

    public int getMatureColor() {
        return settings.matureColor;
    }

    public int getDistilledColor() {
        return settings.distilledColor;
    }

    protected int getDistilledColor(ItemStack stack) {
        return getDistilledColor();
    }

    protected int getMatureColor(ItemStack stack) {
        return getMatureColor();
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemStack fluidStack, List<DrugInfluence> list) {
        super.getDrugInfluencesPerLiter(fluidStack, list);

        double alcohol = getFermentation(fluidStack) / (double) settings.fermentationSteps * settings.fermentationAlcohol
                + settings.distillationAlcohol * MathUtils.progress(getDistillation(fluidStack))
                + settings.maturationAlcohol * MathUtils.progress(getMaturation(fluidStack) * 0.2F);

        list.add(new DrugInfluence("Alcohol", 20, 0.003, 0.002, alcohol));
    }

    @Override
    public int getProcessingTime(ItemStack stack, ProcessType type, boolean openContainer) {
        if (type == ProcessType.DISTILL) {
            if (getFermentation(stack) < settings.fermentationSteps || getMaturation(stack) != 0) {
                return UNCONVERTABLE;
            }

            return settings.tickInfo.get().ticksPerDistillation;
        }

        if (type == ProcessType.FERMENT) {
            if (getFermentation(stack) < settings.fermentationSteps) {
                return openContainer ? settings.tickInfo.get().ticksPerFermentation : UNCONVERTABLE;
            }
            return openContainer ? settings.tickInfo.get().ticksUntilAcetification : settings.tickInfo.get().ticksPerMaturation;
        }

        return UNCONVERTABLE;
    }

    @Override
    public ItemStack process(ItemStack stack, ProcessType type, boolean openContainer) {
        if (type == ProcessType.DISTILL) {
            int fermentation = getFermentation(stack);

            if (fermentation < settings.fermentationSteps) {
                return ItemStack.EMPTY;
            }

            int distillation = getDistillation(stack);

            setDistillation(stack, distillation + 1);
            int distilledAmount = MathHelper.floor(stack.getCount() * MathUtils.progress(distillation, 0.5F));

            stack.split(distilledAmount);

            ItemStack result = new ItemStack(Items.AIR, stack.getCount() - distilledAmount);
            FluidContainerItem.of(result).setFluid(result, PSFluids.SLURRY);
            FluidContainerItem.of(result).setLevel(result, 1);
            return result;
        }

        if (type == ProcessType.FERMENT) {
            int fermentation = getFermentation(stack);

            if (openContainer) {
                if (fermentation < settings.fermentationSteps) {
                    setFermentation(stack, fermentation + 1);
                } else {
                    setIsVinegar(stack, true);
                }
            } else {
                setMaturation(stack, getMaturation(stack) + 1);
            }
        }
        return ItemStack.EMPTY;
    }

    public int getFermentation(ItemStack stack) {
        return MathHelper.clamp(getFluidTag(stack, true).getInt("fermentation"), 0, settings.fermentationSteps);
    }

    public void setFermentation(ItemStack stack, int fermentation) {
        getFluidTag(stack, false).putInt("fermentation", fermentation);
    }

    public int getDistillation(ItemStack stack) {
        return Math.max(getFluidTag(stack, true).getInt("distillation"), 0);
    }

    public void setDistillation(ItemStack stack, int distillation) {
        getFluidTag(stack, false).putInt("distillation", distillation);
    }

    public int getMaturation(ItemStack stack) {
        return Math.max(getFluidTag(stack, true).getInt("maturation"), 0);
    }

    public void setMaturation(ItemStack stack, int maturation) {
        getFluidTag(stack, false).putInt("maturation", maturation);
    }

    public boolean isVinegar(ItemStack stack) {
        return getFluidTag(stack, true).getBoolean("isVinegar");
    }

    public void setIsVinegar(ItemStack stack, boolean isVinegar) {
        getFluidTag(stack, false).putBoolean("isVinegar", isVinegar);
    }

    @Override
    public Text getName(ItemStack stack) {

        if (isVinegar(stack)) {
            return Text.translatable(getTranslationKey() + ".vinegar");
        }

        int fermentation = getFermentation(stack);
        int distillation = getDistillation(stack);
        int maturation = getMaturation(stack);

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
                        MathUtils.progress(getDistillation(stack))
                ),
                getMatureColor(stack),
                MathUtils.progress(getMaturation(stack) * 0.2F)
        );
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
