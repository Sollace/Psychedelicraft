package ivorius.psychedelicraft.entity.drug;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.BagContentsComponent;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class Stomach implements NbtSerialisable {

    private final DrugProperties properties;

    private int vomitCount;
    private int vomitCooldown;
    private int vomitingTicks;

    private final PlayerEntity entity;

    public Stomach(DrugProperties properties) {
        this.properties = properties;
        this.entity = properties.asEntity();
    }

    public LockableHungerManager getStomach() {
        return (LockableHungerManager)entity.getHungerManager();
    }

    public GluttonyManager getGlut() {
        return (GluttonyManager)entity.getHungerManager();
    }

    public void onTick() {
        final float hungerSuppression = MathHelper.clamp(properties.getModifier(Drug.HUNGER_SUPPRESSION), -1, 1);
        final boolean shouldLockHunger = Math.abs(hungerSuppression) > MathHelper.EPSILON;

        if (shouldLockHunger != (getStomach().getLockedState() != null)) {
            if (shouldLockHunger) {
                getStomach().lockHunger(hungerSuppression > 0, hungerSuppression);
            } else {
                getStomach().unlockHunger();
            }

            properties.markDirty();
        }

        if (getStomach().getLockedState() != null) {
            getStomach().getLockedState().setRate(hungerSuppression);
        }

        if (getGlut().getOvereating() >= 10) {
            vomit();
        }

        if (vomitingTicks > 0) {
            if (entity.age % (int)(1 + entity.getWorld().random.nextFloat() * 3) == 0) {
                int count = (int)(entity.getWorld().random.nextFloat() * (vomitingTicks / 2));
                for (int i = 0; i < count; i++) {
                    vomitingTicks--;
                    if (!entity.getWorld().isClient) {
                        entity.dropItem(PSItems.VOMIT.getDefaultStack(), true, true).setPickupDelayInfinite();
                        playBarfNoise();
                    }
                }
                properties.markDirty();
            }
        }
        if (vomitCooldown > 0) {
            properties.markDirty();
            if (vomitCooldown-- <= 0) {
                vomitCount = 0;
            }
        }
    }

    public void vomit() {
        ItemStack heldItem = entity.getStackInHand(Hand.OFF_HAND);
        if (heldItem.isOf(PSItems.PAPER_BAG) && BagContentsComponent.get(heldItem).isEmpty()) {
            playBarfNoise();

            if (!entity.isCreative()) {
                heldItem.decrement(1);
            }
            if (heldItem.isEmpty()) {
                entity.setStackInHand(Hand.OFF_HAND, PSItems.BAG_O_VOMIT.getDefaultStack());
            } else {
                entity.getInventory().offerOrDrop(PSItems.BAG_O_VOMIT.getDefaultStack());
            }
        } else {
            vomitingTicks = entity.getWorld().random.nextBetween(10, 100);
            if (++vomitCount > 16) {
                entity.damage(properties.damageOf(PSDamageTypes.OVER_EATING), Integer.MAX_VALUE);
            }
        }
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 1));
        getGlut().setOvereating(0);
        properties.markDirty();
    }

    private void playBarfNoise() {
        entity.getWorld().playSoundFromEntity(null, entity, SoundEvents.ENTITY_VILLAGER_DEATH, entity.getSoundCategory(), 1, (float)entity.getWorld().random.nextTriangular(0.5, 0.25));
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        if (compound.contains("hunger", NbtElement.COMPOUND_TYPE)) {
            getStomach().setLockedState(LockableHungerManager.State.fromNbt(compound.getCompound("hunger")));
        } else {
            getStomach().unlockHunger();
        }
        vomitCount = compound.getInt("vomitCount");
        vomitCooldown = compound.getInt("vomitCooldown");
        vomitingTicks = compound.getInt("vomitingTicks");
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        LockableHungerManager.State lockedHungerState = getStomach().getLockedState();
        if (lockedHungerState != null) {
            compound.put("hunger", lockedHungerState.toNbt());
        }
        compound.putInt("vomitCount", vomitCount);
        compound.putInt("vomitCooldown", vomitCooldown);
        compound.putInt("vomitingTicks", vomitingTicks);
    }
}
