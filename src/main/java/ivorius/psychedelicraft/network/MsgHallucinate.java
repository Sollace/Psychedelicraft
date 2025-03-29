package ivorius.psychedelicraft.network;

import java.util.Optional;

import com.sollace.fabwork.api.packets.Handled;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.hallucination.AbstractEntityHallucination;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record MsgHallucinate (
        int entityId,
        Identifier type,
        Optional<BlockPos> position
    ) implements Handled<PlayerEntity> {
    public static final PacketCodec<RegistryByteBuf, MsgHallucinate> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, MsgHallucinate::entityId,
            Identifier.PACKET_CODEC, MsgHallucinate::type,
            PacketCodecs.optional(BlockPos.PACKET_CODEC), MsgHallucinate::position,
            MsgHallucinate::new
    );

    @Override
    public void handle(PlayerEntity sender) {
        DrugProperties.of(sender.getWorld().getEntityById(entityId)).ifPresent(properties -> {
            if (properties.getHallucinations().getEntities().addHallucination(type, true) instanceof AbstractEntityHallucination e) {
                position.map(BlockPos::toCenterPos).ifPresent(e.getEntity()::setPosition);
            }
        });
    }
}
