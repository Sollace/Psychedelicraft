/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug;

import ivorius.psychedelicraft.*;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.entity.*;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.entity.drug.sound.DrugMusicManager;
import ivorius.psychedelicraft.entity.effect.PSEffects;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.item.PacifierItem;
import ivorius.psychedelicraft.network.Channel;
import ivorius.psychedelicraft.network.MsgDrugProperties;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.event.GameEvent;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

public class DrugProperties implements NbtSerialisable {
    public static final Identifier DRUG_EFFECT = Psychedelicraft.id("drugs");

    private static final Codec<Map<DrugType<?>, Drug>> DRUGS_CODEC = Codec.unboundedMap(DrugType.REGISTRY.getCodec(), Drug.CODEC);

    private final Map<DrugType<?>, Drug> drugs = DrugType.REGISTRY.stream().collect(Collectors.toMap(Function.identity(), DrugType::create));
    private final List<DrugInfluence> influences = new ArrayList<>();

    private boolean dirty;

    private final HallucinationManager hallucinations = new HallucinationManager(this);
    private final DrugMusicManager soundManager = new DrugMusicManager(this);

    private int timeBreathingSmoke;
    private int breathSmokeColor = -1;

    private final PlayerEntity entity;

    private final Stomach stomach;

    private float teethGrindingRate;
    private int pacifierSqueakDelay = -1;

    public DrugProperties(PlayerEntity entity) {
        this.entity = entity;
        this.stomach = new Stomach(this);
    }

    public static DrugProperties of(PlayerEntity player) {
        return ((DrugPropertiesContainer)player).getDrugProperties();
    }

    public static Optional<DrugProperties> of(LivingEntityRenderState state) {
        return Optional.ofNullable(((DrugPropertiesContainer)state).getDrugProperties());
    }

    public static Stream<DrugProperties> stream(Entity entity) {
        if (entity instanceof DrugPropertiesContainer c) {
            return Stream.of(c.getDrugProperties());
        }
        return Stream.empty();
    }

    public static Optional<DrugProperties> of(Entity entity) {
        if (entity instanceof DrugPropertiesContainer c) {
            return Optional.of(c.getDrugProperties());
        }
        return Optional.empty();
    }

    public PlayerEntity asEntity() {
        return entity;
    }

    public DamageSource damageOf(RegistryKey<DamageType> type) {
        return PSDamageTypes.create(entity.getWorld(), type);
    }

    public Stomach getStomach() {
        return stomach;
    }

    public void markDirty() {
        dirty = true;
    }

    public HallucinationManager getHallucinations() {
        return hallucinations;
    }

    public DrugMusicManager getMusicManager() {
        return soundManager;
    }

    @SuppressWarnings("unchecked")
    public <T extends Drug> T getDrug(DrugType<T> type) {
        return (T)drugs.computeIfAbsent(type, DrugType::create);
    }

    public float getDrugValue(DrugType<?> type) {
        return (float)getDrug(type).getActiveValue();
    }

    public boolean isDrugActive(DrugType<?> type) {
        return getDrugValue(type) > MathHelper.EPSILON;
    }

    public boolean isTripping() {
        float f = getModifier(Drug.MOVEMENT_HALLUCINATION_STRENGTH)
                + getModifier(Drug.CONTEXTUAL_HALLUCINATION_STRENGTH)
                + getModifier(Drug.COLOR_HALLUCINATION_STRENGTH);
        return f > 0.7F;
    }

    public void addToDrug(DrugType<?> type, double effect) {
        getDrug(type).addToDesiredValue(effect);
        PSCriteria.DRUG_EFFECTS_CHANGED.trigger(this);
        markDirty();
    }

    public void setDrugValue(DrugType<?> type, double effect) {
        getDrug(type).setDesiredValue(effect);
        PSCriteria.DRUG_EFFECTS_CHANGED.trigger(this);
        markDirty();
    }

    public void addToDrug(DrugInfluence influence) {
        influences.add(influence.clone());
        markDirty();
    }

    public void addAll(Iterable<DrugInfluence> influences) {
        influences.forEach(influence -> this.influences.add(influence.clone()));
        markDirty();
    }

    public Collection<Drug> getAllDrugs() {
        return drugs.values();
    }

    public Set<DrugType<?>> getAllDrugNames() {
        return drugs.keySet();
    }

    public void startBreathingSmoke(int time, int color) {
        this.breathSmokeColor = color;
        this.timeBreathingSmoke = time + 10; //10 is the time spent breathing in
        markDirty();

        entity.getWorld().playSoundFromEntity(entity, entity, PSSounds.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 0.02F, 1.5F);
    }

