/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs;

import ivorius.psychedelicraft.*;
import ivorius.psychedelicraft.client.rendering.IDrugRenderer;
import ivorius.psychedelicraft.client.screen.UpdatableContainer;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.EntityRealityRift;
import ivorius.psychedelicraft.entities.PSAccessHelperEntity;
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
import java.util.*;
import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class DrugProperties {
    public static final UUID drugUUID = UUID.fromString("2da054e7-0fe0-4fb4-bf2c-a185a5f72aa1"); // Randomly gen'd
    public static final String EEP_KEY = "DrugHelper";
    public static final String EEP_CMP_KEY = "drugData";

    public static boolean waterOverlayEnabled;
    public static boolean hurtOverlayEnabled;
    public static float[] digitalEffectPixelRescale;

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

    public DrugProperties(LivingEntity entity) {
        drugs = DrugRegistry.createDrugs(entity).stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Nullable
    public static DrugProperties getDrugProperties(Entity entity)
    {
        // TODO: (Sollace) Mixin hooks! Eventually...
        //if (entity != null)
          //  return (DrugProperties) entity.getExtendedProperties(EEP_KEY);

        return null;
    }

    public void addDrug(String drugName, Drug drug)
    {
        drugs.put(drugName, drug);
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
            Psychedelicraft.logger.warn("Tried to add to drug " + drugName);
            return;
        }

        hasChanges = true;
        drugs.get(drugName).addToDesiredValue(effect);
    }

    public void setDrugValue(String drugName, double effect) {
        if (!drugs.containsKey(drugName)) {
            Psychedelicraft.logger.warn("Tried to set drug value " + drugName);
            return;
        }

        hasChanges = true;
        drugs.get(drugName).setDesiredValue(effect);
    }

    public void addToDrug(DrugInfluence influence) {
        hasChanges = true;
        influences.add(influence);
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

    public void updateDrugEffects(LivingEntity entity)
    {
        Random random = entity.getRandom();

        if (ticksExisted % 5 == 0) //4 times / sec is enough
        {
            for (Iterator<DrugInfluence> iterator = influences.iterator(); iterator.hasNext(); )
            {
                DrugInfluence influence = iterator.next();
                influence.update(this);

                if (influence.isDone())
                    iterator.remove();
            }
        }

        for (String key : drugs.keySet())
        {
            Drug drug = drugs.get(key);
            drug.update(entity, this);
        }

        if (entity.world.isClient)
        {
            hallucinationManager.update(entity, this);
            musicManager.update(entity, this);

            if (delayUntilHeartbeat > 0)
                delayUntilHeartbeat--;
            if (delayUntilBreath > 0)
                delayUntilBreath--;

            if (delayUntilHeartbeat == 0)
            {
                float heartbeatVolume = 0.0f;
                for (Drug drug : getAllDrugs())
                    heartbeatVolume += drug.heartbeatVolume();

                if (heartbeatVolume > 0.0f)
                {
                    float speed = 1.0f;
                    for (Drug drug : getAllDrugs())
                        speed += drug.heartbeatSpeed();

                    delayUntilHeartbeat = MathHelper.floor(35.0f / (speed - 1.0f));

                    // TODO: (Sollace) PSSoundEvents
                    // entity.world.playSound(entity.getX(), entity.getY(), entity.getZ(), Psychedelicraft.modBase + "heartBeat", heartbeatVolume, speed, false);
                }
            }

            if (delayUntilBreath == 0)
            {
                float breathVolume = 0.0f;
                for (Drug drug : getAllDrugs())
                    breathVolume += drug.breathVolume();

                lastBreathWasIn = !lastBreathWasIn;

                if (breathVolume > 0.0f)
                {
                    float speed = 1.0f;
                    for (Drug drug : getAllDrugs())
                        speed += drug.breathSpeed();
                    delayUntilBreath = MathHelper.floor(30.0f / speed);

                    // TODO: (Sollace) PSSoundEvents
                    // entity.world.playSound(entity.getX(), entity.getY(), entity.getZ(), Psychedelicraft.modBase + "breath", breathVolume, speed * 0.1f + 0.9f + (lastBreathWasIn ? 0.15f : 0.0f), false);
                }
            }

            if (entity.isOnGround()) {
                float jumpChance = 0.0f;
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

        if (timeBreathingSmoke > 0)
        {
            timeBreathingSmoke--;

            if (timeBreathingSmoke > 10 && entity.world.isClient)
            {
                Vec3d look = entity.getCameraPosVec(1);

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

        changeDrugModifierMultiply(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, getSpeedModifier(entity));

        ticksExisted++;

        if (hasChanges)
        {
            hasChanges = false;

            if (!entity.world.isClient) {
                // TODO: (Sollace) Packet to send drug update data to the client
                // PSNetworkHelperServer.sendEEPUpdatePacket(entity, EEP_KEY, "DrugData", Psychedelicraft.network);
            }
        }

        if (entity instanceof PlayerEntity player) {
            if (!entity.world.isClient && PSConfig.randomTicksUntilRiftSpawn > 0) {
                if (random.nextInt(PSConfig.randomTicksUntilRiftSpawn) == 0) {
                    spawnRiftAtPlayer(player);
                }
            }

            if (player.currentScreenHandler instanceof UpdatableContainer updateable) {
                updateable.updateAsCustomContainer();
            }
        }
    }

    public static void spawnRiftAtPlayer(PlayerEntity player) {
        EntityRealityRift rift = new EntityRealityRift(player.getEntityWorld());

        double xP = (player.getRandom().nextDouble() - 0.5) * 100.0;
        double yP = (player.getRandom().nextDouble() - 0.5) * 100.0;
        double zP = (player.getRandom().nextDouble() - 0.5) * 100.0;

        rift.setPosition(player.getX() + xP, player.getY() + yP, player.getZ() + zP);
        player.getEntityWorld().spawnEntity(rift);
    }

    public void readFromNBT(NbtCompound tagCompound, boolean fromPacket)
    {
        NbtCompound drugData = tagCompound.getCompound("Drugs");
        for (String key : drugs.keySet())
            drugs.get(key).readFromNBT(drugData.getCompound(key));

        influences.clear();
        NbtList influenceTagList = tagCompound.getList("drugInfluences", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < influenceTagList.size(); i++)
        {
            NbtCompound compound = influenceTagList.getCompound(i);

            // TODO: (Sollace) Modernize this
            Class<? extends DrugInfluence> influenceClass = DrugRegistry.getClass(compound.getString("influenceClass"));

            if (influenceClass != null)
            {
                DrugInfluence inf = null;

                try
                {
                    inf = influenceClass.newInstance();
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }

                if (inf != null)
                {
                    inf.readFromNBT(compound);
                    addToDrug(inf);
                }
            }
        }

        this.ticksExisted = tagCompound.getInt("drugsTicksExisted");

        if (fromPacket)
            hasChanges = true;
    }

    public void writeToNBT(NbtCompound compound) {
        NbtCompound drugsComp = new NbtCompound();
        for (String key : drugs.keySet())
        {
            NbtCompound cmp = new NbtCompound();
            drugs.get(key).writeToNBT(cmp);
            drugsComp.put(key, cmp);
        }
        compound.put("Drugs", drugsComp);

        NbtList influenceTagList = new NbtList();
        for (DrugInfluence influence : influences)
        {
            NbtCompound infCompound = new NbtCompound();
            influence.writeToNBT(infCompound);
            infCompound.putString("influenceClass", DrugRegistry.getID(influence.getClass()));
            influenceTagList.add(infCompound);
        }
        compound.put("drugInfluences", influenceTagList);
        compound.putInt("drugsTicksExisted", ticksExisted);
    }

    public NbtCompound createNBTTagCompound() {
        NbtCompound tagCompound = new NbtCompound();
        writeToNBT(tagCompound);
        return tagCompound;
    }

    public void wakeUp(LivingEntity entity)
    {
        for (String key : drugs.keySet())
            drugs.get(key).reset(entity, this);
        influences.clear();

        hasChanges = true;
    }

    public void receiveChatMessage(LivingEntity entity, String message)
    {
        hallucinationManager.receiveChatMessage(entity, message);
    }

    public float getSpeedModifier(LivingEntity entity)
    {
        float modifier = 1.0F;
        for (Drug drug : getAllDrugs())
            modifier *= drug.speedModifier();

        return modifier;
    }

    public float getDigSpeedModifier(LivingEntity entity)
    {
        float modifier = 1.0F;
        for (Drug drug : getAllDrugs())
            modifier *= drug.digSpeedModifier();

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
        return digitalEffectPixelRescale;
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
