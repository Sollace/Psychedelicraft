/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.client.render.bezier.Bezier;
import ivorius.psychedelicraft.entity.RealityRiftEntity;
import ivorius.psychedelicraft.entity.PSEntities;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.*;
import net.minecraft.world.World.ExplosionSourceType;

public class RiftJarBlockEntity extends SyncedBlockEntity {
    private static final Codec<Map<UUID, JarRiftConnection>> CONNECTIONS_CODEC = Codec.unboundedMap(Uuids.CODEC, JarRiftConnection.CODEC);
    private static final float MIN_FLOW_RATE = 0.0004F;
    private static final float EFFECT_DISTANCE_FACTOR = 0.2F;
    private static final int HOR_RANGE = 6;
    private static final int VER_RANGE = 20;
    private static final Vec3d ESCAPED_RIFT_SPAWN_OFFSET = new Vec3d(0.5, 3, 0.5);
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
        if (riftConnections.values().removeIf(JarRiftConnection::tick) && world instanceof ServerWorld) {
            markDirty();
        }
    }

    public boolean isOpened() {
        return fractionOpen > 0;
    }

    public boolean isSuckingRifts() {
        return suckingRifts;
    }

    public List<RealityRiftEntity> getAffectedRifts(ServerWorld world) {
        Vec3d pos = getPos().toBottomCenterPos();
        return world.getEntitiesByClass(RealityRiftEntity.class, Box.of(pos, HOR_RANGE, VER_RANGE, HOR_RANGE).withMinY(pos.getY()), EntityPredicates.VALID_ENTITY);
    }

    public JarRiftConnection createAndGetRiftConnection(RealityRiftEntity rift) {
        return riftConnections.computeIfAbsent(rift.getUuid(), id -> new JarRiftConnection(rift.getEyePos(), 0));
    }

    public boolean toggleRiftJarOpen() {
        if (world instanceof ServerWorld) {
            isOpening = !isOpening;

            markDirty();
        }
        return isOpening;
    }

    public void toggleSuckingRifts() {
        if (world instanceof ServerWorld) {
            suckingRifts = !suckingRifts;

            markDirty();
        }
    }

    public void tick(ServerWorld world) {
        tickAnimation();

        if (isOpened()) {
            if (isSuckingRifts()) {
                List<RealityRiftEntity> rifts = getAffectedRifts(world);

                if (rifts.size() > 0) {
                    float minus = (1F / rifts.size()) * 0.001f * fractionOpen;
                    rifts.forEach(rift -> {
                        currentRiftFraction += rift.takeFromRift(minus);

                        createAndGetRiftConnection(rift).addEffect(0.02f * fractionOpen);
                        markDirty();
                    });
                }
            } else {
                if (currentRiftFraction > 0) {
                    leakZeroMatter(world);
                }
            }
        }

        if (currentRiftFraction > 1
                || (currentRiftFraction > 0 && !world.getEntitiesByClass(ProjectileEntity.class, Box.of(pos.toBottomCenterPos(), 1.1, 1.1, 1.1).withMinY(pos.getY()), EntityPredicates.VALID_ENTITY).isEmpty())) {
            explode(world);
        }
    }

    private void leakZeroMatter(ServerWorld world) {
        float minus = Math.min(MIN_FLOW_RATE + MIN_FLOW_RATE * fractionOpen * currentRiftFraction, currentRiftFraction);

        BlockPos pos = getPos();
        Vec3d center = pos.toCenterPos();
        world.getEntitiesByClass(LivingEntity.class, new Box(
                pos.getX() - 5, pos.getY() - 5, pos.getZ() - 2,
                pos.getX() + 6, pos.getY() + 6, pos.getZ() + 6
            ), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR
        ).stream().flatMap(DrugProperties::stream).forEach(drugProperties -> {
            double effect = (5 - drugProperties.asEntity().getEntityPos().distanceTo(center)) * EFFECT_DISTANCE_FACTOR * minus;
            drugProperties.addToDrug(DrugType.ZERO, effect * 5);
            drugProperties.addToDrug(DrugType.POWER, effect * 35);
        });

        List<RealityRiftEntity> rifts = getAffectedRifts(world);
        if (!rifts.isEmpty()) {
            rifts.forEach(rift -> rift.addToRift(minus / rifts.size()));
        }

        currentRiftFraction -= minus;
        markDirty();
    }

    private void releaseRift(ServerWorld world) {
        if (currentRiftFraction > 0) {
            getAffectedRifts(world).stream().findFirst().ifPresentOrElse(rift -> rift.addToRift(currentRiftFraction), () -> {
                RealityRiftEntity rift = PSEntities.REALITY_RIFT.create(world, SpawnReason.EVENT);
                rift.setPosition(getPos().toCenterPos().add(ESCAPED_RIFT_SPAWN_OFFSET));
                rift.setRiftSize(currentRiftFraction);
                world.spawnEntity(rift);
            });

            currentRiftFraction = 0;
            markDirty();
        }
    }

    public void explode(ServerWorld world) {
        jarBroken = true;
        releaseRift(world);
        world.breakBlock(pos, false);
        Vec3d explosionPosition = getPos().toCenterPos();
        world.createExplosion(null, explosionPosition.x, explosionPosition.y, explosionPosition.z, 1, false, ExplosionSourceType.BLOCK);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putFloat("currentRiftFraction", currentRiftFraction);
        view.putBoolean("isOpening", isOpening);
        view.putFloat("fractionOpen", fractionOpen);
        view.putBoolean("jarBroken", jarBroken);
        view.putBoolean("suckingRifts", suckingRifts);
        view.putFloat("fractionHandleUp", fractionHandleUp);
        view.put("connections", CONNECTIONS_CODEC, riftConnections);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        currentRiftFraction = view.getFloat("currentRiftFraction", 0);
        isOpening = view.getBoolean("isOpening", false);
        fractionOpen = view.getFloat("fractionOpen", 0);
        jarBroken = view.getBoolean("jarBroken", false);
        suckingRifts = view.getBoolean("suckingRifts", false);
        fractionHandleUp = view.getFloat("fractionHandleUp", 0);

        view.read("connections", CONNECTIONS_CODEC).ifPresent(connections -> {
            connections.forEach((key, connection) -> {
                riftConnections.compute(key, (uuid, existingConnection) -> {
                    if (existingConnection != null) {
                        return existingConnection.copyFrom(connection);
                    }

                    return connection;
                });
            });
        });
    }

    public static class JarRiftConnection {
        public static final Codec<JarRiftConnection> CODEC = RecordCodecBuilder.create(i -> i.group(
                Vec3d.CODEC.fieldOf("position").forGetter(o -> o.position),
                Codec.FLOAT.fieldOf("fractionUp").forGetter(o -> o.fractionUp)
        ).apply(i, JarRiftConnection::new));
        public Vec3d position;

        @Nullable
        public Bezier bezier;
        public float fractionUp;

        public JarRiftConnection(Vec3d position, float fractionUp) {
            this.position = position;
            this.fractionUp = fractionUp;
        }

        public boolean tick() {
            fractionUp -= 0.01F;
            return fractionUp <= 0;
        }

        public void addEffect(float effect) {
            fractionUp = Math.min(1, fractionUp + effect);
        }

        public JarRiftConnection copyFrom(JarRiftConnection other) {
            position = other.position;
            fractionUp = other.fractionUp;
            bezier = other.bezier;
            return this;
        }
    }
}
