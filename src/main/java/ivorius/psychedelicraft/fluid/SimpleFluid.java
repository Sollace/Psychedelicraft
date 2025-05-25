/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;

import io.netty.buffer.ByteBuf;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;
import ivorius.psychedelicraft.fluid.physical.PhysicalFluid;
import ivorius.psychedelicraft.fluid.physical.PlacedFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.State;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Created by lukas on 29.10.14.
 * Updated by Sollace
 */
public class SimpleFluid implements Combustable {
    public static final Identifier EMPTY_KEY = Psychedelicraft.id("empty");
    public static final RegistryKey<Registry<SimpleFluid>> REGISTRY_KEY = RegistryKey.<SimpleFluid>ofRegistry(Psychedelicraft.id("fluids"));
    public static final Registry<SimpleFluid> REGISTRY = FabricRegistryBuilder.from(new SimpleDefaultedRegistry<>(EMPTY_KEY.toString(), REGISTRY_KEY, Lifecycle.stable(), true)).buildAndRegister();
    protected static final Map<Identifier, SimpleFluid> ALIASED_IDS = new HashMap<>();

    public static final Codec<SimpleFluid> CODEC = Identifier.CODEC.xmap(SimpleFluid::byId, SimpleFluid::getId);
    public static final PacketCodec<ByteBuf, SimpleFluid> PACKET_CODEC = PacketCodecs.IDENTIFIER.xmap(SimpleFluid::byId, SimpleFluid::getId);

    public static SimpleFluid byId(@Nullable Identifier id) {
        if (id == null) {
            return PSFluids.EMPTY;
        }
        SimpleFluid fluid = ALIASED_IDS.get(id);
        if (fluid != null) {
            return fluid;
        }
        return REGISTRY.getOrEmpty(id).orElseGet(() -> Registries.FLUID.getOrEmpty(id).map(SimpleFluid::of).orElse(PSFluids.EMPTY));
    }

