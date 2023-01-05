package ivorius.psychedelicraft.crafting;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import ivorius.psychedelicraft.fluids.SimpleFluid;
import ivorius.psychedelicraft.items.FluidContainerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

record FluidIngredient (SimpleFluid fluid, int level, NbtCompound attributes) {
    public static FluidIngredient fromJson(JsonObject json) {
        SimpleFluid fluid = SimpleFluid.REGISTRY.getOrEmpty(Identifier.tryParse(JsonHelper.getString(json, "fluid"))).orElseThrow();
        int level = JsonHelper.getInt(json, "level");

        NbtCompound nbt = new NbtCompound();

        if (json.has("attributes")) {
            try {
                nbt = NbtHelper.fromNbtProviderString(json.get("attributes").toString());
            } catch (CommandSyntaxException ignored) {}
        }
        return new FluidIngredient(fluid, level, nbt);
    }

    public FluidIngredient(PacketByteBuf buffer) {
        this(buffer.readRegistryValue(SimpleFluid.REGISTRY), buffer.readVarInt(), buffer.readNbt());
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeRegistryValue(SimpleFluid.REGISTRY, fluid);
        buffer.writeVarInt(level);
        buffer.writeNbt(attributes);
    }

    public boolean test(ItemStack stack) {
        if (!(stack.getItem() instanceof FluidContainerItem)) {
            return false;
        }
        boolean result = true;
        result &= fluid.isEmpty() || FluidContainerItem.of(stack).getFluid(stack) == fluid;
        result &= !stack.hasNbt() || attributes.isEmpty() || (stack.getNbt().contains("fluid") && NbtHelper.matches(attributes, stack.getSubNbt("fluid"), true));
        return result;
    }
}

