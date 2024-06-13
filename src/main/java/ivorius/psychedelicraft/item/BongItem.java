/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.recipe.RecipeUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Function;

import org.joml.Vector3f;

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
                int slot = inventory.indexOf(consumable.getKey());
                inventory.removeStack(slot, 1);
                drugProperties.addAll(consumable.getValue().drugInfluences().apply(consumable.getKey()));
                stack.damage(1, drugProperties.asEntity(), EquipmentSlot.MAINHAND);
                drugProperties.startBreathingSmoke(10 + world.random.nextInt(10), consumable.getValue().smokeColor);
            });
        });

        return super.finishUsing(stack, world, entity);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!DrugProperties.of(player).isBreathingSmoke() && hasUsableConsumable(player)) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }

        return TypedActionResult.fail(stack);
    }

    public Optional<Map.Entry<ItemStack, Consumable>> getUsedConsumable(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return Optional.empty();
        }

        return RecipeUtils.stacks(((PlayerEntity)entity)
                .getInventory())
                .flatMap(stack -> consumables.stream()
                    .filter(consumable -> ItemStack.areItemsEqual(stack, consumable.consumedItem))
                    .limit(1)
                    .map(c -> Map.entry(stack, c)))
                .findFirst();
    }

    public boolean hasUsableConsumable(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }

        PlayerInventory inventory = ((PlayerEntity)entity).getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            for (Consumable consumable : consumables) {
                if (ItemStack.areItemsEqual(inventory.getStack(i), consumable.consumedItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 30;
    }

    public record Consumable (
            ItemStack consumedItem,
            Function<ItemStack, List<DrugInfluence>> drugInfluences,
            Vector3f smokeColor
    ) {
        public Consumable(ItemStack consumedItem, DrugInfluence...drugInfluences) {
            this(consumedItem, stack -> List.of(drugInfluences), SmokeableItem.WHITE);
        }

        public Consumable(ItemStack consumedItem, Function<ItemStack, DrugInfluence> drugInfluences) {
            this(consumedItem, stack -> List.of(drugInfluences.apply(stack)), SmokeableItem.WHITE);
        }
    }
}
