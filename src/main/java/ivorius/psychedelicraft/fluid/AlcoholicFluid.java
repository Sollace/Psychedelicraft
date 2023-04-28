package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        super(id, settings.drinkable());
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
    public int getProcessingTime(Resovoir tank, ProcessType type, boolean openContainer) {
        if (type == ProcessType.DISTILL) {
            if (FERMENTATION.get(tank.getContents()) < FERMENTATION_STEPS || MATURATION.get(tank.getContents()) != 0) {
                return UNCONVERTABLE;
            }

            return settings.tickInfo.get().ticksPerDistillation;
        }

        if (type == ProcessType.FERMENT) {
            if (FERMENTATION.get(tank.getContents()) < FERMENTATION_STEPS) {
                return openContainer ? settings.tickInfo.get().ticksPerFermentation : UNCONVERTABLE;
            }
            return openContainer ? settings.tickInfo.get().ticksUntilAcetification : settings.tickInfo.get().ticksPerMaturation;
        }

        return UNCONVERTABLE;
    }

    @Override
    public ItemStack process(Resovoir tank, ProcessType type, boolean openContainer) {
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

        if (type == ProcessType.FERMENT) {
            int fermentation = FERMENTATION.get(contents);

            if (openContainer) {
                if (fermentation < FERMENTATION_STEPS) {
                    FERMENTATION.set(contents, fermentation + 1);
                } else {
                    VINEGAR.set(contents, true);
                }
            } else {
                MATURATION.set(contents, MATURATION.get(contents) + 1);
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
                        getDistilledColor(),
                        MathUtils.progress(DISTILLATION.get(stack))
                ),
                getMatureColor(),
                MathUtils.progress(MATURATION.get(stack) * 0.2F)
        );
    }

    @Override
    public Identifier getSymbol(ItemStack stack) {
        var specialName = settings.displayNames.find(stack);
        if (specialName != null) {
            return getId().withPath(p -> "textures/fluid/" + specialName + ".png");
        }
        return super.getSymbol(stack);
    }

    @Override
    public Optional<Identifier> getFlowTexture(ItemStack stack) {
        return Optional.ofNullable(settings.textures.find(stack))
                .map(DrinkTypes.Icons::still)
                .map(name -> flowTextures.computeIfAbsent(name, this::getFlowTexture));
    }

    @Override
    public void getDefaultStacks(FluidContainer container, Consumer<ItemStack> consumer) {
        super.getDefaultStacks(container, consumer);
        settings.displayNames.variants().forEach(namedAlcohol -> {
            ItemStack stack = getDefaultStack(container);
            namedAlcohol.predicate().applyTo(stack);
            consumer.accept(stack);
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSuitableContainer(FluidContainer container) {
        return container.asItem().getRegistryEntry().isIn(PSTags.Items.SUITABLE_ALCOHOLIC_DRINK_RECEPTICALS);
    }

    public static class Settings extends DrugFluid.Settings {
        private DrinkTypes.VariantSet<String> displayNames = DrinkTypes.VariantSet.empty();
        private DrinkTypes.VariantSet<DrinkTypes.Icons> textures = DrinkTypes.VariantSet.empty();

        private double fermentationAlcohol;
        private double distillationAlcohol;
        private double maturationAlcohol;

        private int matureColor = 0xcc592518;
        private int distilledColor = 0x33ffffff;

        DrugType drugType = DrugType.ALCOHOL;

        public Supplier<PSConfig.Balancing.FluidProperties.TickInfo> tickInfo;

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

        public Settings variants(DrinkTypes.VariantSet<String> names, DrinkTypes.VariantSet<DrinkTypes.Icons> textures) {
            this.displayNames = names;
            this.textures = textures;
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
