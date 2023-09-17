package ivorius.psychedelicraft.recipe;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;

public record FluidIngredient (SimpleFluid fluid, int level, NbtCompound attributes) {
    @SuppressWarnings("deprecation")
    public static final Codec<FluidIngredient> CODEC = Codecs.fromJsonSerializer(element -> {
        if (element.isJsonObject()) {
            JsonObject json = element.getAsJsonObject();
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

        SimpleFluid fluid = SimpleFluid.byId(Identifier.tryParse(JsonHelper.asString(element, "fluid")));

        return new FluidIngredient(fluid, -1, new NbtCompound());
    }, ingredient -> {
        throw new NotImplementedException("Cannot serialize a fluid ingredient!!");
    });

    public FluidIngredient(PacketByteBuf buffer) {
        this(SimpleFluid.byId(buffer.readIdentifier()), buffer.readVarInt(), buffer.readNbt());
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(fluid.getId());
        buffer.writeVarInt(level);
        buffer.writeNbt(attributes);
    }

    public boolean test(Resovoir tank) {
        return test(tank.getContents());
    }

    public boolean test(MutableFluidContainer container) {
        boolean result = true;
        result &= fluid.isEmpty() || container.getFluid() == fluid;
        result &= attributes.isEmpty() || NbtHelper.matches(attributes, container.getAttributes(), true);
        result &= level <= 0 || container.getLevel() >= level;
        return result;
    }

    public boolean test(ItemStack stack) {
        return test(MutableFluidContainer.of(stack));
    }
}

