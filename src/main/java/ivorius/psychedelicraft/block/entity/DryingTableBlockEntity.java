/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.Optional;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.recipe.DryingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;

public class DryingTableBlockEntity extends BlockEntityWithInventory {
    public static final int OUTPUT_SLOT_INDEX = 0;
    private static final int[] INPUT_SLOTS = new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private static final int[] OUTPUT_SLOTS = new int[]{ OUTPUT_SLOT_INDEX };

    public static long getCookingTime(float recipeDifficulty, boolean ironTable) {
        return (long)(recipeDifficulty * (ironTable
                ? Psychedelicraft.getConfig().balancing.ironDryingTableTickDuration
                : Psychedelicraft.getConfig().balancing.dryingTableTickDuration));
    }

    private int age;

    private float heat;
    private float dryingProgress;

    private long cookingTime;
    private Optional<Identifier> currentRecipe = Optional.empty();

    public final PropertyDelegate propertyDelegate = new PropertyDelegate(){
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 ->  (int)(heat * 1000);
                case 1 -> (int)dryingProgress;
                case 2 -> (int)cookingTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: {
                    heat = value / 1000F;
                    break;
                }
                case 1: {
                    dryingProgress = value;
                    break;
                }
                case 2: {
                    cookingTime = value;
                    break;
                }
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public DryingTableBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.DRYING_TABLE, pos, state, 10);
    }

    public static void serverTick(ServerWorld world, BlockPos pos, BlockState state, DryingTableBlockEntity entity) {
        entity.tick(world);
    }

    public float getHeatRatio() {
        return heat;
    }

    public float getDryingProgress() {
        return cookingTime == 0 ? 0 : dryingProgress / cookingTime;
    }

    private float calculateSunStrength() {
        float l = world.getLightLevel(pos) / 15F;
        float h = !world.isAir(pos) ? world.getBiome(pos).value().getTemperature() * 0.75F + 0.25F : 0;
        return MathHelper.clamp((l * l * h) * (l * l * h), 0, 1);
    }

    public void tick(ServerWorld world) {
        float oldProgress = dryingProgress;
        float oldHeat = heat;

        if (++age % 30 == 0) {
            heat = calculateSunStrength();

            if (world.getRainGradient(1) > 0 && world.isSkyVisible(pos)) {
               // dryingProgress = heat;
            }
        }

        if (!(world.getRainGradient(1) > 0 && world.isSkyVisible(pos))) {
            if (currentRecipe.isPresent() && cookingTime > 0) {
                dryingProgress += heat;

                int delta = (int)((1 - MathHelper.clamp(dryingProgress / cookingTime, 0, 1)) * 100);

                if (delta == 0 || world.getTime() % 30 == 0) {
                    for (int i = 0; i < 5; i++) {
                        world.spawnParticles(ParticleTypes.SMOKE,
                            pos.getX() + world.getRandom().nextTriangular(0.5F, 0.5F),
                            pos.getY() + 0.6F,
                            pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.5F),
                            2, 0, 0, 0, 0);
                    }
                }

                if (dryingProgress >= cookingTime) {
                    DryingRecipe.Input input = new DryingRecipe.Input(getStack(OUTPUT_SLOT_INDEX), getStacks().skip(1).toList());
                    world.getRecipeManager().getFirstMatch(PSRecipes.DRYING_TYPE, input, world, currentRecipe.get()).ifPresent(recipe -> {
                        craft(recipe.value(), input);
                    });
                    currentRecipe = Optional.empty();
                    dryingProgress = 0;
                    cookingTime = 0;

                    world.getChunkManager().markForUpdate(pos);
                }
            } else {
                dryingProgress = 0;
            }
        }

        if (!MathHelper.approximatelyEquals(oldProgress, dryingProgress) || !MathHelper.approximatelyEquals(oldHeat, heat)) {
            world.updateComparators(pos, getCachedState().getBlock());
            world.updateNeighbors(pos, getCachedState().getBlock());
            world.getChunkManager().markForUpdate(pos);
            markDirty();
        }
    }

    private void craft(DryingRecipe recipe, DryingRecipe.Input input) {
        ItemStack result = recipe.craft(input, getWorld().getRegistryManager());
        clear();
        DefaultedList<ItemStack> remainder = recipe.getRemainder(input);
        for (int i = 0; i < remainder.size(); i++) {
            setStack(i + 1, remainder.get(i));
        }

        if (!input.result().isEmpty()) {
            result.increment(input.result().getCount());
        }

        setStack(OUTPUT_SLOT_INDEX, result);
    }

    @Override
    public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
        super.writeNbt(compound, lookup);
        currentRecipe.ifPresent(r -> {
            compound.putString("currentRecipe", r.toString());
        });
        compound.putFloat("heatRatio", heat);
        compound.putLong("cookingTime", cookingTime);
        compound.putFloat("dryingProgress", dryingProgress);
    }

    @Override
    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        super.readNbt(compound, lookup);
        currentRecipe = Identifier.validate(compound.getString("currentRecipe")).result();
        heat = compound.getFloat("heatRatio");
        cookingTime = compound.getLong("cookingTime");
        dryingProgress = compound.getFloat("dryingProgress");
    }

    @Override
    public void onInventoryChanged() {
        if (!getWorld().isClient) {
            getWorld()
                    .getRecipeManager()
                    .getFirstMatch(PSRecipes.DRYING_TYPE, new DryingRecipe.Input(getStack(OUTPUT_SLOT_INDEX), getStacks().skip(1).toList()), getWorld())
                    .ifPresentOrElse(recipe -> {
                        currentRecipe = Optional.of(recipe.id());
                        cookingTime = getCookingTime(recipe.value().cookTime(), getCachedState().isOf(PSBlocks.IRON_DRYING_TABLE));
                    }, () -> {
                        currentRecipe = Optional.empty();
                        cookingTime = 0;
                    });
            dryingProgress = 0;
            heat = calculateSunStrength();
        }

        super.onInventoryChanged();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        return slot != OUTPUT_SLOT_INDEX && getStack(slot).isEmpty();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT_INDEX;
    }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        return direction == Direction.DOWN ? OUTPUT_SLOTS : INPUT_SLOTS;
    }
}
