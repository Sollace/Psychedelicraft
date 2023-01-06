/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;

import ivorius.psychedelicraft.client.rendering.bezier.IvBezierPath3D;
import ivorius.psychedelicraft.entities.EntityRealityRift;
import ivorius.psychedelicraft.entities.PSEntityList;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World.ExplosionSourceType;

public class TileEntityRiftJar extends BlockEntity {
    public float currentRiftFraction;
    public int ticksAliveVisual;

    public boolean isOpening;
    public float fractionOpen;

    public boolean jarBroken = false;
    public boolean suckingRifts = true;
    public float fractionHandleUp;

    private final Map<UUID, JarRiftConnection> riftConnections = new HashMap<>();

    public TileEntityRiftJar(BlockPos pos, BlockState state) {
        super(PSBlockEntities.RIFT_JAR, pos, state);
    }

    public Collection<JarRiftConnection> getConnections() {
        return riftConnections.values();
    }

    public void tick(ServerWorld world) {
        fractionOpen = MathUtils.nearValue(fractionOpen, isOpening ? 1 : 0, 0, 0.02F);
        fractionHandleUp = MathUtils.nearValue(fractionHandleUp, isSuckingRifts() ? 0 : 1, 0, 0.04F);

//        if (!world.isClient)
//        {
//            boolean before = suckingRifts;
//            suckingRifts = !world.isDaytime() && world.canBlockSeeTheSky(xCoord, yCoord, zCoord);
//
//            if (before != suckingRifts)
//            {
//                markDirty();
//                world.markBlockForUpdate(xCoord, yCoord, zCoord);
//            }
//        }

        if (isSuckingRifts()) {
            if (fractionOpen > 0) {
                List<EntityRealityRift> rifts = getAffectedRifts();

                if (rifts.size() > 0) {
                    float minus = (1F / rifts.size()) * 0.001f * fractionOpen;
                    rifts.forEach(rift -> {
                        currentRiftFraction += rift.takeFromRift(minus);

                        JarRiftConnection connection = createAndGetRiftConnection(rift);
                        connection.fractionUp = Math.min(1, connection.fractionUp + 0.02f * fractionOpen);
                    });
                }
            }
        } else {
            if (fractionOpen > 0) {
                float minus = Math.min(0.0004f * fractionOpen * currentRiftFraction + 0.0004f, currentRiftFraction);

                BlockPos pos = getPos();
                Vec3d center = pos.toCenterPos();
                world.getEntitiesByClass(LivingEntity.class, new Box(
                        pos.getX() - 5, pos.getY() - 5, pos.getZ() - 2,
                        pos.getX() + 6, pos.getY() + 6, pos.getZ() + 6
                    ), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR
                ).stream().flatMap(DrugProperties::stream).forEach(drugProperties -> {
                    double effect = (5 - drugProperties.asEntity().getPos().distanceTo(center)) * 0.2F * minus;
                    drugProperties.addToDrug("Zero", effect * 5);
                    drugProperties.addToDrug("Power", effect * 35);
                });

                currentRiftFraction -= minus;
            }
        }

        riftConnections.values().removeIf(connection -> (connection.fractionUp -= 0.01F) <= 0);

        if (currentRiftFraction > 1.0f) {
            jarBroken = true;

            releaseRift();
            world.breakBlock(pos, false);
            Vec3d explosionPosition = getPos().toCenterPos();
            world.createExplosion(null, explosionPosition.x, explosionPosition.y, explosionPosition.z, 1, false, ExplosionSourceType.BLOCK);
        }

        ticksAliveVisual++;
    }

    public JarRiftConnection createAndGetRiftConnection(EntityRealityRift rift) {
        return riftConnections.computeIfAbsent(rift.getUuid(), id -> new JarRiftConnection(rift));
    }

    public void toggleRiftJarOpen() {
        if (!world.isClient) {
            isOpening = !isOpening;

            markDirty();
        }
    }

    public void toggleSuckingRifts() {
        if (!world.isClient) {
            suckingRifts = !suckingRifts;

            markDirty();
        }
    }

    public boolean isSuckingRifts() {
        return suckingRifts;
    }

    public void releaseRift() {
        if (currentRiftFraction > 0) {
            List<EntityRealityRift> rifts = getAffectedRifts();

            if (rifts.size() > 0) {
                rifts.get(0).addToRift(currentRiftFraction);
            } else if (!world.isClient) {
                EntityRealityRift rift = PSEntityList.REALITY_RIFT.create(world);
                rift.setPosition(getPos().toCenterPos().add(5, 3, 0.5));
                rift.setRiftSize(currentRiftFraction);
                world.spawnEntity(rift);
            }

            currentRiftFraction = 0.0f;
        }
    }

    public List<EntityRealityRift> getAffectedRifts() {
        BlockPos pos = getPos();
        return world.getEntitiesByClass(EntityRealityRift.class, new Box(
                pos.getX() - 2.0f, pos.getY() + 0.0f, pos.getZ() - 2.0f,
                pos.getX() + 3.0f, pos.getY() + 10, pos.getZ() + 3
            ), EntityPredicates.VALID_ENTITY
        );
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        compound.putFloat("currentRiftFraction", currentRiftFraction);
        compound.putBoolean("isOpening", isOpening);
        compound.putFloat("fractionOpen", fractionOpen);
        compound.putBoolean("jarBroken", jarBroken);
        compound.putBoolean("suckingRifts", suckingRifts);
        compound.putFloat("fractionHandleUp", fractionHandleUp);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        currentRiftFraction = compound.getFloat("currentRiftFraction");
        isOpening = compound.getBoolean("isOpening");
        fractionOpen = compound.getFloat("fractionOpen");
        jarBroken = compound.getBoolean("jarBroken");
        suckingRifts = compound.getBoolean("suckingRifts");
        fractionHandleUp = compound.getFloat("fractionHandleUp");
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public static class JarRiftConnection {
        public final UUID riftID;
        public final Vec3d position;

        public IvBezierPath3D bezierPath3D;
        public float fractionUp;

        public JarRiftConnection(EntityRealityRift rift) {
            riftID = rift.getUuid();
            position = rift.getEyePos();
        }
    }
}
