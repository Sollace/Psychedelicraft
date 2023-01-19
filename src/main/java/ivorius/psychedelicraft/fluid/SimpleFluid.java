/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.*;
import java.util.function.Consumer;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.fluid.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * Created by lukas on 29.10.14.
 */
public class SimpleFluid {
    public static final Identifier EMPTY_KEY = Psychedelicraft.id("empty");
    private static final Registry<SimpleFluid> REGISTRY = FabricRegistryBuilder.createDefaulted(SimpleFluid.class, Psychedelicraft.id("fluids"), EMPTY_KEY).buildAndRegister();
    private static final Map<Identifier, SimpleFluid> VANILLA_FLUIDS = new HashMap<>();

    protected final Identifier id;

    private final Identifier symbol;
    private final Identifier stationaryTexture;
    private final Identifier flowingTexture;

    private int color;
    private boolean translucent;

    private final Fluid fluid;

    public SimpleFluid(Identifier id, Settings settings) {
        this.id = id;
        this.color = settings.color;
        this.translucent = settings.translucent;
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.stationaryTexture = id.withPath(p -> "textures/block/fluid/" + p + "_still.png");
        this.flowingTexture = id.withPath(p -> "textures/block/fluid/" + p + "_flow.png");
        this.fluid = Fluids.EMPTY;

        Registry.register(REGISTRY, id, this);
    }

    public SimpleFluid(Identifier id, int color, Fluid fluid) {
        this.id = id;
        this.color = color;
        this.translucent = true;
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.stationaryTexture = id.withPath(p -> "textures/block/" + p + "_still.png");
        this.flowingTexture = id.withPath(p -> "textures/block/" + p + "_flow.png");
        this.fluid = fluid;
    }

    public final boolean isEmpty() {
        return this == PSFluids.EMPTY;
    }

    public final Identifier getId() {
        return id;
    }

    public final Identifier getSymbol() {
        return symbol;
    }

    public final Identifier getStationaryTexture() {
        return stationaryTexture;
    }

    public final Identifier getFlowingTexture() {
        return flowingTexture;
    }

    public FluidState getFluidState(int level) {
        return fluid.getDefaultState().withIfExists(FlowableFluid.LEVEL, level);
    }

    public int getColor(ItemStack stack) {
        return color;
    }

    public final int getTranslucentColor(ItemStack stack) {
        int color = getColor(stack);
        if (!isTranslucent()) {
            return color | 0xFF000000;
        }
        return color;
    }

    public boolean isTranslucent() {
        return translucent;
    }

    protected String getTranslationKey() {
        return Util.createTranslationKey(fluid == Fluids.EMPTY ? "fluid" : "block", id);
    }

    public final ItemStack getDefaultStack(FluidContainerItem container) {
        return getDefaultStack(container, container.getMaxCapacity());
    }

    public final ItemStack getDefaultStack() {
        return getDefaultStack(FluidContainerItem.DEFAULT);
    }

    public final ItemStack getDefaultStack(int level) {
        return getDefaultStack(FluidContainerItem.DEFAULT, level);
    }

    public ItemStack getDefaultStack(FluidContainerItem container, int level) {
        return container.setLevel(container.getDefaultStack(this), level);
    }

    public void getDefaultStacks(FluidContainerItem container, Consumer<ItemStack> consumer) {
        consumer.accept(getDefaultStack(container));
    }

    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey());
    }

    private static final NbtCompound EMPTY_NBT = new NbtCompound();

    protected NbtCompound getFluidTag(ItemStack stack, boolean readOnly) {
        if (!readOnly) {
            return stack.getOrCreateSubNbt("fluid");
        }

        if (stack.hasNbt() && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)) {
            return stack.getSubNbt("fluid");
        }
        return EMPTY_NBT;
    }

    public static SimpleFluid byId(Identifier id) {
        if (id == null) {
            return PSFluids.EMPTY;
        }
        return REGISTRY.getOrEmpty(id).orElseGet(() -> {
            return VANILLA_FLUIDS.computeIfAbsent(id, i -> {
                return Registries.FLUID.getOrEmpty(i)
                        .map(SimpleFluid::toStill)
                        .map(fluid -> new SimpleFluid(i, 0, fluid))
                        .orElse(PSFluids.EMPTY);
            });
        });
    }

    public static SimpleFluid forVanilla(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return PSFluids.EMPTY;
        }
        Fluid f = toStill(fluid);
        Identifier id = Registries.FLUID.getId(fluid);
        return VANILLA_FLUIDS.computeIfAbsent(id, i -> new SimpleFluid(i, 0, f));
    }

    private static Fluid toStill(Fluid fluid) {
        return fluid instanceof FlowableFluid ? ((FlowableFluid)fluid).getStill() : fluid;
    }

    public static Iterable<SimpleFluid> all() {
        return REGISTRY;
    }

    public static class Settings {
        private int color;
        private boolean translucent;

        public Settings color(int color) {
            this.color = color;
            return this;
        }

        public Settings translucent() {
            translucent = true;
            return this;
        }
    }
}
