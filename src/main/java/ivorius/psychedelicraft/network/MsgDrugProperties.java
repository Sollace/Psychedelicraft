package ivorius.psychedelicraft.network;

import com.sollace.fabwork.api.packets.Handled;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public record MsgDrugProperties (
        int entityId,
        NbtCompound compound
    ) implements Handled<PlayerEntity> {
    public static final PacketCodec<RegistryByteBuf, MsgDrugProperties> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, MsgDrugProperties::entityId,
            PacketCodecs.NBT_COMPOUND, MsgDrugProperties::compound,
            MsgDrugProperties::new
    );

    public MsgDrugProperties(DrugProperties properties, WrapperLookup lookup) {
        this(properties.asEntity().getId(), properties.toNbt(lookup));
    }

    @Override
    public void handle(PlayerEntity sender) {
        DrugProperties.of(sender.getWorld().getEntityById(entityId)).ifPresent(e -> e.fromNbt(compound, sender.getRegistryManager()));
    }
}
