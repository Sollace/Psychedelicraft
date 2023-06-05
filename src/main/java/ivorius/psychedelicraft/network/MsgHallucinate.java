package ivorius.psychedelicraft.network;

import java.util.Optional;

import com.sollace.fabwork.api.packets.HandledPacket;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.hallucination.AbstractEntityHallucination;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record MsgHallucinate (
        int entityId,
        Identifier type,
        Optional<BlockPos> position
    ) implements HandledPacket<PlayerEntity> {

    public MsgHallucinate(PacketByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readIdentifier(), buffer.readOptional(PacketByteBuf::readBlockPos));
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeIdentifier(type);
        buffer.writeOptional(position, PacketByteBuf::writeBlockPos);
    }

    @Override
    public void handle(PlayerEntity sender) {
        DrugProperties.of(sender.getWorld().getEntityById(entityId)).ifPresent(properties -> {
            if (properties.getHallucinations().getEntities().addHallucination(type, true) instanceof AbstractEntityHallucination e) {
                position.map(BlockPos::toCenterPos).ifPresent(e.getEntity()::setPosition);
            }
        });
    }
}