    public void increaseTeethGrindingSideEffect() {
        if (entity.age % 10 == 0) {
            teethGrindingRate = MathHelper.clamp(teethGrindingRate + 0.001F, 0, 100);
        }
    }

    public boolean isBreathingSmoke() {
        return timeBreathingSmoke > 0;
    }

    public int getAge() {
        return entity.age;
    }

    public void onTick() {
        //4 times / sec is enough
        if (entity.age % 5 == 0 && influences.removeIf(influence -> influence.update(this))) {
            markDirty();
        }

        drugs.values().forEach(drug -> drug.update(this));

        stomach.onTick();
        soundManager.update();

        Random random = entity.getRandom();

        if (entity.getWorld().isClient) {
            hallucinations.update();

            if (entity.isOnGround() && random.nextFloat() < getModifier(Drug.JUMP_CHANCE)) {
                ((LivingEntityDuck)entity).invokeJump();
            }

            if (!entity.handSwinging && random.nextFloat() < getModifier(Drug.PUNCH_CHANCE)) {
                entity.swingHand(Hand.MAIN_HAND);
            }
        } else {
            if (random.nextFloat() < getModifier(Drug.DROWSYNESS)) {
                entity.addExhaustion(0.05F);
            }

            if (!entity.hasStatusEffect(PSEffects.TEETH_GRINDING) && ((getDrugValue(DrugType.METHAMPHETAMINE) <= 0.001F
                    && teethGrindingRate > 1
                    && entity.age % 200 == 0
                    && random.nextFloat() * (entity.isSleeping() ? 2 : 1) < teethGrindingRate / 100F))) {
                PSCriteria.SIDE_EFFECT.trigger(entity);
                entity.addStatusEffect(new StatusEffectInstance(PSEffects.TEETH_GRINDING, 1000));
                if (!PacifierItem.consumePacifier(entity)) {
                    teethGrindingRate = Math.max(0, teethGrindingRate - 0.00001F);
                    entity.damage((ServerWorld)entity.getWorld(), damageOf(PSDamageTypes.TEETH_GRINDING), 1);
                } else {
                    pacifierSqueakDelay = 5 + entity.getRandom().nextInt(15);
                    entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PSSounds.ENTITY_PLAYER_PACIFIER_SQUEAK,
                            entity.getSoundCategory(),
                            entity.getRandom().nextTriangular(1, 0.2F),
                            entity.getRandom().nextTriangular(1, 0.2F)
                    );
                    entity.getWorld().emitGameEvent(entity, GameEvent.BLOCK_PLACE, entity.getBlockPos());
                    PSCriteria.SUCK_PACIFIER.trigger(entity);
                }
            }

            ((FireBlock)Blocks.FIRE).registerFlammableBlock(PSFluids.ATROPINE.getPhysical().getBlock(), 60, 100);

            if (pacifierSqueakDelay > 0 && --pacifierSqueakDelay == 0) {
                entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PSSounds.ENTITY_PLAYER_PACIFIER_SQUEAK,
                        entity.getSoundCategory(),
                        entity.getRandom().nextTriangular(1, 0.2F),
                        entity.getRandom().nextTriangular(1, 0.2F)
                );
                entity.getWorld().emitGameEvent(entity, GameEvent.BLOCK_PLACE, entity.getBlockPos());
            }

            if (entity.isOnFire()) {
                BlockPos headPos = BlockPos.ofFloored(entity.getEyePos());
                if (entity.getWorld().getBlockState(headPos).isOf(PSBlocks.FLAMMABLE_GAS)) {
                    entity.getWorld().setBlockState(headPos, Blocks.FIRE.getDefaultState());
                }
            }

