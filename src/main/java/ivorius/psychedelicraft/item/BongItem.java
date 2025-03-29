/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.ParticleHelper;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.recipe.RecipeUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Predicates;

/**
 * Created by calebmanley on 4/05/2014.
 *
 * Updated by Sollace on Jan 1 2023
 */
public class BongItem extends Item {
    public final ArrayList<Consumable> consumables = new ArrayList<>();

    public BongItem(Settings settings) {
        super(settings);
    }

    public BongItem consumes(Consumable consumable) {
        consumables.add(consumable);
        return this;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.TOOT_HORN;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        DrugProperties.of(entity).ifPresent(drugProperties -> {
            getUsedConsumable(drugProperties.asEntity()).ifPresent(consumable -> {
                PlayerInventory inventory = drugProperties.asEntity().getInventory();
                ItemStack s = inventory.removeStack(consumable.slot(), 1);
                drugProperties.addAll(consumable.content().drugInfluences().apply(s));
                stack.damage(1, drugProperties.asEntity(), EquipmentSlot.MAINHAND);
                drugProperties.startBreathingSmoke(10 + world.random.nextInt(10), consumable.content().smokeColor());
            });
        });

        return super.finishUsing(stack, world, entity);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!DrugProperties.of(player).isBreathingSmoke() && getConsumableSlotIndex(player) != -1) {
            player.setCurrentHand(hand);
            return ActionResult.CONSUME;
        }

        return ActionResult.FAIL;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.random.nextInt(3) == 0) {
            user.playSound(SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1, 1);
        }

        DrugProperties.of(user).ifPresent(drugProperties -> {
            getUsedConsumable(drugProperties.asEntity()).ifPresent(consumable -> {
                if (user.getRandom().nextInt(2) == 0) {
                    float s = (float)user.getRandom().nextTriangular(0.5, 0.25);
                    ParticleHelper.spawnParticleAtFace(user, new DrugDustParticleEffect(PSParticles.BUBBLE, consumable.content().smokeColor(), s), 0.2F);
                }
            });
        });
    }

    public Optional<RecipeUtils.Slot<Consumable>> getUsedConsumable(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return Optional.empty();
        }

        return RecipeUtils.slots(player.getInventory(), Predicates.alwaysTrue(), stack -> consumables.stream()
                .filter(consumable -> consumable.test(stack)).findFirst().orElse(null))
                .filter(slot -> slot.content() != null && slot.slot() != -1)
                .findFirst();
    }

    public int getConsumableSlotIndex(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return -1;
        }

        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            for (Consumable consumable : consumables) {
                if (consumable.test(inventory.getStack(i))) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 30;
    }

    public record Consumable (
            ItemStack consumedItem,
            Function<ItemStack, List<DrugInfluence>> drugInfluences,
            int smokeColor
    ) implements Predicate<ItemStack> {
        public Consumable(ItemStack consumedItem, DrugInfluence...drugInfluences) {
            this(consumedItem, stack -> List.of(drugInfluences), Colors.WHITE);
        }

        public Consumable(ItemStack consumedItem, Function<ItemStack, DrugInfluence> drugInfluences) {
            this(consumedItem, stack -> List.of(drugInfluences.apply(stack)), Colors.WHITE);
        }

        @Override
        public boolean test(ItemStack stack) {
            return ItemStack.areItemsEqual(stack, consumedItem);
        }
    }
}
