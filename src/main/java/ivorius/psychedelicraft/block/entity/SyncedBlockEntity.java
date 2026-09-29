package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.util.NbtViews;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

public abstract class SyncedBlockEntity extends BlockEntity {

    protected SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public final Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public final NbtCompound toInitialChunkDataNbt(WrapperLookup lookup) {
        return createNbt(lookup);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        readNbt(NbtViews.read(view), view.getRegistries());
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        NbtCompound compound = new NbtCompound();
        writeNbt(compound, NbtViews.lookup(world));
        NbtViews.write(view, compound);
    }

    protected void readNbt(NbtCompound compound, WrapperLookup lookup) { }

    protected void writeNbt(NbtCompound compound, WrapperLookup lookup) { }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(getPos());
        }
    }
}
