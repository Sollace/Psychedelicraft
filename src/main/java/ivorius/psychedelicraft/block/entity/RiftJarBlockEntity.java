/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;

import ivorius.psychedelicraft.client.render.bezier.Bezier;
import ivorius.psychedelicraft.entity.RealityRiftEntity;
import ivorius.psychedelicraft.entity.PSEntities;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World.ExplosionSourceType;

public class RiftJarBlockEntity extends SyncedBlockEntity {
    public float currentRiftFraction;
    public int ticksAliveVisual;

    public boolean isOpening;
    public float fractionOpen;

    public boolean jarBroken = false;
    public boolean suckingRifts = true;
    public float fractionHandleUp;

    private final Map<UUID, JarRiftConnection> riftConnections = new HashMap<>();

    public RiftJarBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.RIFT_JAR, pos, state);
    }

    public Collection<JarRiftConnection> getConnections() {
        return riftConnections.values();
    }

    public void tickAnimation() {
        fractionOpen = MathUtils.nearValue(fractionOpen, isOpening ? 1 : 0, 0, 0.02F);
        fractionHandleUp = MathUtils.nearValue(fractionHandleUp, isSuckingRifts() ? 0 : 1, 0, 0.04F);
        ticksAliveVisual++;
    }

    public void tick(ServerWorld world) {
        tickAnimation();

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
                List<RealityRiftEntity> rifts = getAffectedRifts();

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
                    drugProperties.addToDrug(DrugType.ZERO, effect * 5);
                    drugProperties.addToDrug(DrugType.POWER, effect * 35);
                });

                currentRiftFraction -= minus;
            }
        }

        riftConnections.values().removeIf(connection -> (connection.fractionUp -= 0.01F) <= 0);

        if (currentRiftFraction > 1) {
            jarBroken = true;

            releaseRift();
            world.breakBlock(pos, false);
            Vec3d explosionPosition = getPos().toCenterPos();
            world.createExplosion(null, explosionPosition.x, explosionPosition.y, explosionPosition.z, 1, false, ExplosionSourceType.BLOCK);
        }
    }

    public JarRiftConnection createAndGetRiftConnection(RealityRiftEntity rift) {
        return riftConnections.computeIfAbsent(rift.getUuid(), id -> new JarRiftConnection(rift));
    }

    public boolean toggleRiftJarOpen() {
        if (!world.isClient) {
            isOpening = !isOpening;

            markDirty();
            ((ServerWorld)world).getChunkManager().markForUpdate(getPos());
        }
        return isOpening;
    }

    public void toggleSuckingRifts() {
        if (!world.isClient) {
            suckingRifts = !suckingRifts;

            markDirty();
            ((ServerWorld)world).getChunkManager().markForUpdate(getPos());
        }
    }

    public boolean isSuckingRifts() {
        return suckingRifts;
    }

    public void releaseRift() {
        if (currentRiftFraction > 0) {
            List<RealityRiftEntity> rifts = getAffectedRifts();

            if (rifts.size() > 0) {
                rifts.get(0).addToRift(currentRiftFraction);
            } else if (!world.isClient) {
                RealityRiftEntity rift = PSEntities.REALITY_RIFT.create(world);
                rift.setPosition(getPos().toCenterPos().add(5, 3, 0.5));
                rift.setRiftSize(currentRiftFraction);
                world.spawnEntity(rift);
            }

            currentRiftFraction = 0.0f;
        }
    }

    public List<RealityRiftEntity> getAffectedRifts() {
        BlockPos pos = getPos();
        return world.getEntitiesByClass(RealityRiftEntity.class, new Box(
                pos.getX() - 2.0f, pos.getY() + 0.0f, pos.getZ() - 2.0f,
                pos.getX() + 3.0f, pos.getY() + 10, pos.getZ() + 3
            ), EntityPredicates.VALID_ENTITY
        );
    }

    @Override
    public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
        compound.putFloat("currentRiftFraction", currentRiftFraction);
        compound.putBoolean("isOpening", isOpening);
        compound.putFloat("fractionOpen", fractionOpen);
        compound.putBoolean("jarBroken", jarBroken);
        compound.putBoolean("suckingRifts", suckingRifts);
        compound.putFloat("fractionHandleUp", fractionHandleUp);
    }

    @Override
    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        currentRiftFraction = compound.getFloat("currentRiftFraction");
        isOpening = compound.getBoolean("isOpening");
        fractionOpen = compound.getFloat("fractionOpen");
        jarBroken = compound.getBoolean("jarBroken");
        suckingRifts = compound.getBoolean("suckingRifts");
        fractionHandleUp = compound.getFloat("fractionHandleUp");
    }

    public static class JarRiftConnection {
        public final UUID riftID;
        public final Vec3d position;

        public Bezier bezier;
        public float fractionUp;

        public JarRiftConnection(RealityRiftEntity rift) {
            riftID = rift.getUuid();
            position = rift.getEyePos();
        }
    }
}
