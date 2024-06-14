package ivorius.psychedelicraft.network;

import com.sollace.fabwork.api.packets.HandledPacket;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public record MsgDrugProperties (
        int entityId,
        NbtCompound compound
    ) implements HandledPacket<PlayerEntity> {

    public MsgDrugProperties(DrugProperties properties, WrapperLookup lookup) {
        this(properties.asEntity().getId(), properties.toNbt(lookup));
    }

    public MsgDrugProperties(PacketByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readNbt());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeNbt(compound);
    }

    @Override
    public void handle(PlayerEntity sender) {
        DrugProperties.of(sender.getWorld().getEntityById(entityId)).ifPresent(e -> e.fromNbt(compound, sender.getRegistryManager()));
    }
}
