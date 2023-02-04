/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.*;
import java.util.function.Consumer;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.fluid.*;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
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

    private final Fluid standing;
    private final Fluid flowing;

    public SimpleFluid(Identifier id, Settings settings) {
        this.id = id;
        this.color = settings.color;
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.stationaryTexture = id.withPath(p -> "textures/block/fluid/" + p + "_still.png");
        this.flowingTexture = id.withPath(p -> "textures/block/fluid/" + p + "_flow.png");

        Registry.register(REGISTRY, id, this);
        standing = Registry.register(Registries.FLUID, id, new WaterFluid.Still());
        flowing = Registry.register(Registries.FLUID, id.withPath(p -> "flowing_" + p), new WaterFluid.Flowing());
    }

    public SimpleFluid(Identifier id, int color, Fluid standing) {
        this.id = id;
        this.color = color;
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.stationaryTexture = id.withPath(p -> "textures/block/" + p + "_still.png");
        this.flowingTexture = id.withPath(p -> "textures/block/" + p + "_flow.png");
        this.standing = standing;
        this.flowing = null;
    }

    public final boolean isEmpty() {
        return this == PSFluids.EMPTY;
    }

    public final Identifier getId() {
        return id;
    }

    public Identifier getSymbol(ItemStack stack) {
        return symbol;
    }

    public Identifier getStationaryTexture(ItemStack stack) {
        return stationaryTexture;
    }

    public Identifier getFlowingTexture(ItemStack stack) {
        return flowingTexture;
    }

    public FluidState getFluidState(int level) {
        return getStandingFluid().getDefaultState().with(FlowableFluid.LEVEL, level);
    }

    public Fluid getStandingFluid() {
        return standing;
    }

    @SuppressWarnings("deprecation")
    public boolean isIn(TagKey<Fluid> tag) {
        return getStandingFluid().isIn(tag);
    }

    public boolean isCustomFluid() {
        return flowing != null;
    }

    public int getColor(ItemStack stack) {
        return color;
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

    public static SimpleFluid byId(Identifier id) {
        if (id == null) {
            return PSFluids.EMPTY;
        }
        return REGISTRY.getOrEmpty(id).orElseGet(() -> {
            return VANILLA_FLUIDS.computeIfAbsent(id, i -> {
                return Registries.FLUID.getOrEmpty(i)
                        .map(SimpleFluid::toStill)
                        .map(fluid -> new SimpleFluid(Registries.FLUID.getId(fluid), 0xFFFFFFFF, fluid))
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

        public Settings color(int color) {
            this.color = color;
            return this;
        }
    }
}
