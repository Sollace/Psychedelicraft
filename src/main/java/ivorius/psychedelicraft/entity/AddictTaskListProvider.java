package ivorius.psychedelicraft.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.PSDamageSources;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.FindInteractionTargetTask;
import net.minecraft.entity.ai.brain.task.HoldTradeOffersTask;
import net.minecraft.entity.ai.brain.task.ScheduleActivityTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;

public class AddictTaskListProvider {
    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> createWorkTasks(Pair<Integer, Task<LivingEntity>> busyFollowTasks, float speed) {
        return ImmutableList.of(
                busyFollowTasks,
                Pair.of(7, new WorkTask()),
                Pair.of(10, new HoldTradeOffersTask(400, 1600)),
                Pair.of(10, new FindInteractionTargetTask(EntityType.PLAYER, 8)),
             // Pair.of(11, goToPlayer(MemoryModuleType.NEAREST_PLAYERS, speed, 9)),
                Pair.of(99, new ScheduleActivityTask()
        ));
    }
/*
    public static SingleTickTask<PathAwareEntity> goToPlayer(MemoryModuleType<List<PlayerEntity>> posModule, float walkSpeed, int maxDistance) {
        MutableLong mutableLong = new MutableLong(0L);
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(posModule)).apply(context, (walkTarget, pos) -> (world, entity, time) -> {
            PlayerEntity target = context.getValue(pos).get(0);
            if (target == null || !target.getPos().isInRange(entity.getPos(), maxDistance)) {
                return false;
            }
            if (time <= mutableLong.getValue()) {
                return true;
            }
            Optional<Vec3d> optional = Optional.ofNullable(FuzzyTargeting.find(entity, 8, 6));
            walkTarget.remember(optional.map(targetPos -> new WalkTarget(targetPos, walkSpeed, 1)));
            mutableLong.setValue(time + 180L);
            return true;
        }));
    }
*/
    public static float getShakeAmount(LivingEntity entity) {
        float healthScale = 1 - (entity.getHealth() / entity.getMaxHealth());
        float shakeAmount = (float)(Math.cos(entity.age * 3.25) * Math.PI * 0.4F * (1 + healthScale * 8));
        entity.hurtTime = Math.abs(shakeAmount) > 5F ? 1 : 0;
        return shakeAmount;
    }

    static class WorkTask extends Task<VillagerEntity> {
        private static final long RUN_TIME = 300;
        private long lastCheckedTime;

        public WorkTask() {
            super(ImmutableMap.of());
        }

        @Override
        protected boolean shouldRun(ServerWorld world, VillagerEntity entity) {
            if (world.getTime() - lastCheckedTime < RUN_TIME || world.random.nextInt(2) != 0) {
                return false;
            }
            this.lastCheckedTime = world.getTime();
            return true;
        }

        @Override
        protected void run(ServerWorld world, VillagerEntity entity, long l) {
            if (entity.shouldRestock()) {
                entity.playWorkSound();
                entity.restock();
                entity.damage(PSDamageSources.OVERDOSE, 1 + (world.getRandom().nextFloat() * 5));
            }
        }
    }
}
