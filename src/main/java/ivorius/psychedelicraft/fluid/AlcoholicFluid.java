package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.alcohol.AlcoholicFluidState;
import ivorius.psychedelicraft.fluid.alcohol.DrinkType;
import ivorius.psychedelicraft.fluid.alcohol.DrinkTypes;
import ivorius.psychedelicraft.fluid.alcohol.Maturity;
import ivorius.psychedelicraft.fluid.alcohol.TickRate;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

/**
 * Created by lukas on 25.11.14.
 */
public class AlcoholicFluid extends DrugFluid implements Processable, TickRate.Tickable {
    public static final Attribute<Integer> DISTILLATION = Attribute.ofInt("distillation", 0, 16);
    public static final Attribute<Integer> MATURATION = Attribute.ofInt("maturation", 0, 16);

    private static final int FERMENTATION_STEPS = 2;
    public static final Attribute<Integer> FERMENTATION = Attribute.ofInt("fermentation", 0, FERMENTATION_STEPS);
    public static final Attribute<Boolean> VINEGAR = Attribute.ofBoolean("vinegar");

    final Settings settings;

    public AlcoholicFluid(Identifier id, Settings settings) {
        super(id, settings.drinkable().with(new FluidStateManager.FluidProperty<>(IntProperty.of("variant", 0, settings.variants.variants().size()),
                (properties, value) -> settings.variants.findState(value).apply(properties),
                stack -> settings.variants.findId(stack)))
        );
        this.settings = settings;
    }

    @Override
    public boolean hasRandomTicks() {
        return true;
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

        double alcohol = getAlcoholContent(stack);

        if (alcohol > 0) {
            consumer.accept(new DrugInfluence(settings.drugType, 20, 0.003, 0.002, alcohol));
        }
        settings.variants.find(stack).extraDrug().ifPresent(consumer);
    }

    double getAlcoholContent(ItemFluids stack) {
        if (settings.variants.find(stack).isOf(DrinkType.VINEGAR)) {
            return 0;
        }
        return settings.fermentationAlcohol * (FERMENTATION.get(stack) / (double) FERMENTATION_STEPS)
                + settings.distillationAlcohol * MathUtils.progress(DISTILLATION.get(stack))
                + settings.maturationAlcohol * MathUtils.progress(MATURATION.get(stack) * 0.2F);
    }


