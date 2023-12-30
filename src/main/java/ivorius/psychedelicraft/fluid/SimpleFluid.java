/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.fluid.container.VariantMarshal;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;
import ivorius.psychedelicraft.fluid.physical.PhysicalFluid;
import ivorius.psychedelicraft.fluid.physical.PlacedFluid;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.State;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Created by lukas on 29.10.14.
 * Updated by Sollace
 */
public class SimpleFluid {
    public static final Identifier EMPTY_KEY = Psychedelicraft.id("empty");
    private static final Registry<SimpleFluid> REGISTRY = FabricRegistryBuilder.createDefaulted(RegistryKey.<SimpleFluid>ofRegistry(Psychedelicraft.id("fluids")), EMPTY_KEY).buildAndRegister();
    private static final Map<Identifier, SimpleFluid> VANILLA_FLUIDS = new HashMap<>();

    protected final Identifier id;

    private final Identifier symbol;

    private final boolean custom;
    private final boolean empty;

    private final Settings settings;

    private final PhysicalFluid physical;

    public SimpleFluid(Identifier id, Settings settings) {
        this(id, settings, false);
    }

    public SimpleFluid(Identifier id, Settings settings, boolean empty) {
        this.id = id;
        this.settings = settings;
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.custom = true;
        this.empty = empty;
        physical = new PhysicalFluid(id, this);
        Registry.register(REGISTRY, id, this);
        FluidVariantAttributes.register(physical.getStandingFluid(), new FluidVariantAttributeHandler() {
            @Override
            public Text getName(FluidVariant fluidVariant) {
                return SimpleFluid.this.getName(VariantMarshal.unpackFluid(Items.STONE.getDefaultStack(), fluidVariant, 1).asStack());
            }
        });
    }

    private SimpleFluid(Identifier id, int color, PhysicalFluid physical) {
        this.id = id;
        this.empty = false;
        this.settings = new Settings().color(color);
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.custom = false;
        this.physical = physical;
    }

    @SuppressWarnings("unchecked")
    protected <S extends Settings> S getSettings() {
        return (S)settings;
    }

    public final FluidStateManager getStateManager() {
        return settings.stateManager;
    }

    public final boolean isEmpty() {
        return empty;
    }

    public final Identifier getId() {
        return id;
    }

    public Identifier getSymbol(ItemStack stack) {
        return symbol;
    }

    public Optional<Identifier> getFlowTexture(ItemStack stack) {
        return Optional.empty();
    }

    public final ItemStack getStack(State<?, ?> state, FluidContainer container) {
        return getStateManager().writeStack(state, container.getDefaultStack(this));
    }

    public final FluidState getFluidState(ItemStack stack) {
        return getStateManager().readStack(getPhysical().getDefaultState(), stack);
    }

    public PhysicalFluid getPhysical() {
        return physical;
    }

    public boolean isCustomFluid() {
        return custom;
    }

    public int getColor(ItemStack stack) {
        return settings.color;
    }

    public int getViscocity() {
        return settings.viscocity;
    }

    protected String getTranslationKey() {
        return Util.createTranslationKey(isCustomFluid() ? "fluid" : "block", id);
    }

    public final ItemStack getDefaultStack(FluidContainer container) {
        return getDefaultStack(container, container.getMaxCapacity());
    }

    public final ItemStack getDefaultStack() {
        return getDefaultStack(FluidContainer.UNLIMITED);
    }

    public final ItemStack getDefaultStack(int level) {
        return getDefaultStack(FluidContainer.UNLIMITED, level);
    }

    public ItemStack getDefaultStack(FluidContainer container, int level) {
        return container.toMutable(container.getDefaultStack(this)).withLevel(level).asStack();
    }

