/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.recipe.DryingRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap.Type;
import java.util.ArrayList;
import java.util.List;

public class DryingTableBlockEntity extends BlockEntityWithInventory {
    private static final int[] INPUT_SLOTS = new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private static final int[] OUTPUT_SLOTS = new int[]{ 0 };

    public int ticksAlive;

    public float heatRatio;
    public float dryingProgress;

    public ItemStack plannedResult = ItemStack.EMPTY;

    public DryingTableBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.DRYING_TABLE, pos, state, 10);
    }

    public static void serverTick(ServerWorld world, BlockPos pos, BlockState state, DryingTableBlockEntity entity) {
        entity.tick(world);
    }

    public void tick(ServerWorld world) {

        ticksAlive++;
        float progress = dryingProgress;
        float oldHeat = heatRatio;

        if (ticksAlive % 30 == 5) {
            float l = world.getLightLevel(pos) / 15F;
            float h = !world.isAir(pos) ? world.getBiome(pos).value().getTemperature() * 0.75F + 0.25F : 0;
            heatRatio = MathHelper.clamp((l * l * h) * (l * l * h), 0, 1);

            if (world.getRainGradient(1) > 0 && world.getTopPosition(Type.MOTION_BLOCKING, pos).getY() == pos.getY() + 1) {
                dryingProgress = 0;
            }
        }

        if (!plannedResult.isEmpty()) {
            dryingProgress += heatRatio / (
                    world.getBlockState(pos).isOf(PSBlocks.IRON_DRYING_TABLE)
                    ? Psychedelicraft.getConfig().balancing.ironDryingTableTickDuration
                    : Psychedelicraft.getConfig().balancing.dryingTableTickDuration
            );

            if (dryingProgress >= 1) {
                endDryingProcess();
            }
        } else {
            dryingProgress = 0;
        }

        if (progress != dryingProgress || oldHeat != heatRatio) {
            world.getChunkManager().markForUpdate(pos);
        }
        markDirty();
    }

    public ItemStack getResult() {
        List<ItemStack> src = new ArrayList<>(size() - 1);
        for (int i = 1; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                src.add(stack);
            }
        }

        ItemStack itemStack = DryingRegistry.dryingResult(src);
        return getStack(0).isEmpty() || ItemStack.areEqual(itemStack, getStack(0)) ? itemStack : ItemStack.EMPTY;
    }

    public void endDryingProcess() {
        dryingProgress = 0;

        ItemStack result = getResult();

        for (int i = 1; i < size(); i++) {
            setStack(i, ItemStack.EMPTY);
        }

        if (getStack(0).isEmpty()) {
            setStack(0, result);
        } else {
            getStack(0).increment(result.getCount());
        }
        onInventoryChanged();
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.put("plannedResult", plannedResult.writeNbt(new NbtCompound()));
        compound.putFloat("heatRatio", heatRatio);
        compound.putFloat("dryingProgress", dryingProgress);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        plannedResult = ItemStack.fromNbt(compound.getCompound("plannedResult"));
        heatRatio = compound.getFloat("heatRatio");
        dryingProgress = compound.getFloat("dryingProgress");
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public void onInventoryChanged() {
        plannedResult = getResult();
        dryingProgress = 0;

        super.onInventoryChanged();
    }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        return direction == Direction.UP ? OUTPUT_SLOTS : INPUT_SLOTS;
    }
}