    @Override
    public void onRandomTick(ServerWorld world, BlockPos pos, FluidState state, Random random) {
        if (getAlcoholContent(getStack(state, 1)) > 0.3) {
            world.getOtherEntities(null, Box.of(pos.toCenterPos(), 5, 5, 5)).forEach(entity -> {
                if (random.nextInt(450) == 0) {
                    DrugProperties.of(entity).ifPresent(properties -> {
                        properties.addToDrug(DrugType.ALCOHOL, 0.1);
                    });
                }
            });
        }
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
            case FERMENT -> getTickRate().ticksPerFermentation();
            case DISTILL -> FERMENTATION.get(tank.getContents()) == 0 || MATURATION.get(tank.getContents()) != 0 ? UNCONVERTABLE : getTickRate().ticksPerDistillation();
            case MATURE -> FERMENTATION.get(tank.getContents()) == 0 ? UNCONVERTABLE : getTickRate().ticksPerMaturation();
            case ACETIFY -> VINEGAR.get(tank.getContents()) ? UNCONVERTABLE : getTickRate().ticksUntilAcetification();
            case PURIFY -> 1;
            default -> UNCONVERTABLE;
        };
    }

    @Override
    public void process(Context context, ProcessType type, ByProductConsumer output) {
        Resovoir tank = context.getPrimaryTank();
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
                tank.setContents(VINEGAR.set(tank.getContents(), true));
                break;
            case PURIFY:
                double alcohol = getAlcoholContent(tank.getContents()) / 10;
                int consumed = Math.max(1, tank.getContents().amount() / 10);
                tank.drain(consumed * 2);
                output.accept(
                        alcohol == 0
                        ? SimpleFluid.of(Fluids.WATER).getDefaultStack(consumed)
                        : PSFluids.ETHANOL.getDefaultStack((int)Math.ceil(alcohol) * consumed)
                );
                break;
            default:
        }
    }

    @Override
    public Stream<Process> getProcesses() {
        return settings.variants.variants().stream().flatMap(variant -> {
            AlcoholicFluidState state = variant.predicate().state();
            String key = "_" + variant.value().getUniqueKey();
            return Stream.of(
                    new Process(this, getId().withSuffixedPath(key + "_alco"), getAlcoTransitions(state)),
                    new Process(this, getId().withSuffixedPath(key + "_chem"), getChemTransitions(state))
            );
        });
    }

    private List<Transition> getAlcoTransitions(AlcoholicFluidState state) {
        int fermentations = state.fermentation();
        int distillations = state.distillation();
        int maturations = state.maturation();

        Function<ItemFluids, ItemFluids> withFerment = i -> FERMENTATION.set(i, fermentations);
        Function<ItemFluids, ItemFluids> withDistil = withFerment.andThen(i -> DISTILLATION.set(i, distillations));
        Function<ItemFluids, ItemFluids> withMature = withDistil.andThen(i -> MATURATION.set(i, maturations));

        List<Transition> result = new ArrayList<>();

        if (fermentations > 0) {
            result.add(new Transition(ProcessType.FERMENT, getTickRate().ticksPerFermentation(), fermentations, Function.identity(), withFerment));
        }
        if (state.vinegar()) {
            result.add(new Transition(ProcessType.ACETIFY, getTickRate().ticksPerFermentation(), FERMENTATION_STEPS + 1, withMature, state::apply));
        }
        if (distillations > 0) {
            result.add(new Transition(ProcessType.DISTILL, getTickRate().ticksPerDistillation(), distillations, withFerment, withDistil));
        }
        if (maturations > 0) {
            result.add(new Transition(ProcessType.MATURE, getTickRate().ticksPerMaturation(), maturations, withDistil, withMature));
        }
        return result;
    }

    @Override
    public TickRate getDefaultTickRate() {
        return settings.defaultTickInfo;
    }

    private List<Transition> getChemTransitions(AlcoholicFluidState state) {
        return List.of(new Transition(ProcessType.PURIFY, 0, 1, state::apply, to -> {
            double alcohol = getAlcoholContent(state.apply(to)) / 10;
            return alcohol == 0 ? SimpleFluid.of(Fluids.WATER).getDefaultStack(1) : PSFluids.ETHANOL.getDefaultStack((int)Math.ceil(alcohol));
        }));
    }

    @Override
    public int getHash(ItemFluids stack) {
        return Objects.hash(this, getVariant(stack));
    }

    @Override
    public Text getName(ItemFluids stack) {
        return getVariant(stack).getName(Text.translatable(getTranslationKey()));
    }

    @Override
    public String getUniqueKey(ItemFluids stack) {
        return "_" + getVariant(stack).getUniqueKey();
    }

    public DrinkType getVariant(ItemFluids stack) {
        return settings.variants.find(stack);
    }

    @Override
    public void appendTooltip(ItemFluids stack, Consumer<Text> tooltip, TooltipType type) {

        int distillation = DISTILLATION.get(stack);
        int maturation = MATURATION.get(stack);
        int fermentation = FERMENTATION.get(stack);

        if (distillation > 0) {
            tooltip.accept(Text.translatable("psychedelicraft.alcohol.distillations", distillation).formatted(Formatting.GRAY));
        }

        if (fermentation > 0) {
            tooltip.accept(Text.translatable("psychedelicraft.alcohol.fermentations", fermentation).formatted(Formatting.GRAY));
        }

        if (maturation > 0) {
            tooltip.accept(Text.translatable("psychedelicraft.alcohol.maturations", maturation, Maturity.getMaturity(maturation).getName()).formatted(Formatting.GRAY));
        }

        tooltip.accept(Text.translatable("psychedelicraft.alcohol.potency", AttributeModifiersComponent.DECIMAL_FORMAT.format(getAlcoholContent(stack))).formatted(Formatting.GRAY));

        /*if (distillation > 0 || maturation > 0 || fermentation > 0) {
            tooltip.add(Text.empty());
            tooltip.add(FlavorProfile.DEFAULT.getFlavour(distillation, fermentation, maturation));
        }*/
    }

    @Override
    public void appendTankTooltip(ItemFluids stack, @Nullable World world, List<Text> tooltip, FluidProcessingBlockEntity tank) {
        super.appendTankTooltip(stack, world, tooltip, tank);

        if (tank.getProcessType() == ProcessType.DISTILL) {
            tooltip.add(Text.literal("Requirements:"));
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
    public Stream<ItemFluids> getDefaultStacks(int capacity) {
        return settings.variants.variants().stream().map(variant -> variant.predicate().state().apply(getDefaultStack(capacity)));
    }

    public List<DrinkTypes.Variant> getVariants() {
        return settings.variants.variants();
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return (container.isIn(getPreferredContainerTag())
                || container.isOf(Items.BUCKET)
                || container.isOf(PSItems.FILLED_BUCKET))
                && !(container.isOf(PSItems.SHOT_GLASS));
    }

    @Override
    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.SUITABLE_ALCOHOLIC_DRINK_RECEPTICALS;
    }

    public static class Settings extends DrugFluid.Settings {
        protected DrinkTypes variants = DrinkTypes.empty();

        private double fermentationAlcohol;
        private double distillationAlcohol;
        private double maturationAlcohol;

        private int matureColor = 0xcc592518;
        private int distilledColor = 0x33ffffff;

        DrugType<?> drugType = DrugType.ALCOHOL;

        private TickRate defaultTickInfo = TickRate.DEFAULT;

        public Settings() {
            this.appearance = stack -> variants.find(stack).appearance();
        }

        public Settings drug(DrugType<?> drug) {
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

        public Settings tickRate(TickRate defaultTickInfo) {
            this.defaultTickInfo = defaultTickInfo;
            return this;
        }
    }
}
