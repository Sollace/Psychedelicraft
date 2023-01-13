package ivorius.psychedelicraft.network;

import com.sollace.fabwork.api.packets.HandledPacket;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public record MsgDrugProperties (
        int entityId,
        NbtCompound compound
    ) implements HandledPacket<PlayerEntity> {

    public MsgDrugProperties(DrugProperties properties) {
        this(properties.asEntity().getId(), properties.toNbt());
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
        DrugProperties.of(sender.world.getEntityById(entityId)).ifPresent(e -> e.fromNbt(compound));
    }
}
