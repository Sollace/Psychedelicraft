package ivorius.psychedelicraft.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.RecipeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.BoneMealTask;
import net.minecraft.entity.ai.brain.task.FindInteractionTargetTask;
import net.minecraft.entity.ai.brain.task.GoToIfNearbyTask;
import net.minecraft.entity.ai.brain.task.GoToNearbyPositionTask;
import net.minecraft.entity.ai.brain.task.GoToSecondaryPositionTask;
import net.minecraft.entity.ai.brain.task.HoldTradeOffersTask;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.ScheduleActivityTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerWalkTowardsTask;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.event.GameEvent;

public class DealerTaskListProvider {

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> createWorkTasks(Pair<Integer, Task<LivingEntity>> busyFollowTasks, float speed) {
        return ImmutableList.of(
                busyFollowTasks,
                Pair.of(5, new RandomTask<>(ImmutableList.of(
                        Pair.of(new DealerWorkTask(), 7),
                        Pair.of(GoToIfNearbyTask.create(MemoryModuleType.JOB_SITE, 0.4f, 4), 2),
                        Pair.of(GoToNearbyPositionTask.create(MemoryModuleType.JOB_SITE, 0.4f, 1, 10), 5),
                        Pair.of(GoToSecondaryPositionTask.create(MemoryModuleType.SECONDARY_JOB_SITE, speed, 1, 6, MemoryModuleType.JOB_SITE), 5),
                        Pair.of(new DealerVillagerTask(), 2),
                        Pair.of(new BoneMealTask(), 4))
                )),
                Pair.of(10, new HoldTradeOffersTask(400, 1600)),
                Pair.of(10, FindInteractionTargetTask.create(EntityType.PLAYER, 4)),
                Pair.of(2, VillagerWalkTowardsTask.create(MemoryModuleType.JOB_SITE, speed, 9, 100, 1200)),
            //  Pair.of(3, new GiveGiftsToHeroTask(100)),
                Pair.of(99, ScheduleActivityTask.create()
        ));
    }

    static class DealerWorkTask extends VillagerWorkTask {
        @Override
        protected void performAdditionalWork(ServerWorld world, VillagerEntity entity) {
            Optional<GlobalPos> optional = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
            if (!optional.isPresent()) {
                return;
            }
            GlobalPos globalPos = optional.get();
            BlockState blockState = world.getBlockState(globalPos.getPos());
            if (blockState.isIn(PSTags.DRYING_TABLES)) {

                DryingTableBlockEntity blockEntity = (DryingTableBlockEntity)world.getBlockEntity(globalPos.getPos());

                int[] inputSlots = blockEntity.getAvailableSlots(Direction.DOWN);

                ItemStack output = blockEntity.getStack(0);
                if (!output.isEmpty()) {
                    blockEntity.clear();
                    output = entity.getInventory().addStack(output);
                    if (!output.isEmpty()) {
                        entity.dropStack(output);
                    }
                }
                world.getRecipeManager().getAllMatches(PSRecipes.DRYING_TYPE, entity.getInventory(), world)
                        .stream()
                        .map(RecipeEntry::value)
                        .filter(recipe -> recipe.getIngredients().size() <= inputSlots.length)
                        .findFirst()
                        .ifPresent(recipe -> {
                    List<Ingredient> ingredients = new ArrayList<>(recipe.getIngredients());
                    List<ItemStack> consumedMaterials = new ArrayList<>();
                    RecipeUtils.slots(entity.getInventory(), s -> !s.isEmpty(), Function.identity()).forEach(slot -> {
                        ingredients.stream().filter(ingredient -> ingredient.test(slot.content())).findFirst().ifPresent(matchedIngredient -> {
                            ingredients.remove(matchedIngredient);
                            consumedMaterials.add(slot.content());
                            slot.set(ItemStack.EMPTY);
                        });
                    });
                    for (int slot : inputSlots) {
                        if (consumedMaterials.isEmpty()) {
                            break;
                        }
                        blockEntity.setStack(slot, consumedMaterials.remove(0));
                    }
                });
            }
        }

    }

