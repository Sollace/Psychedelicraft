/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.Psychedelicraft;
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

    public int getColor(ItemStack stack) {
        return color;
    }

    public boolean isTranslucent() {
        return translucent;
    }

    protected String getTranslationKey() {
        return Util.createTranslationKey("fluid", id);
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
            return this;
        }

        public Settings translucent() {
            translucent = true;
            return this;
        }
    }
}
