/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.item.FluidContainerItem;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * Created by lukas on 29.10.14.
 */
public class SimpleFluid {
    public static final Identifier EMPTY_KEY = Psychedelicraft.id("empty");
    public static final Registry<SimpleFluid> REGISTRY = FabricRegistryBuilder.createDefaulted(SimpleFluid.class, Psychedelicraft.id("fluids"), EMPTY_KEY).buildAndRegister();
    public static final SimpleFluid EMPTY = new SimpleFluid(EMPTY_KEY, new Settings().color(0xFFFFFFFF));

    protected final Identifier id;

    private int color;
    private boolean translucent;

    public SimpleFluid(Identifier id, Settings settings) {
        this.id = id;
        this.color = settings.color;
        this.translucent = settings.translucent;
        Registry.register(REGISTRY, id, this);
    }

    public final boolean isEmpty() {
        return this == EMPTY;
    }

    public final Identifier getId() {
        return id;
    }

    public final Identifier getSymbol() {
        Identifier id = getId();
        return new Identifier(id.getNamespace(), "textures/fluids/" + id.getPath() + ".png");
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
        return Util.createTranslationKey("fluid", id);
    }

    public final ItemStack getDefaultStack(FluidContainerItem container) {
        return getDefaultStack(container, container.getMaxCapacity());
    }

    public final ItemStack getDefaultStack() {
        return getDefaultStack(FluidContainerItem.FLUID);
    }

    public final ItemStack getDefaultStack(int level) {
        return getDefaultStack(FluidContainerItem.FLUID, level);
    }

    public ItemStack getDefaultStack(FluidContainerItem container, int level) {
        return container.setLevel(container.getDefaultStack(this), level);
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
