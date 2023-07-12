package ivorius.psychedelicraft.network;

import java.util.Optional;

import com.sollace.fabwork.api.packets.HandledPacket;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.hallucination.AbstractEntityHallucination;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
        DrugProperties.of(sender.world.getEntityById(entityId)).ifPresent(properties -> {
            if (properties.getHallucinations().getEntities().addHallucination(type, true) instanceof AbstractEntityHallucination e) {
                position.map(Vec3d::ofCenter).ifPresent(e.getEntity()::setPosition);
            }
        });
    }
}
