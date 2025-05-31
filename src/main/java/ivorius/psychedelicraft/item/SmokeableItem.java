/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.block.PlacedDrinksBlock;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.item.component.ItemDrugs;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class SmokeableItem extends Item {
    private final int useStages;

    public SmokeableItem(int useStages, Settings settings) {
        super(settings);
        this.useStages = useStages;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 25;
    }

    public int getStages() {
        return useStages;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.TOOT_HORN;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        DrugProperties.of(entity).ifPresent(drugProperties -> {
            ItemDrugs.get(stack).applyTo(stack, drugProperties);
        });

        if (!(entity instanceof PlayerEntity player && player.getAbilities().creativeMode)) {
            if (!stack.isDamageable()) {
                stack.decrement(1);
            } else {
                stack.damage(1, entity, EquipmentSlot.MAINHAND);
            }
        }

        return super.finishUsing(stack, world, entity);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return PlacedDrinksBlock.tryPlace(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!DrugProperties.of(player).isBreathingSmoke()) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }

        return TypedActionResult.fail(stack);
    }

    public void onIncinerated(ItemStack stack, ServerWorld world, BlockPos pos, AbstractFurnaceBlockEntity furnace) {
        ItemDrugs drugs = ItemDrugs.get(stack);
        world.getEntitiesByClass(PlayerEntity.class, new Box(pos).expand(3), EntityPredicates.EXCEPT_SPECTATOR).forEach(player -> {
            drugs.applyTo(stack, DrugProperties.of(player));
        });

        var effect = new DrugDustParticleEffect(PSParticles.EXHALED_SMOKE, drugs.smokeColor().orElse(ItemDrugs.DEFAULT_SMOKE_COLOR), 1);
        for (int i = 0; i < 30; i++) {
            world.spawnParticles(effect,
                    world.random.nextTriangular(pos.getX() + 0.5, 0.3),
                    pos.getY() + 1,
                    world.random.nextTriangular(pos.getZ() + 0.5, 0.3),
                    1,
                    world.random.nextTriangular(0, 0.3),
                    world.random.nextTriangular(0.8, 0.3),
                    world.random.nextTriangular(0, 0.3), 0.001F);
        }
    }
}