    static class DealerVillagerTask extends MultiTickTask<VillagerEntity> { // version of FarmerVillagerTask changed to work with drug crops
        private static final int MAX_RUN_TIME = 200;
        public static final float WALK_SPEED = 0.5f;

        @Nullable
        private BlockPos currentTarget;
        private long nextResponseTime;
        private int ticksRan;
        private final List<BlockPos> targetPositions = Lists.newArrayList();

        public DealerVillagerTask() {
            super(ImmutableMap.of(
                    MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT,
                    MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT,
                    MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleState.VALUE_PRESENT
            ));
        }

        @Override
        protected boolean shouldRun(ServerWorld world, VillagerEntity entity) {
            if (!world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                return false;
            }

            if (entity.getVillagerData().getProfession() != PSTradeOffers.DRUG_DEALER_PROFESSION) {
                return false;
            }

            BlockPos.Mutable mutable = entity.getBlockPos().mutableCopy();
            targetPositions.clear();

            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        mutable.set(
                                entity.getX() + i,
                                entity.getY() + j,
                                entity.getZ() + k
                        );
                        if (!isSuitableTarget(mutable, world)) {
                            continue;
                        }
                        targetPositions.add(mutable.toImmutable());
                    }
                }
            }

            return (currentTarget = chooseRandomTarget(world)) != null;
        }

        @Nullable
        private BlockPos chooseRandomTarget(ServerWorld world) {
            return targetPositions.isEmpty() ? null : targetPositions.get(world.getRandom().nextInt(targetPositions.size()));
        }

        private boolean isSuitableTarget(BlockPos pos, ServerWorld world) {
            BlockState blockState = world.getBlockState(pos);
            return  blockState.getBlock() instanceof CropBlock crop
                    && crop.isMature(blockState)
                    || blockState.isAir() && world.getBlockState(pos.down()).getBlock() instanceof FarmlandBlock;
        }

        @Override
        protected void run(ServerWorld world, VillagerEntity entity, long l) {
            if (l > nextResponseTime && currentTarget != null) {
                entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(currentTarget));
                entity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(currentTarget), 0.5f, 1));
            }
        }

        @Override
        protected void finishRunning(ServerWorld world, VillagerEntity entity, long l) {
            entity.getBrain().forget(MemoryModuleType.LOOK_TARGET);
            entity.getBrain().forget(MemoryModuleType.WALK_TARGET);
            ticksRan = 0;
            nextResponseTime = l + 40L;
        }

        @Override
        protected void keepRunning(ServerWorld world, VillagerEntity entity, long l) {
            if (currentTarget != null && !currentTarget.isWithinDistance(entity.getPos(), 1)) {
                return;
            }

            if (currentTarget != null && l > nextResponseTime) {
                BlockState state = world.getBlockState(currentTarget);

                if (state.getBlock() instanceof CropBlock crop && crop.isMature(state)) {
                    world.breakBlock(currentTarget, true, entity);
                }

                if (state.isAir() && world.getBlockState(currentTarget.down()).getBlock() instanceof FarmlandBlock) {
                    RecipeUtils.consume(entity.getInventory(), i -> i.getItem() instanceof BlockItem && i.isIn(PSTags.Items.DRUG_CROP_SEEDS)).ifPresent(stack -> {
                        BlockState plantedState = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
                        world.setBlockState(currentTarget, plantedState);
                        world.emitGameEvent(GameEvent.BLOCK_PLACE, currentTarget, GameEvent.Emitter.of(entity, plantedState));
                        world.playSound(null,
                                currentTarget.getX(),
                                currentTarget.getY(),
                                currentTarget.getZ(),
                                SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1, 1);
                    });
                }

                if (state.getBlock() instanceof CropBlock crop && !crop.isMature(state)) {
                    targetPositions.remove(currentTarget);
                    currentTarget = chooseRandomTarget(world);
                    if (currentTarget != null) {
                        nextResponseTime = l + 20L;
                        entity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(currentTarget), 0.5f, 1));
                        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(currentTarget));
                    }
                }
            }

            ++ticksRan;
        }

        @Override
        protected boolean shouldKeepRunning(ServerWorld world, VillagerEntity entity, long l) {
            return ticksRan < MAX_RUN_TIME;
        }
    }
}