    public void getDefaultStacks(FluidContainer container, Consumer<ItemStack> consumer) {
        consumer.accept(getDefaultStack(container));
    }

    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey());
    }

    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {

    }

    @SuppressWarnings("deprecation")
    public boolean isSuitableContainer(FluidContainer container) {
        return !container.asItem().getRegistryEntry().isIn(PSTags.Items.BARRELS);
    }

    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        if (!custom) {
            state.randomDisplayTick(world, pos, random);
        }
    }

    public void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
    }

    public static final boolean isEquivalent(SimpleFluid fluidA, ItemStack selfStack, SimpleFluid fluidB, ItemStack otherStack) {
        return fluidA == fluidB && fluidA.getHash(selfStack) == fluidB.getHash(otherStack);
    }

    public int getHash(ItemStack stack) {
        return hashCode();
    }

    public static SimpleFluid byId(@Nullable Identifier id) {
        if (id == null) {
            return PSFluids.EMPTY;
        }
        return REGISTRY.getOrEmpty(id).orElseGet(() -> Registries.FLUID.getOrEmpty(id).map(SimpleFluid::forVanilla).orElse(PSFluids.EMPTY));
    }

    public static SimpleFluid forVanilla(@Nullable Fluid fluid) {
        if (fluid instanceof PlacedFluid pf) {
            return pf.getType();
        }
        if (fluid == null || fluid == Fluids.EMPTY) {
            return PSFluids.EMPTY;
        }
        Fluid still = toStill(fluid);
        Identifier id = Registries.FLUID.getId(still);
        return VANILLA_FLUIDS.computeIfAbsent(id, i -> new SimpleFluid(i, 0xFFFFFFFF,
                new PhysicalFluid(still, toFlowing(still), (FluidBlock)still.getDefaultState().getBlockState().getBlock())
        ));
    }

    private static Fluid toStill(Fluid fluid) {
        return fluid instanceof FlowableFluid ? ((FlowableFluid)fluid).getStill() : fluid;
    }

    private static Fluid toFlowing(Fluid fluid) {
        return fluid instanceof FlowableFluid ? ((FlowableFluid)fluid).getFlowing() : fluid;
    }

    public static Iterable<SimpleFluid> all() {
        return REGISTRY;
    }

    @SuppressWarnings("unchecked")
    public static class Settings {
        private int color;
        private int viscocity = 1;
        final FluidStateManager stateManager = new FluidStateManager(new HashSet<>());

        public <T extends Settings> T color(int color) {
            this.color = color;
            return (T)this;
        }

        public <T extends Settings> T viscocity(int viscocity) {
            this.viscocity = viscocity;
            return (T)this;
        }

        public <T extends Settings> T with(FluidStateManager.FluidProperty<?> property) {
            stateManager.properties().add(property);
            return (T)this;
        }
    }

    public abstract static class Attribute<T extends Comparable<T>> {
        public abstract T get(ItemStack stack);

        public abstract ItemStack set(ItemStack stack, T value);

        public abstract T get(MutableFluidContainer stack);

        public abstract MutableFluidContainer set(MutableFluidContainer stack, T value);

        public abstract void forEachStep(BiConsumer<T , T> consumer);

        public static Attribute<Integer> ofInt(String name, int min, int max) {
            return new Attribute<>() {
                @Override
                public Integer get(ItemStack stack) {
                    return MathHelper.clamp(FluidContainer.getFluidAttributesTag(stack, true).getInt(name), min, max);
                }

                @Override
                public ItemStack set(ItemStack stack, Integer value) {
                    FluidContainer.getFluidAttributesTag(stack, false).putInt(name, value);
                    return stack;
                }

                @Override
                public Integer get(MutableFluidContainer stack) {
                    return MathHelper.clamp(stack.getAttributes().getInt(name), min, max);
                }

                @Override
                public MutableFluidContainer set(MutableFluidContainer stack, Integer value) {
                    NbtCompound attributes = stack.getAttributes().copy();
                    attributes.putInt(name, value);
                    stack.withAttributes(attributes);
                    return stack;
                }

                @Override
                public void forEachStep(BiConsumer<Integer, Integer> consumer) {
                    for (int i = min; i < max; i++) {
                        consumer.accept(i, i + 1);
                    }
                }
            };
        }

        public static Attribute<Boolean> ofBoolean(String name) {
            return new Attribute<>() {
                @Override
                public Boolean get(ItemStack stack) {
                    return FluidContainer.getFluidAttributesTag(stack, true).getBoolean(name);
                }

                @Override
                public ItemStack set(ItemStack stack, Boolean value) {
                    FluidContainer.getFluidAttributesTag(stack, false).putBoolean(name, value);
                    return stack;
                }

                @Override
                public Boolean get(MutableFluidContainer stack) {
                    return stack.getAttributes().getBoolean(name);
                }

                @Override
                public MutableFluidContainer set(MutableFluidContainer stack, Boolean value) {
                    NbtCompound attributes = stack.getAttributes().copy();
                    attributes.putBoolean(name, value);
                    stack.withAttributes(attributes);
                    return stack;
                }

                @Override
                public void forEachStep(BiConsumer<Boolean, Boolean> consumer) {
                    consumer.accept(false, true);
                }
            };
        }
    }
}
