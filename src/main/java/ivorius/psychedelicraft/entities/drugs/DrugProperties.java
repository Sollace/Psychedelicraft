/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs;

import ivorius.psychedelicraft.*;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.rendering.IDrugRenderer;
import ivorius.psychedelicraft.client.screen.TickableContainer;
import ivorius.psychedelicraft.entities.*;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.*;

public class DrugProperties implements NbtSerialisable {
    public static final UUID drugUUID = UUID.fromString("2da054e7-0fe0-4fb4-bf2c-a185a5f72aa1"); // Randomly gen'd
    public static final String EEP_KEY = "DrugHelper";
    public static final String EEP_CMP_KEY = "drugData";

    private Map<String, Drug> drugs;
    public List<DrugInfluence> influences = new ArrayList<>();

    public boolean hasChanges;

    public IDrugRenderer renderer;
    public DrugMessageDistorter messageDistorter = new DrugMessageDistorter();
    public DrugHallucinationManager hallucinationManager = new DrugHallucinationManager();
    public DrugMusicManager musicManager = new DrugMusicManager();

    public int ticksExisted = 0;

    public int timeBreathingSmoke;
    public float[] breathSmokeColor;

    public int delayUntilHeartbeat;
    public int delayUntilBreath;
    public boolean lastBreathWasIn;

    private final PlayerEntity entity;

