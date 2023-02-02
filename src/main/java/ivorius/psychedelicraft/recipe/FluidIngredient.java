package ivorius.psychedelicraft.recipe;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import ivorius.psychedelicraft.fluid.FluidContainer;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public record FluidIngredient (SimpleFluid fluid, int level, NbtCompound attributes) {
    public static FluidIngredient fromJson(JsonObject json) {
        SimpleFluid fluid = SimpleFluid.byId(Identifier.tryParse(JsonHelper.getString(json, "fluid")));
        int level = JsonHelper.getInt(json, "level", -1);

        NbtCompound nbt = new NbtCompound();

        if (json.has("attributes")) {
            try {
                nbt = NbtHelper.fromNbtProviderString(json.get("attributes").toString());
            } catch (CommandSyntaxException ignored) {}
        }
        return new FluidIngredient(fluid, level, nbt);
    }

    public FluidIngredient(PacketByteBuf buffer) {
        this(SimpleFluid.byId(buffer.readIdentifier()), buffer.readVarInt(), buffer.readNbt());
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(fluid.getId());
        buffer.writeVarInt(level);
        buffer.writeNbt(attributes);
    }

    public boolean test(ItemStack stack) {
        if (!(stack.getItem() instanceof FluidContainer)) {
            return false;
        }
        boolean result = true;
        result &= fluid.isEmpty() || FluidContainer.of(stack).getFluid(stack) == fluid;
        result &= !stack.hasNbt() || attributes.isEmpty() || (stack.getNbt().contains("fluid") && NbtHelper.matches(attributes, stack.getSubNbt("fluid"), true));
        result &= level <= 0 || FluidContainer.of(stack).getLevel(stack) >= level;
        return result;
    }
}

