/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;
import ivorius.psychedelicraft.fluid.physical.PhysicalFluid;
import ivorius.psychedelicraft.fluid.physical.PlacedFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.State;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
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
    public static final Registry<SimpleFluid> REGISTRY = FabricRegistryBuilder.createDefaulted(RegistryKey.<SimpleFluid>ofRegistry(Psychedelicraft.id("fluids")), EMPTY_KEY).buildAndRegister();
    private static final Map<Identifier, SimpleFluid> VANILLA_FLUIDS = new HashMap<>();
    public static final Codec<SimpleFluid> CODEC = Identifier.CODEC.xmap(SimpleFluid::byId, SimpleFluid::getId);
    public static final PacketCodec<RegistryByteBuf, SimpleFluid> PACKET_CODEC = PacketCodecs.registryValue(REGISTRY.getKey());

    protected final Identifier id;

    private final Identifier symbol;

    private final boolean custom;
    private final boolean empty;

    private final Settings settings;

    private final PhysicalFluid physical;

    private final ItemFluids defaultStack;

    public SimpleFluid(Identifier id, Settings settings) {
        this(id, settings, false);
    }

    public SimpleFluid(Identifier id, Settings settings, boolean empty) {
        this.id = id;
        this.settings = settings;
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.custom = true;
        this.empty = empty;
        this.defaultStack = ItemFluids.create(this, 1, Map.of());
        physical = new PhysicalFluid(id, this);
        Registry.register(REGISTRY, id, this);
        FluidVariantAttributes.register(physical.getStandingFluid(), new FluidVariantAttributeHandler() {
            @Override
            public Text getName(FluidVariant fluidVariant) {
                return SimpleFluid.this.getName(ItemFluids.of(fluidVariant, 1));
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
        this.defaultStack = ItemFluids.create(this, 1, Map.of());
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

    public Identifier getSymbol(ItemFluids stack) {
        return symbol;
    }

    public Optional<Identifier> getFlowTexture(ItemFluids stack) {
        return Optional.empty();
    }

    public final ItemFluids getStack(State<?, ?> state, int amount) {
        Map<String, Integer> attributes = new HashMap<>();
        getStateManager().writeAttributes(state, attributes);
        return ItemFluids.create(this, amount, attributes);
    }

    public final FluidState getFluidState(ItemFluids stack) {
        return getStateManager().readAttributes(getPhysical().getDefaultState(), stack);
    }

    public PhysicalFluid getPhysical() {
        return physical;
    }

    public boolean isCustomFluid() {
        return custom;
    }

    public int getColor(ItemFluids stack) {
        return settings.color;
    }

    public int getViscocity() {
        return settings.viscocity;
    }

    protected String getTranslationKey() {
        return Util.createTranslationKey(isCustomFluid() ? "fluid" : "block", id);
    }

    public final ItemFluids getDefaultStack() {
        return defaultStack;
    }

    public final ItemFluids getDefaultStack(int amount) {
        return amount == 1 ? getDefaultStack() : ItemFluids.create(this, amount, Map.of());
    }

    public void getDefaultStacks(ItemStack stack, Consumer<ItemStack> consumer) {
        int capacity = FluidCapacity.get(stack);
        if (capacity > 0) {
            stack = stack.copy();
            stack.set(PSComponents.FLUIDS, getDefaultStack(capacity));
            consumer.accept(stack);
        }
    }

    public Text getName(ItemFluids stack) {
        return Text.translatable(getTranslationKey());
    }

    public void appendTooltip(ItemFluids stack, List<Text> tooltip, TooltipType type) {

    }

    public void appendTankTooltip(ItemFluids stack, @Nullable World world, List<Text> tooltip, FluidProcessingBlockEntity tank) {

    }

    public boolean isSuitableContainer(ItemStack container) {
        return !container.isIn(PSTags.Items.BARRELS);
    }

    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.DRINK_RECEPTICALS;
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

    @Deprecated
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

        public abstract ItemFluids set(ItemFluids fluids, T value);

        public abstract T get(ItemFluids fluids);

        public final T get(ItemStack stack) {
            return get(ItemFluids.of(stack));
        }

        public abstract void set(Map<String, Integer> attributes, T value);

        public abstract ItemFluids cycle(ItemFluids fluids);

        public final ItemStack set(ItemStack stack, T value) {
            return ItemFluids.set(stack, set(ItemFluids.of(stack), value));
        }

        public abstract Stream<Pair<T, T>> steps();

        public static Attribute<Integer> ofInt(String name, int min, int max) {
            return new Attribute<>() {
                @Override
                public ItemFluids set(ItemFluids fluids, Integer value) {
                    return fluids.withAttribute(name, MathHelper.clamp(value, min, max));
                }

                @Override
                public void set(Map<String, Integer> attributes, Integer value) {
                    attributes.put(name, value);
                }

                @Override
                public Integer get(ItemFluids fluids) {
                    return MathHelper.clamp(fluids.attributes().getOrDefault(name, 0), min, max);
                }

                @Override
                public Stream<Pair<Integer, Integer>> steps() {
                    return IntStream.range(min, max).mapToObj(i -> new Pair<>(i, i + 1));
                }

                @Override
                public ItemFluids cycle(ItemFluids fluids) {
                    int value = get(fluids);
                    return value < max ? set(fluids, value + 1) : fluids;
                }
            };
        }

        public static Attribute<Boolean> ofBoolean(String name) {
            return new Attribute<>() {
                @Override
                public ItemFluids set(ItemFluids fluids, Boolean value) {
                    return fluids.withAttribute(name, value ? 1 : 0);
                }

                @Override
                public void set(Map<String, Integer> attributes, Boolean value) {
                    attributes.put(name, value ? 1 : 0);
                }

                @Override
                public Boolean get(ItemFluids fluids) {
                    return fluids.attributes().getOrDefault(name, 0) != 0;
                }

                @Override
                public Stream<Pair<Boolean, Boolean>> steps() {
                    return Stream.of(new Pair<>(false, true));
                }

                @Override
                public ItemFluids cycle(ItemFluids fluids) {
                    return !get(fluids) ? set(fluids, true) : fluids;
                }
            };
        }
    }
}