    public static SimpleFluid of(@Nullable Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return PSFluids.EMPTY;
        }
        if (fluid instanceof PlacedFluid pf) {
            return pf.getType();
        }
        Identifier id = Registries.FLUID.getId(VanillaFluid.toStill(fluid));
        SimpleFluid alias = ALIASED_IDS.get(id);
        if (alias != null) {
            return alias;
        }
        return REGISTRY.get(id);
    }

    protected final Identifier id;

    private final Identifier symbol;

    private final boolean custom;
    private final boolean empty;

    private final Settings settings;

    private final PhysicalFluid physical;

    private final Supplier<ItemFluids> defaultStack = Suppliers.memoize(() -> ItemFluids.create(this, 1, Map.of()));

    private final RegistryEntry.Reference<SimpleFluid> registryEntry = REGISTRY.createEntry(this);

    protected SimpleFluid(Identifier id, Settings settings) {
        Registry.register(REGISTRY, id, this);
        this.id = id;
        this.settings = settings;
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.custom = true;
        this.empty = false;
        physical = new PhysicalFluid(id, this);
        FluidVariantAttributes.register(physical.getStandingFluid(), new FluidVariantAttributeHandler() {
            @Override
            public Text getName(FluidVariant fluidVariant) {
                return SimpleFluid.this.getName(ItemFluids.of(fluidVariant, 1));
            }
        });
    }

    protected SimpleFluid(Identifier id, int color, PhysicalFluid physical, boolean empty) {
        this.id = id;
        this.empty = empty;
        this.settings = new Settings().color(color);
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.custom = false;
        this.physical = physical;
    }

    @Deprecated
    public RegistryEntry.Reference<SimpleFluid> getRegistryEntry() {
        return registryEntry;
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
        return settings.flowTexture;
    }

    public Optional<Identifier> getStandingTexture(ItemFluids stack) {
        return settings.stillTexture;
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

    public int getCondensationTemperature() {
        return settings.condensationPoint;
    }

    protected String getTranslationKey() {
        return Util.createTranslationKey(isCustomFluid() ? "fluid" : "block", id);
    }

    public final ItemFluids getDefaultStack() {
        return defaultStack.get();
    }

    public final ItemFluids getDefaultStack(int amount) {
        return getDefaultStack().ofAmount(amount);
    }

    public Stream<ItemFluids> getDefaultStacks(int capacity) {
        return Stream.of(getDefaultStack(capacity));
    }

    public String getUniqueKey(ItemFluids fluids) {
        return "";
    }

    public final Stream<ItemStack> getDefaultStacks(ItemStack stack) {
        int capacity = FluidCapacity.get(stack);
        if (capacity > 0 && isSuitableContainer(stack)) {
            return getDefaultStacks(capacity).map(s -> ItemFluids.set(stack.copy(), s));
        }
        return Stream.of();
    }

    public Text getName(ItemFluids stack) {
        return Text.translatable(getTranslationKey());
    }

    public void appendTooltip(ItemFluids stack, Consumer<Text> tooltip, TooltipContext type) {

    }

    public void appendTankTooltip(ItemFluids stack, @Nullable World world, List<Text> tooltip, FluidProcessingBlockEntity tank) {
        int ticksProcessed = tank.getTimeProcessed();
        int ticksNeeded = Math.abs(tank.getTimeNeeded());
        String timeRemaining = StringHelper.formatTicks(ticksNeeded - ticksProcessed);
        ProcessType processType = tank.getActiveProcess();
        tooltip.add(Text.translatable("fluid.status", processType.getStatus()).formatted(Formatting.GRAY));

        if (processType != ProcessType.IDLE) {
            tooltip.add(Text.translatable(processType.getTimeLabelTranslationKey(), timeRemaining).formatted(Formatting.GRAY));
        }
    }

    public boolean isSuitableContainer(ItemStack container) {
        return container.isIn(ConventionalItemTags.EMPTY_BUCKETS);
    }

    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.DRINK_RECEPTICALS;
    }

    public boolean hasRandomTicks() {
        return getFireStrength(getDefaultStack()) > 0;
    }

    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        if (!custom) {
            state.randomDisplayTick(world, pos, random);
        }
    }

    public void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
        if (getFireStrength(getDefaultStack()) > 0) {
            for (Direction direction : Direction.values()) {
                BlockPos side = pos.offset(direction);
                BlockState above = world.getBlockState(side);
                if (above.isAir() && !above.isOf(PSBlocks.FLAMMABLE_GAS)) {
                    world.setBlockState(side, PSBlocks.FLAMMABLE_GAS.getDefaultState());
                }
            }
        }
    }

    public int getHash(ItemFluids stack) {
        return hashCode();
    }

    @Override
    public String toString() {
        return REGISTRY.getEntry(this).getKey().map(k->k.toString()).orElse("<unregistered>");
    }

    @Override
    public float getFireStrength(ItemFluids stack) {
        return settings.flammability * stack.amount();
    }

    @Override
    public float getExplosionStrength(ItemFluids stack) {
        return settings.explosiveness * stack.amount();
    }

    @SuppressWarnings("unchecked")
    public static class Settings {
        private int color;
        private int viscocity = 1;
        private float explosiveness;
        private float flammability;
        private int condensationPoint = 15;
        final FluidStateManager stateManager = new FluidStateManager(new HashSet<>());

        private Optional<Identifier> flowTexture = Optional.empty();
        private Optional<Identifier> stillTexture = Optional.empty();

        public <T extends Settings> T condensationPoint(int temperature) {
            Preconditions.checkArgument(temperature >= 0 && temperature <= 15);
            condensationPoint = temperature;
            return (T)this;
        }

        public <T extends Settings> T color(int color) {
            this.color = color;
            return (T)this;
        }

        public <T extends Settings> T viscocity(int viscocity) {
            this.viscocity = viscocity;
            return (T)this;
        }

        public <T extends Settings> T flammability(float flammability, float explosiveness) {
            this.explosiveness = explosiveness;
            this.flammability = flammability;
            return (T)this;
        }

        public <T extends Settings> T with(FluidStateManager.FluidProperty<?> property) {
            stateManager.properties().add(property);
            return (T)this;
        }

        public <T extends Settings> T sprites(Identifier flowing, Identifier still) {
            flowTexture = Optional.ofNullable(flowing);
            stillTexture = Optional.ofNullable(still);
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