            if (Psychedelicraft.getConfig().randomTicksUntilRiftSpawn.get() > 0
                    && random.nextInt(Psychedelicraft.getConfig().randomTicksUntilRiftSpawn.get()) == 0) {
                RealityRiftEntity.spawn(entity);
            }
        }

        if (isBreathingSmoke()) {
            timeBreathingSmoke--;

            if (timeBreathingSmoke > 10 && entity.getWorld().isClient) {
                if (random.nextInt(2) == 0) {
                    ParticleHelper.spawnParticleAtFace(entity, new DrugDustParticleEffect(PSParticles.EXHALED_SMOKE, breathSmokeColor, 1), random.nextFloat() * 0.05F + 0.1F);
                }

                if (random.nextInt(5) == 0) {
                    ParticleHelper.spawnParticleAtFace(entity, new DrugDustParticleEffect(PSParticles.EXHALED_SMOKE, breathSmokeColor, 2.5F), random.nextFloat() * 0.05F + 0.1F);
                }
            }
        }

        float speed = getModifier(Drug.SPEED);

        changeDrugModifierMultiply(entity, EntityAttributes.MOVEMENT_SPEED, speed);
        changeDrugModifierMultiply(entity, EntityAttributes.ATTACK_SPEED, speed);

        if (dirty) {
            dirty = false;
            sendCapabilities();
        }
    }

    public void sendCapabilities() {
        if (!entity.getWorld().isClient) {
            Channel.UPDATE_DRUG_PROPERTIES.sendToSurroundingPlayers(new MsgDrugProperties(this, entity.getRegistryManager()), entity);
            // We have to ensure it's sent to ourselves as well (Send to surrounding players ends to us but that doesn't seem to work when loading into a world??)
            Channel.UPDATE_DRUG_PROPERTIES.sendToPlayer(new MsgDrugProperties(this, entity.getRegistryManager()), (ServerPlayerEntity)entity);
        }
    }

    @Override
    public void fromNbt(NbtCompound tagCompound, WrapperLookup lookup) {
        drugs.clear();
        DRUGS_CODEC.decode(NbtOps.INSTANCE, tagCompound.getCompound("Drugs")).result().map(Pair::getFirst).ifPresent(drugs::putAll);
        influences.clear();
        DrugInfluence.LIST_CODEC.decode(NbtOps.INSTANCE, tagCompound.getList("drugInfluences", NbtElement.COMPOUND_TYPE)).result().map(Pair::getFirst).ifPresent(influences::addAll);
        stomach.fromNbt(tagCompound.getCompound("stomach"), lookup);
        teethGrindingRate = tagCompound.getFloat("teethGrindingRate");
        dirty = false;
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        DRUGS_CODEC.encodeStart(NbtOps.INSTANCE, drugs).result().ifPresent(drugs -> compound.put("Drugs", drugs));
        DrugInfluence.LIST_CODEC.encodeStart(NbtOps.INSTANCE, influences).result().ifPresent(influenceTagList -> compound.put("drugInfluences", influenceTagList));
        compound.put("stomach", stomach.toNbt(lookup));
        compound.putFloat("teethGrindingRate", teethGrindingRate);
    }

    public void copyFrom(DrugProperties old, boolean alive) {
        if (alive) {
            influences.clear();
            influences.addAll(old.influences);
            drugs.clear();
            drugs.putAll(old.drugs);
            timeBreathingSmoke = old.timeBreathingSmoke;
            breathSmokeColor = old.breathSmokeColor;
            markDirty();
        }
    }

    public boolean onAwoken() {
        if (asEntity().getWorld() instanceof ServerWorld sw) {
            drugs.values().forEach(drug -> drug.onWakeUp(sw, this));
        }
        influences.clear();
        markDirty();

        // TODO: (Sollace) Implement longer sleeping/comas
        return true;
    }

    public float onDamaged(DamageSource source, float initial) {
        if (source.isOf(DamageTypes.OUT_OF_WORLD) || initial >= Integer.MAX_VALUE) {
            return initial;
        }
        float painSuppression = getModifier(Drug.PAIN_SUPPRESSION);
        initial *= painSuppression;
        if (initial < 0.5F) {
            return 0;
        }
        return initial;
    }

    public Optional<Text> trySleep(BlockPos pos) {
        return getAllDrugs().stream().flatMap(drug -> drug.trySleep(pos).stream()).findFirst();
    }

    public float getModifier(Attribute modifier) {
        return modifier.get(this);
    }

    private void changeDrugModifierMultiply(LivingEntity entity, RegistryEntry<EntityAttribute> attribute, double value) {
        // 2: ret *= 1.0 + value
        changeDrugModifier(entity, attribute, value - 1.0, Operation.ADD_MULTIPLIED_TOTAL);
    }

    private void changeDrugModifier(LivingEntity entity, RegistryEntry<EntityAttribute> attribute, double value, Operation operation) {
        EntityAttributeInstance speedInstance = entity.getAttributeInstance(attribute);
        speedInstance.removeModifier(DRUG_EFFECT);
        speedInstance.addTemporaryModifier(new EntityAttributeModifier(DRUG_EFFECT, value, operation));
    }
}
