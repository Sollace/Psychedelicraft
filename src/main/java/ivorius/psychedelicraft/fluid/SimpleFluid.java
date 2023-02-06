/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.*;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.*;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
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
    private static final Registry<SimpleFluid> REGISTRY = FabricRegistryBuilder.createDefaulted(SimpleFluid.class, Psychedelicraft.id("fluids"), EMPTY_KEY).buildAndRegister();
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
    }

    private SimpleFluid(Identifier id, int color, PhysicalFluid physical) {
        this.id = id;
        this.empty = false;
        this.settings = new Settings().color(color);
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.custom = false;
        this.physical = physical;
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

    public ItemStack getStack(State<?, ?> state, FluidContainer container) {
        return container.getDefaultStack(this);
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

    public boolean isSuitableContainer(FluidContainer container) {
        return !container.asItem().getDefaultStack().isIn(PSTags.Items.BARRELS);
    }

    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
    }

    public static SimpleFluid byId(@Nullable Identifier id) {
        if (id == null) {
            return PSFluids.EMPTY;
        }
        return REGISTRY.getOrEmpty(id).orElseGet(() -> Registries.FLUID.getOrEmpty(id).map(SimpleFluid::forVanilla).orElse(PSFluids.EMPTY));
    }

    public static SimpleFluid forVanilla(Fluid fluid) {
        if (fluid instanceof PhysicalFluid.PlacedFluid pf) {
            return pf.getType();
        }
        if (fluid == Fluids.EMPTY) {
            return PSFluids.EMPTY;
        }
        Fluid still = toStill(fluid);
        Identifier id = Registries.FLUID.getId(fluid);
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

        public <T extends Settings> T color(int color) {
            this.color = color;
            return (T)this;
        }

        public <T extends Settings> T viscocity(int viscocity) {
            this.viscocity = viscocity;
            return (T)this;
        }
    }

    public abstract static class Attribute<T extends Comparable<T>> {
        public abstract T get(ItemStack stack);

        public abstract T set(ItemStack stack, T value);

        public static Attribute<Integer> ofInt(String name, int min, int max) {
            return new Attribute<>() {
                @Override
                public Integer get(ItemStack stack) {
                    return MathHelper.clamp(FluidContainer.getFluidAttributesTag(stack, true).getInt(name), min, max);
                }

                @Override
                public Integer set(ItemStack stack, Integer value) {
                    FluidContainer.getFluidAttributesTag(stack, false).putInt(name, value);
                    return value;
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
                public Boolean set(ItemStack stack, Boolean value) {
                    FluidContainer.getFluidAttributesTag(stack, false).putBoolean(name, value);
                    return value;
                }
            };
        }
    }
}
