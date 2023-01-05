package ivorius.psychedelicraft.fluids;

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
public class AlcoholicFluid extends FluidDrug implements Fermentable, FluidDistillable {
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

        int fermentation = getFermentation(fluidStack);
        int distillation = getDistillation(fluidStack);
        int maturation = getMaturation(fluidStack);

        double alcohol = fermentation / (double) settings.fermentationSteps * settings.fermentationAlcohol
                + settings.distillationAlcohol * (1.0 - 1.0 / (1.0 + distillation))
                + settings.maturationAlcohol * (1.0 - 1.0 / (1.0 + maturation * 0.2));

        list.add(new DrugInfluence("Alcohol", 20, 0.003, 0.002, alcohol));
    }

    @Override
    public int getFermentationTime(ItemStack stack, boolean openContainer) {
        if (getFermentation(stack) < settings.fermentationSteps) {
            return openContainer ? settings.tickInfo.get().ticksPerFermentation : UNFERMENTABLE;
        }
        return openContainer ? settings.tickInfo.get().ticksUntilAcetification : settings.tickInfo.get().ticksPerMaturation;
    }

    @Override
    public ItemStack ferment(ItemStack stack, boolean openContainer) {
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
        return ItemStack.EMPTY;
    }

    @Override
    public int distillationTime(ItemStack stack) {
        if (getFermentation(stack) < settings.fermentationSteps || getMaturation(stack) != 0) {
            return UNDISTILLABLE;
        }

        return settings.tickInfo.get().ticksPerDistillation;
    }

    @Override
    public ItemStack distillStep(ItemStack stack) {
        int fermentation = getFermentation(stack);

        if (fermentation < settings.fermentationSteps) {
            return ItemStack.EMPTY;
        }

        int distillation = getDistillation(stack);

        setDistillation(stack, distillation + 1);
        int distilledAmount = MathHelper.floor(stack.getCount() * (1.0f - 0.5f / (distillation + 1.0f)));

        stack.split(distilledAmount);

        ItemStack result = new ItemStack(Items.AIR, stack.getCount() - distilledAmount);
        FluidContainerItem.of(result).setFluid(result, PSFluids.slurry);
        FluidContainerItem.of(result).setLevel(result, 1);
        return result;
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
        int slurryColor = super.getColor(stack);
        int matureColor = getMatureColor(stack);
        int clearColor = getDistilledColor(stack);

        int distillation = getDistillation(stack);
        int maturation = getMaturation(stack);

        int baseFluidColor = MathUtils.mixColors(slurryColor, clearColor, (1.0f - 1.0f / (1.0f + distillation)));
        return MathUtils.mixColors(baseFluidColor, matureColor, (1.0f - 1.0f / (1.0f + maturation * 0.2f)));
    }

    public static class Settings extends FluidDrug.Settings {
        private AlcoholicDrinkTypes types;

        int fermentationSteps;

        private double fermentationAlcohol;
        private double distillationAlcohol;
        private double maturationAlcohol;

        private int matureColor = 0xcc592518;
        private int distilledColor = 0x33ffffff;

        public Supplier<TickInfo> tickInfo;

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

        public Settings tickRate(Supplier<TickInfo> tickInfo) {
            this.tickInfo = tickInfo;
            return this;
        }
    }

    public static class TickInfo {
        public int ticksPerFermentation;
        public int ticksPerDistillation;
        public int ticksPerMaturation;
        public int ticksUntilAcetification;
    }
}
