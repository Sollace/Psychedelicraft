/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraft.crafting.RecipeUtils;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
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
        return UseAction.BLOCK;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        DrugProperties.of(entity).ifPresent(drugProperties -> {
            getUsedConsumable(drugProperties.asEntity()).ifPresent(consumable -> {
                // TODO: (Sollace) check for possible client desync
                drugProperties.asEntity().getInventory().removeOne(consumable.getKey());
                drugProperties.addAll(consumable.getValue().drugInfluences().apply(consumable.getKey()));
                stack.damage(1, drugProperties.asEntity(), p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                drugProperties.startBreathingSmoke(10 + world.random.nextInt(10), consumable.getValue().smokeColor);
            });
        });

        return super.finishUsing(stack, world, entity);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (DrugProperties.of(player).timeBreathingSmoke <= 0 && getUsedConsumable(player).isPresent()) {
            return TypedActionResult.consume(player.getStackInHand(hand));
        }

        return TypedActionResult.fail(ItemStack.EMPTY);
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
    public int getMaxUseTime(ItemStack stack) {
        return 30;
    }

    public record Consumable (
            ItemStack consumedItem,
            Function<ItemStack, List<DrugInfluence>> drugInfluences,
            float[] smokeColor
    ) {
        public Consumable(ItemStack consumedItem, DrugInfluence...drugInfluences) {
            this(consumedItem, stack -> List.of(drugInfluences), new float[]{ 1, 1, 1 });
        }

        public Consumable(ItemStack consumedItem, Function<ItemStack, DrugInfluence> drugInfluences) {
            this(consumedItem, stack -> List.of(drugInfluences.apply(stack)), new float[]{ 1, 1, 1 });
        }
    }
}