    public DrugProperties(PlayerEntity entity) {
        this.entity = entity;
        drugs = DrugRegistry.createDrugs(entity).stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public static DrugProperties getDrugProperties(Entity entity) {
        if (entity instanceof DrugPropertiesContainer c) {
            return c.getDrugProperties();
        }
        return null;
    }

    public static DrugProperties of(PlayerEntity player) {
        return ((DrugPropertiesContainer)player).getDrugProperties();
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

    public void addDrug(String drugName, Drug drug) {
        drugs.put(drugName, drug);
    }

    /**
     * TODO: Client separation
     * @return
     */
    public Optional<IDrugRenderer> getDrugRenderer() {
        return Optional.ofNullable(renderer);
    }

    @Nullable
    public Drug getDrug(String drugName) {
        return drugs.get(drugName);
    }

    public float getDrugValue(String drugName) {
        return (float) drugs.get(drugName).getActiveValue();
    }

    public void addToDrug(String drugName, double effect) {
        if (!drugs.containsKey(drugName)) {
            Psychedelicraft.LOGGER.warn("Tried to add to drug " + drugName);
            return;
        }

        hasChanges = true;
        drugs.get(drugName).addToDesiredValue(effect);
    }

    public void setDrugValue(String drugName, double effect) {
        if (!drugs.containsKey(drugName)) {
            Psychedelicraft.LOGGER.warn("Tried to set drug value " + drugName);
            return;
        }

        hasChanges = true;
        drugs.get(drugName).setDesiredValue(effect);
    }

    public void addToDrug(DrugInfluence influence) {
        hasChanges = true;
        influences.add(influence);
    }

    public void addAll(Iterable<DrugInfluence> influences) {
        influences.forEach(influence -> {
            this.influences.add(influence.clone());
        });
        hasChanges = true;
    }

    public Collection<Drug> getAllDrugs() {
        return drugs.values();
    }

    public Set<String> getAllDrugNames() {
        return drugs.keySet();
    }

    public String[] getAllVisibleDrugNames() {
        return drugs.entrySet().stream().filter(e -> e.getValue().isVisible()).map(Map.Entry::getKey).toArray(String[]::new);
    }

    public boolean doesDrugExist(String name) {
        return drugs.containsKey(name);
    }

    public void startBreathingSmoke(int time, float[] color) {
        this.breathSmokeColor = color == null ? new float[]{1.0f, 1.0f, 1.0f} : color;
        this.timeBreathingSmoke = time + 10; //10 is the time spent breathing in
    }

    public void onTick() {
        if (ticksExisted % 5 == 0) { //4 times / sec is enough
            influences.removeIf(influence -> {
                influence.update(this);
                return influence.isDone();
            });
        }

        drugs.values().forEach(drug -> drug.update(entity, this));

        Random random = entity.getRandom();

        if (entity.world.isClient) {
            hallucinationManager.update(entity, this);
            musicManager.update(entity, this);

            if (delayUntilHeartbeat > 0) {
                delayUntilHeartbeat--;
            }

            if (delayUntilBreath > 0) {
                delayUntilBreath--;
            }

            if (delayUntilHeartbeat == 0) {
                float heartbeatVolume = 0;
                for (Drug drug : getAllDrugs()) {
                    heartbeatVolume += drug.heartbeatVolume();
                }

                if (heartbeatVolume > 0) {
                    float speed = 1;
                    for (Drug drug : getAllDrugs()) {
                        speed += drug.heartbeatSpeed();
                    }

                    delayUntilHeartbeat = MathHelper.floor(35.0f / (speed - 1.0f));

                    // TODO: (Sollace) PSSoundEvents
                    // entity.world.playSound(entity.getX(), entity.getY(), entity.getZ(), Psychedelicraft.modBase + "heartBeat", heartbeatVolume, speed, false);
                }
            }

            if (delayUntilBreath == 0) {
                float breathVolume = 0;
                for (Drug drug : getAllDrugs()) {
                    breathVolume += drug.breathVolume();
                }

                lastBreathWasIn = !lastBreathWasIn;

                if (breathVolume > 0) {
                    float speed = 1.0f;
                    for (Drug drug : getAllDrugs()) {
                        speed += drug.breathSpeed();
                    }
                    delayUntilBreath = MathHelper.floor(30F / speed);

                    // TODO: (Sollace) PSSoundEvents
                    // entity.world.playSound(entity.getX(), entity.getY(), entity.getZ(), Psychedelicraft.modBase + "breath", breathVolume, speed * 0.1f + 0.9f + (lastBreathWasIn ? 0.15f : 0.0f), false);
                }
            }

            if (entity.isOnGround()) {
                float jumpChance = 0;
                for (Drug drug : getAllDrugs())
                    jumpChance += drug.randomJumpChance();

                if (random.nextFloat() < jumpChance) {
                    PSAccessHelperEntity.jump(entity);
                }
            }

            if (!entity.handSwinging) {
                float punchChance = 0.0f;
                for (Drug drug : getAllDrugs()) {
                    punchChance += drug.randomPunchChance();
                }

                if (random.nextFloat() < punchChance) {
                    entity.swingHand(Hand.MAIN_HAND);
                }
            }
        }

        if (timeBreathingSmoke > 0) {
            timeBreathingSmoke--;

            if (timeBreathingSmoke > 10 && entity.world.isClient) {
                Vec3d look = entity.getRotationVec(1);


                if (random.nextInt(2) == 0) {
                    float s = random.nextFloat() * 0.05f + 0.1f;
                    ParticleHelper.spawnColoredParticle(entity, breathSmokeColor, look, s, 1.0f);
                }

                if (random.nextInt(5) == 0) {
                    float s = random.nextFloat() * 0.05f + 0.1f;
                    ParticleHelper.spawnColoredParticle(entity, breathSmokeColor, look, s, 2.5f);
                }
            }
        }

        if (renderer != null && entity.world.isClient) {
            renderer.update(this, entity);
        }

        changeDrugModifierMultiply(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, getSpeedModifier());

        ticksExisted++;

        if (hasChanges) {
            hasChanges = false;

            if (!entity.world.isClient) {
                Channel.UPDATE_DRUG_PROPERTIES.sendToSurroundingPlayers(new MsgDrugProperties(this), entity);
                Channel.UPDATE_DRUG_PROPERTIES.sendToPlayer(new MsgDrugProperties(this), (ServerPlayerEntity)entity);
            }
        }

        if (!entity.world.isClient && Psychedelicraft.getConfig().balancing.randomTicksUntilRiftSpawn > 0) {
            if (random.nextInt(Psychedelicraft.getConfig().balancing.randomTicksUntilRiftSpawn) == 0) {
                spawnRiftAtPlayer();
            }
        }

        if (entity.currentScreenHandler instanceof TickableContainer updateable) {
            updateable.onClientTick();
        }
    }

    private void spawnRiftAtPlayer() {
        EntityRealityRift rift = PSEntities.REALITY_RIFT.create(entity.world);

        double xP = (entity.getRandom().nextDouble() - 0.5) * 100;
        double yP = (entity.getRandom().nextDouble() - 0.5) * 100;
        double zP = (entity.getRandom().nextDouble() - 0.5) * 100;

        rift.setPosition(entity.getX() + xP, entity.getY() + yP, entity.getZ() + zP);
        entity.world.spawnEntity(rift);
    }

    @Override
    public void fromNbt(NbtCompound tagCompound) {
        NbtCompound drugData = tagCompound.getCompound("Drugs");
        drugs.forEach((key, drug) -> {
            drug.fromNbt(drugData.getCompound(key));
        });
        influences.clear();
        NbtList influenceTagList = tagCompound.getList("drugInfluences", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < influenceTagList.size(); i++) {
            NbtCompound compound = influenceTagList.getCompound(i);

            // TODO: (Sollace) Modernize this
            Class<? extends DrugInfluence> influenceClass = DrugRegistry.getClass(compound.getString("influenceClass"));

            if (influenceClass != null) {
                DrugInfluence inf = null;

                try {
                    inf = influenceClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (inf != null) {
                    inf.fromNbt(compound);
                    addToDrug(inf);
                }
            }
        }

        this.ticksExisted = tagCompound.getInt("drugsTicksExisted");
        hasChanges = false;
    }

    @Override
    public void toNbt(NbtCompound compound) {
        NbtCompound drugsComp = new NbtCompound();
        drugs.forEach((key, drug) -> drugsComp.put(key, drug.toNbt()));
        compound.put("Drugs", drugsComp);

        NbtList influenceTagList = new NbtList();
        for (DrugInfluence influence : influences) {
            NbtCompound infCompound = influence.toNbt();
            infCompound.putString("influenceClass", DrugRegistry.getID(influence.getClass()));
            influenceTagList.add(infCompound);
        }
        compound.put("drugInfluences", influenceTagList);
        compound.putInt("drugsTicksExisted", ticksExisted);
    }

    public boolean onAwoken() {
        drugs.values().forEach(drug -> drug.reset(entity, this));
        influences.clear();
        hasChanges = true;

        // TODO: (Sollace) Reimplement longer sleeping/comas
        return true;
    }

    public void receiveChatMessage(LivingEntity entity, String message) {
        hallucinationManager.receiveChatMessage(entity, message);
    }

    public float getSpeedModifier() {
        float modifier = 1;
        for (Drug drug : getAllDrugs()) {
            modifier *= drug.speedModifier();
        }
        return modifier;
    }

    public float getDigSpeedModifier() {
        float modifier = 1;
        for (Drug drug : getAllDrugs()) {
            modifier *= drug.digSpeedModifier();
        }
        return modifier;
    }
/*
    public EntityPlayer.EnumStatus getDrugSleepStatus()
    {
        for (Drug drug : getAllDrugs())
        {
            EntityPlayer.EnumStatus status = drug.getSleepStatus();
            if (status != null)
                return status;
        }

        return null;
    }
*/
    public float getSoundMultiplier() {
        float modifier = 1;
        for (Drug drug : getAllDrugs()) {
            modifier *= drug.soundVolumeModifier();
        }
        return modifier;
    }

    public float[] getDigitalEffectPixelResize() {
        // TODO: Shouldn't this be stored? Why is it even here?
        return PsychedelicraftClient.getConfig().visual.getDigitalEffectPixelResize();
    }

    public void changeDrugModifierMultiply(LivingEntity entity, EntityAttribute attribute, double value) {
        // 2: ret *= 1.0 + value
        changeDrugModifier(entity, attribute, value - 1.0, Operation.MULTIPLY_TOTAL);
    }

    public void changeDrugModifier(LivingEntity entity, EntityAttribute attribute, double value, Operation operation) {
        EntityAttributeInstance speedInstance = entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);  // TODO: (Sollace) [sic] Should be using the passed attribute
        EntityAttributeModifier oldModifier = speedInstance.getModifier(DrugProperties.drugUUID);

        if (oldModifier != null) {
            speedInstance.removeModifier(oldModifier);
        }

        speedInstance.addTemporaryModifier(new EntityAttributeModifier(DrugProperties.drugUUID, "Drug Effects", value, operation));
    }
}
