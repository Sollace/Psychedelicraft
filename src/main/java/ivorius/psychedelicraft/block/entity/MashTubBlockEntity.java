/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.ParticleHelper;
import ivorius.psychedelicraft.block.MashTubBlock;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.recipe.ItemMound;
import ivorius.psychedelicraft.recipe.MashingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.util.MathUtils;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Created by lukas on 27.10.14.
 */
public class MashTubBlockEntity extends FluidProcessingBlockEntity {
    public ItemStack solidContents = ItemStack.EMPTY;
    private ItemFluids auxiliaryFluids = ItemFluids.EMPTY;

    private Optional<Stew> currentStew = Optional.empty();

    private final ItemMound suppliedIngredients = new ItemMound();

    public MashTubBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.MASH_TUB, pos, state, FluidVolumes.VAT);
    }

    public ItemMound getSuppliedIngredients() {
        return suppliedIngredients;
    }

    public ItemFluids getAuxiliaryFluids() {
        return auxiliaryFluids;
    }

    @Override
    public void accept(ItemStack stack) {
        if (!solidContents.isEmpty()) {
            if (ItemStack.canCombine(solidContents, stack)) {
                int maxToMove = Math.min(stack.getCount(), solidContents.getMaxCount() - solidContents.getCount());
                if (maxToMove > 0) {
                    stack.decrement(maxToMove);
                    solidContents.increment(maxToMove);
                    world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_READY, SoundCategory.BLOCKS);
                    return;
                }
            }

            Block.dropStack(world, pos, solidContents);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS);
        }
        solidContents = stack;
        world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_READY, SoundCategory.BLOCKS);
    }

    @Override
    protected void onFluidRejected(ServerWorld world, ItemFluids fluids) {
        if (auxiliaryFluids.isEmpty()) {
            auxiliaryFluids = fluids;
        } else {
            if (auxiliaryFluids.canCombine(fluids)) {
                int spaceInTank = (int)(getPrimaryTank().getCapacity() - getPrimaryTank().getAmount());
                int maxInserted = Math.min(fluids.amount(), spaceInTank - auxiliaryFluids.amount());
                if (maxInserted > 0) {
                    auxiliaryFluids = auxiliaryFluids.ofAmount(auxiliaryFluids.amount() + maxInserted);
                    if (maxInserted >= fluids.amount()) {
                        return;
                    }
                    fluids = fluids.ofAmount(fluids.amount() - maxInserted);
                }
            }

            super.onFluidRejected(world, fluids);
        }
    }

    @Override
    public Processable.ProcessType getProcessType() {
        return Processable.ProcessType.FERMENT;
    }

    @Override
    public void tick(ServerWorld world) {
        if (isAcceptingIngredients()) {
            Vec3d center = getPos().toCenterPos();
            Box box = Box.of(center, 1.5, 0.5, 1.5);
            for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, box, EntityPredicates.VALID_ENTITY)) {
                ItemStack stack = item.getStack();
                if (isValidIngredient(stack)) {
                    suppliedIngredients.addStack(stack);
                    beginStewing();
                    markForUpdate();
                    spawnBubbles(20, 0, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
                    getWorld().playSound(null, getPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1, 1);
                    item.discard();
                }
            }
        }

        if (currentStew.isEmpty()) {
            super.tick(world);
        }
        currentStew = currentStew.filter(Stew::tick);
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int difference) {
        if (world == null) {
            return;
        }
        if (resovoir.getContents().isEmpty() && !auxiliaryFluids.isEmpty()) {
            resovoir.setContents(auxiliaryFluids, false);
            auxiliaryFluids = ItemFluids.EMPTY;
        }
        super.onLevelChange(resovoir, difference);

        int luminance = resovoir.getContents().fluid().getPhysical().getDefaultState().getBlockState().getLuminance();

        int currentLuminance = getCachedState().get(MashTubBlock.LIGHT);
        if (luminance != currentLuminance) {
            world.setBlockState(getPos(), getCachedState().with(MashTubBlock.LIGHT, luminance));
        }

        if (difference > 0) {
            setTimeProcessed(0);
            setRepeatCount(0);
        }

        if (resovoir.getContents().isEmpty() && !suppliedIngredients.isEmpty()) {
            for (ItemStack stack : suppliedIngredients.convertToItemStacks()) {
                Block.dropStack(getWorld(), getPos(), stack);
            }
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS);
            suppliedIngredients.clear();
        }
    }

    @Override
    public void clientTick(World world) {
        super.clientTick(world);
        if (!suppliedIngredients.isEmpty() && world.getRandom().nextFloat() < 0.33F && world.getTime() % 3 == 0) {
            spawnBubbles(1 + (int)(suppliedIngredients.size() * 1.5), 0, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
        }
    }

    public TypedActionResult<ItemStack> interactWithItem(ItemStack stack) {

        if (!currentStew.isEmpty()) {
            return TypedActionResult.fail(stack);
        }

        if (getWorld().isClient) {
            return TypedActionResult.success(stack);
        }

        if (FluidCapacity.get(stack) > 0) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
            Resovoir tank = getPrimaryTank();
            FluidVariant variant = t.fluids().toVariant();
            if (!t.fluids().isEmpty()) {
                if (tank.deposit(t, t.capacity()) > 0) {
                    getWorld().playSound(null, getPos(), FluidVariantAttributes.getEmptySound(variant), SoundCategory.BLOCKS, 1, 1);
                    return TypedActionResult.success(t.toItemStack());
                }
            } else {
                if (tank.withdraw(t, t.capacity()) > 0) {
                    getWorld().playSound(null, getPos(), FluidVariantAttributes.getFillSound(variant), SoundCategory.BLOCKS, 1, 1);
                    return TypedActionResult.success(t.toItemStack());
                }
            }

            return TypedActionResult.fail(stack);
        }

        if (isAcceptingIngredients() && acceptsItem(stack)) {
            ItemStack consumed = stack.split(1);
            suppliedIngredients.add(consumed.getItem(), 1);
            beginStewing();
            markForUpdate();
            spawnBubbles(20, 0, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
            getWorld().playSound(null, getPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1, 1);
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    public boolean acceptsItem(ItemStack stack) {
        return FluidCapacity.get(stack) == 0;
    }

    public boolean isAcceptingIngredients() {
        return currentStew.isEmpty() && !getPrimaryTank().getContents().isEmpty();
    }

    public boolean isValidIngredient(ItemStack stack) {
        return acceptsItem(stack)
            && (world.getRecipeManager()
                .listAllOfType(PSRecipes.MASHING_TYPE).stream()
                .filter(recipe -> recipe.value().baseFluid().test(getPrimaryTank().getContents()))
                .flatMap(recipe -> recipe.value().getIngredients().stream())
                .anyMatch(i -> i.test(stack)));
    }

    public void beginStewing() {
        if (suppliedIngredients.isEmpty() || getWorld().isClient()) {
            return;
        }

        currentStew = world.getRecipeManager().getFirstMatch(
                PSRecipes.MASHING_TYPE,
                new MashingRecipe.Input(getPrimaryTank().getContents(), solidContents, suppliedIngredients),
                world
        ).map(Stew::new);

        if (currentStew.isEmpty() && suppliedIngredients.countMatches(stack -> !isValidIngredient(stack)) >= 8) {
            suppliedIngredients.clear();
            getPrimaryTank().setContents(PSFluids.SLURRY.getDefaultStack(getPrimaryTank().getContents().amount()));
            markDirty();
        }
    }

    public Stream<MashingRecipe> getPotentialMatches() {
        var input = new MashingRecipe.Input(getPrimaryTank().getContents(), solidContents, suppliedIngredients);
        return world.getRecipeManager().listAllOfType(PSRecipes.MASHING_TYPE).stream().map(RecipeEntry::value).filter(recipe -> {
            return recipe.matchesPartially(input, world);
        });
    }

    private void spawnBubbles(int count, float spread, SoundEvent sound) {
        Random random = getWorld().getRandom();
        Vec3d center = ParticleHelper.apply(getPos().toCenterPos(), x -> random.nextTriangular(x, 0.25));

        Resovoir tank = getPrimaryTank();
        ParticleHelper.spawnParticles(getWorld(),
                new DrugDustParticleEffect(PSParticles.BUBBLE, MathUtils.unpackRgb(tank.getContents().fluid().getColor(tank.getContents())), 1F),
                () -> ParticleHelper.apply(center, x -> random.nextTriangular(x, 0.5 + spread)).add(0, 0.5, 0),
                Suppliers.ofInstance(new Vec3d(
                        random.nextTriangular(0, 0.125),
                        random.nextTriangular(0.1, 0.125),
                        random.nextTriangular(0, 0.125)
                )),
                count
        );

        if (getWorld() instanceof ServerWorld) {
            getWorld().playSound(null, getPos(), sound, SoundCategory.BLOCKS,
                    0.5F + getWorld().getRandom().nextFloat(),
                    0.3F + getWorld().getRandom().nextFloat()
            );
        } else {
            getWorld().playSoundAtBlockCenter(getPos(), sound, SoundCategory.BLOCKS,
                    0.5F + getWorld().getRandom().nextFloat(),
                    0.3F + getWorld().getRandom().nextFloat(), true);
        }
    }

    @Deprecated
    @Override
    public List<ItemStack> getDroppedStacks(ItemStack container) {
        List<ItemStack> ingredients = new ArrayList<>(this.suppliedIngredients.convertToItemStacks());
        if (!solidContents.isEmpty()) {
            ingredients.add(solidContents);
        }
        return ingredients;
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (!currentStew.isEmpty()) {
            return PipeInsertable.reject(fluids);
        }
        return super.tryInsert(world, state, pos, direction, fluids);
    }

    @Override
    public Optional<PipeFluids> tryExtract(ServerWorld world, BlockState state, BlockPos pos, Direction direction) {
        if (!currentStew.isEmpty()) {
            return Optional.empty();
        }
        return super.tryExtract(world, state, pos, direction);
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        if (!solidContents.isEmpty()) {
            compound.put("solidContents", solidContents.writeNbt(new NbtCompound()));
        }
        compound.put("suppliedIngredients", suppliedIngredients.toNbt());
        compound.put("auxiliaryFluids", auxiliaryFluids.encode());
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        solidContents = compound.contains("solidContents", NbtElement.COMPOUND_TYPE)
                ? ItemStack.fromNbt(compound.getCompound("solidContents"))
                : ItemStack.EMPTY;
        auxiliaryFluids = compound.contains("auxiliaryFluids", NbtElement.COMPOUND_TYPE)
                ? ItemFluids.decode(compound.get("auxiliaryFluids"))
                : ItemFluids.EMPTY;
        suppliedIngredients.fromNbt(compound.getCompound("suppliedIngredients"));
    }

    class Stew implements NbtSerialisable {
        @Nullable
        private Identifier recipe;
        private int stewTime;

        public Stew(RecipeEntry<MashingRecipe> recipe) {
            this.recipe = recipe.id();
            this.stewTime = (2 + world.getRandom().nextInt(4)) + recipe.value().stewTime();
        }

        public boolean tick() {
            if (recipe == null) {
                markDirty();
                return false;
            }

            if (world.getTime() % 30 == 0) {
                spawnBubbles(9, 0.5F, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE);
                markDirty();
                if (--stewTime <= 0) {
                    if (world.getRecipeManager().get(recipe).map(RecipeEntry::value).orElse(null) instanceof MashingRecipe recipe) {
                        var input = new MashingRecipe.Input(getPrimaryTank().getContents(), solidContents, suppliedIngredients);
                        getPrimaryTank().setContents(recipe.result().ofAmount(getPrimaryTank().getContents().amount()));

                        recipe.getRemainder(input).forEach(stack -> {
                           Block.dropStack(world, getPos(), stack);
                        });
                    }
                    suppliedIngredients.clear();

                    return false;
                }
            }

            return true;
        }

        @Override
        public void toNbt(NbtCompound compound) {
            compound.putInt("stewTime", stewTime);
            compound.putString("recipe", recipe.toString());
        }

        @Override
        public void fromNbt(NbtCompound compound) {
            stewTime = compound.getInt("stewTime");
            recipe = Identifier.validate(compound.getString("recipe")).result().orElse(null);
        }
    }
}
