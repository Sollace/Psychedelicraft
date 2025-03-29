/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;

import ivorius.psychedelicraft.ParticleHelper;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.*;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

/**
 * Created by lukas on 03.03.14.
 */
public class RealityRiftEntity extends Entity {
    public static final float CLOSING_DECAY_RATE = 0.05F;
    public static final float RIFT_DECAY_RATE = CLOSING_DECAY_RATE / 1200F;
    public static final float ANIMATION_CHANGE_RATE = CLOSING_DECAY_RATE / 10F;
    public static final float RIFT_COLLAPSE_THRESHOLD = 0.9F;
    public static final float CRITICAL_RIFT_BLEED_AMOUNT = 0.2F;
    public static final double AFFECT_PER_BLOCK = 0.0005;
    private static final TrackedData<Float> SIZE = DataTracker.registerData(RealityRiftEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> INSTABILITY = DataTracker.registerData(RealityRiftEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> CLOSING = DataTracker.registerData(RealityRiftEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private float prevVvisualRiftSize;
    private float visualRiftSize;

    public static void spawn(Entity entity) {
        RealityRiftEntity rift = PSEntities.REALITY_RIFT.create(entity.getWorld(), SpawnReason.NATURAL);
        rift.setPosition(
                entity.getX() + (entity.getWorld().getRandom().nextDouble() - 0.5) * 100,
                entity.getY() + (entity.getWorld().getRandom().nextDouble() - 0.5) * 100,
                entity.getZ() + (entity.getWorld().getRandom().nextDouble() - 0.5) * 100
        );
        entity.getWorld().spawnEntity(rift);
    }

    RealityRiftEntity(EntityType<RealityRiftEntity> type, World par1World) {
        super(type, par1World);
        setRiftSize(getWorld().getRandom().nextTriangular(0.5F, 0.5F));
    }

    @Override
    protected void initDataTracker(Builder builder) {
        builder.add(SIZE, 0F).add(CLOSING, false).add(INSTABILITY, 0F);
    }

    public float getRiftSize() {
        return getDataTracker().get(SIZE);
    }

    public float getRiftSize(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevVvisualRiftSize, visualRiftSize);
    }

    public void setRiftSize(float size) {
        getDataTracker().set(SIZE, Math.max(0, size));
    }

    public void addToRift(float size) {
        setRiftSize(getRiftSize() + size);
    }

    public float takeFromRift(float size) {
        if (isCritical()) {
            return CRITICAL_RIFT_BLEED_AMOUNT;
        }

        float riftSize = getRiftSize();
        float newVal = Math.max(riftSize - size, 0);

        setRiftSize(newVal);

        return riftSize - newVal;
    }

    public float getInstability() {
        return getDataTracker().get(INSTABILITY);
    }

    public void setInstability(float instability) {
        getDataTracker().set(INSTABILITY, Math.max(0, instability));
    }

    public boolean isRiftClosing() {
        return getDataTracker().get(CLOSING);
    }

    public void setRiftClosing(boolean closing) {
        getDataTracker().set(CLOSING, closing);
    }

    public boolean isCritical() {
        return ((getInstability() > 0) || (getRiftSize() > 3)) && !isRiftClosing();
    }


    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    public Text getDisplayName() {
        return super.getDisplayName().copy().formatted(Formatting.OBFUSCATED);
    }

    @Override
    public void tick() {
        super.tick();
        setVelocity(Vec3d.ZERO);

        if (getWorld() instanceof ServerWorld sw) {
            if (Psychedelicraft.getConfig().randomTicksUntilRiftSpawn.get() == 0) {
                kill(sw);
                return;
            }

            emitEffects(sw);

            if (isCritical()) {
                spreadCorruption();
            }

            if (isRiftClosing()) {
                setRiftSize(getRiftSize() - CLOSING_DECAY_RATE);
            } else if (!isCritical()) {
                setRiftSize(getRiftSize() - RIFT_DECAY_RATE);
            }

            if (getInstability() >= RIFT_COLLAPSE_THRESHOLD) {
                setRiftClosing(true);
            }

            if (visualRiftSize <= 0 && getRiftSize() <= 0) {
                remove(Entity.RemovalReason.KILLED);
            }
        } else {
            Vec3d pos = getPos();
            Supplier<Vec3d> particlePositionSupplier = () -> {
                float distance = random.nextFloat() * random.nextFloat();
                return ParticleHelper.apply(pos, x -> x + (random.nextFloat() * 8 - 4) * distance).add(0, getHeight() / 2F, 0);
            };
            ParticleHelper.spawnParticles(getWorld(), ParticleTypes.LARGE_SMOKE, particlePositionSupplier, Suppliers.ofInstance(Vec3d.ZERO), random.nextInt(3));
            ParticleHelper.spawnParticles(getWorld(), new DustParticleEffect(ColorHelper.fromFloats(1, 1, 0.5F, 0.5F), 1), particlePositionSupplier, Suppliers.ofInstance(new Vec3d(-10, -10, -10)), random.nextInt(2));
            ParticleHelper.spawnParticles(getWorld(), ParticleTypes.ENCHANT, Suppliers.ofInstance(pos.add(0, 1 + (getHeight() / 2F), 0)), () -> {
                float distance = random.nextFloat() * random.nextFloat();
                return ParticleHelper.apply(Vec3d.ZERO, x -> x + (random.nextFloat() * 8 - 4) * distance).add(0, getHeight() / 2F, 0);
            }, 1);
        }

        prevVvisualRiftSize = visualRiftSize;
        visualRiftSize = MathUtils.nearValue(visualRiftSize, getRiftSize(), CLOSING_DECAY_RATE, ANIMATION_CHANGE_RATE);
    }

    private void emitEffects(ServerWorld world) {
        float searchDistance = 5 + getInstability() * 50;
        boolean critical = isCritical();
        for (LivingEntity entity : getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(searchDistance), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
            double dist = entity.distanceTo(this);
            double effect = (searchDistance - dist) * AFFECT_PER_BLOCK * getRiftSize();

            if (effect > 0) {
                DrugProperties.of(entity).ifPresentOrElse(drugProperties -> {
                    drugProperties.addToDrug(DrugType.ZERO, effect * 20);
                    drugProperties.addToDrug(DrugType.POWER, effect * 200);
                }, () -> {
                    if (critical) {
                        entity.damage(world, getDamageSources().magic(), (float)effect * 20);
                    }
                });
            }
        }
    }

    private void spreadCorruption() {
        float prevS = getInstability();
        float newS = Math.min(prevS + 0.001f, 1);
        setInstability(newS);

        float prevDesRange = prevS * 50;
        float newDesRange = newS * 50;

        if (prevDesRange < newDesRange) {
            int desRange = MathHelper.ceil(newDesRange);
            BlockPos center = getBlockPos();
            BlockPos.iterateOutwards(center, desRange, desRange, desRange).forEach(p -> {
                if (p.isWithinDistance(center, newDesRange) && !p.isWithinDistance(center, prevDesRange) && !getWorld().isAir(p)) {
                    getWorld().setBlockState(p, PSBlocks.GLITCH.getDefaultState());
                }
            });
        }
    }

    @Override
    public void kill(ServerWorld world) {
        setRiftClosing(true);
        emitGameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        if (compound.contains("riftSize", NbtElement.FLOAT_TYPE)) {
            setRiftSize(compound.getFloat("riftSize"));
        }
        if (compound.contains("isRiftClosing")) {
            setRiftClosing(compound.getBoolean("isRiftClosing"));
        }
        if (compound.contains("instability", NbtElement.FLOAT_TYPE)) {
            setInstability(compound.getFloat("instability"));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        compound.putFloat("riftSize", getRiftSize());
        compound.putBoolean("isRiftClosing", isRiftClosing());
        compound.putFloat("instability", getInstability());
    }
}
