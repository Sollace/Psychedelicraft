package ivorius.psychedelicraft.network;

import com.sollace.fabwork.api.packets.HandledPacket;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record MsgHallucinate (
        int entityId,
        Identifier type
    ) implements HandledPacket<PlayerEntity> {

    public MsgHallucinate(PacketByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readIdentifier());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeIdentifier(type);
    }

    @Override
    public void handle(PlayerEntity sender) {
        DrugProperties.of(sender.world.getEntityById(entityId)).ifPresent(properties -> {
            properties.getHallucinations().getEntities().addHallucination(type, true);
        });
    }
}
