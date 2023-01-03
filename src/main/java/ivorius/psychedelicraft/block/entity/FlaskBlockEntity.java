/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */
package ivorius.psychedelicraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import ivorius.psychedelicraft.fluids.FluidHelper;
import ivorius.psychedelicraft.fluids.Resovoir;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 2 Jan 2023
 */
public class FlaskBlockEntity extends BlockEntity implements Resovoir.ChangeListener {
    public static final int FLASK_CAPACITY = FluidHelper.MILLIBUCKETS_PER_LITER * 8;

    private final Resovoir tank;
    private boolean pendingSync;

    public FlaskBlockEntity(BlockPos pos, BlockState state) {
        this(PSBlockEntities.FLASK, pos, state, FLASK_CAPACITY);
    }

    public FlaskBlockEntity(BlockEntityType<? extends FlaskBlockEntity> type, BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state);
        tank = new Resovoir(capacity, this);
    }

    @Override
    public void onDrain(Resovoir resovoir) {
        onIdle(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        onIdle(resovoir);
    }

    @Override
    public void onIdle(Resovoir resovoir) {
        this.markDirty();
        pendingSync = true;
    }

    public Resovoir getTank(Direction direction) {
        return tank;
    }

    public void tick(ServerWorld world) {
        if (pendingSync) {
            pendingSync = false;
            world.getChunkManager().markForUpdate(getPos());
        }
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.put("tank", tank.toNbt());
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        tank.fromNbt(compound.getCompound("tank"));
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
