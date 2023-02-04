/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;

import com.google.common.base.Suppliers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ivorius.psychedelicraft.ParticleHelper;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.recipe.FillDrinkContainerRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 27.10.14.
 */
public class MashTubBlockEntity extends FluidProcessingBlockEntity {
    public ItemStack solidContents = ItemStack.EMPTY;

    private Optional<FillDrinkContainerRecipe> expectedRecipe = Optional.empty();

    private final Object2IntMap<Item> suppliedIngredients = new Object2IntOpenHashMap<>();

    public MashTubBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.MASH_TUB, pos, state, FluidVolumes.MASH_TUB, Processable.ProcessType.FERMENT);
    }

    @Override
    protected boolean isOpen() {
        return true;
    }

    @Override
    protected FluidContainer getContainerType() {
        return PSItems.MASH_TUB;
    }

    public Object2IntMap<Item> getSuppliedIngredients() {
        return suppliedIngredients;
    }

    @Override
    protected void onProcessCompleted(ServerWorld world, Resovoir tank, ItemStack solids) {
        if (!solids.isEmpty()) {
            tank.clear();
            solidContents = solids;
        }

        super.onProcessCompleted(world, tank, solids);
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);
        if (!suppliedIngredients.isEmpty() && world.getTime() % 10 == 0) {
            spawnBubbles(1 + (int)(suppliedIngredients.size() * 1.5));
        }
    }

    public TypedActionResult<ItemStack> depositIngredient(ItemStack stack) {
        if (!FluidContainer.of(stack).getFluid(stack).isEmpty()) {
            Resovoir tank = getTank(Direction.UP);
            if (tank.getLevel() < tank.getCapacity()) {
                getWorld().playSound(null, getPos(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1, 1);
                onIdle(getTank(Direction.UP));
                return TypedActionResult.success(getTank(Direction.UP).deposit(stack));
            }
            return TypedActionResult.fail(stack);
        }

        if (isValidIngredient(stack)) {
            suppliedIngredients.computeInt(stack.getItem(), (s, i) -> i == null ? 1 : (i + 1));
            checkIngredients();
            spawnBubbles(20);
            getWorld().playSound(null, getPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1, 1);
            onIdle(getTank(Direction.UP));
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(stack);
    }

    public boolean isValidIngredient(ItemStack stack) {
        return FluidContainer.of(stack, null) == null
            && getTank(Direction.UP).getFluidType() == SimpleFluid.forVanilla(Fluids.WATER)
            && world.getRecipeManager().listAllOfType(RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.getSerializer() == PSRecipes.FILL_DRINK_CONTAINER)
                .map(recipe -> (FillDrinkContainerRecipe)recipe)
                .flatMap(recipe -> recipe.getIngredients().stream())
                .anyMatch(ingredient -> ingredient.test(stack));
    }

    private void checkIngredients() {
        if (suppliedIngredients.isEmpty() || getWorld().isClient()) {
            return;
        }

        var expectedRecipeMatchPair = expectedRecipe.map(recipe -> Map.entry(recipe, recipe.matchPartially(suppliedIngredients)));
        var matchedRecipes = world.getRecipeManager().listAllOfType(RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.getSerializer() == PSRecipes.FILL_DRINK_CONTAINER)
                .map(recipe -> (FillDrinkContainerRecipe)recipe)
                .map(recipe -> Map.entry(recipe, recipe.matchPartially(suppliedIngredients)))
                .filter(pair -> pair.getValue().isMatch())
                .toList();

        expectedRecipeMatchPair = expectedRecipeMatchPair.or(() -> matchedRecipes.stream().findFirst());
        expectedRecipe = expectedRecipeMatchPair.filter(pair -> pair.getValue().isMatch()).map(Map.Entry::getKey);

        if (expectedRecipeMatchPair.isEmpty()) {
            onCraftingFailed();
            return;
        }

        if (matchedRecipes.size() == 1) {
            expectedRecipeMatchPair
                .filter(pair -> pair.getValue().isCraftable())
                .map(Map.Entry::getKey)
                .ifPresent(this::onCraftingSucceeded);
        }
    }

    private void onCraftingSucceeded(FillDrinkContainerRecipe recipe) {
        suppliedIngredients.clear();
        getTank(Direction.UP).getContents()
            .withFluid(recipe.getOutputFluid().fluid())
            .withAttributes(recipe.getOutputFluid().attributes());
        onIdle(getTank(Direction.UP));
    }

    private void onCraftingFailed() {
        suppliedIngredients.clear();
        getTank(Direction.UP).getContents()
            .withFluid(PSFluids.SLURRY);
        getWorld().playSound(null, getPos(), SoundEvents.BLOCK_MUD_BREAK, SoundCategory.BLOCKS, 1, 1);
        spawnBubbles(90);
        onIdle(getTank(Direction.UP));
    }

    private void spawnBubbles(int count) {
        Random random = getWorld().getRandom();
        Vec3d center = ParticleHelper.apply(getPos().toCenterPos(), x -> random.nextTriangular(x, 0.25));
        ParticleHelper.spawnParticles(getWorld(), ParticleTypes.BUBBLE_POP,
                () -> ParticleHelper.apply(center, x -> random.nextTriangular(x, 0.5)).add(0, 0.5, 0),
                Suppliers.ofInstance(new Vec3d(
                        random.nextTriangular(0, 0.125),
                        random.nextTriangular(0.5, 0.125),
                        random.nextTriangular(0, 0.125)
                )),
                count
        );
    }

    @Override
    public void onDestroyed(ServerWorld world) {
        super.onDestroyed(world);
        if (!solidContents.isEmpty()) {
            Block.dropStack(world, pos, solidContents);
        }
    }

    @Override
    public void onDrain(Resovoir resovoir) {
        if (!solidContents.isEmpty() && resovoir.isEmpty()) {
            setTimeProcessed(0);
        }
        onIdle(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        if (!solidContents.isEmpty()) {
            super.onFill(resovoir, amountFilled);
        } else {
            onIdle(resovoir);
        }
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        if (!solidContents.isEmpty()) {
            compound.put("solidContents", solidContents.writeNbt(new NbtCompound()));
        }
        NbtCompound suppliedIngredsTag = new NbtCompound();
        suppliedIngredients.forEach((item, count) -> {
            suppliedIngredsTag.putInt(Registries.ITEM.getId(item).toString(), count);
        });
        compound.put("suppliedIngredients", suppliedIngredsTag);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        solidContents = compound.contains("solidContents", NbtElement.COMPOUND_TYPE)
                ? ItemStack.fromNbt(compound.getCompound("solidContents"))
                : ItemStack.EMPTY;
        NbtCompound suppliedIngredsTag = compound.getCompound("suppliedIngredients");
        suppliedIngredients.clear();
        suppliedIngredsTag.getKeys().forEach(key -> {
            Optional.ofNullable(Identifier.tryParse(key)).map(Registries.ITEM::get).filter(Objects::nonNull).ifPresent(item -> {
                suppliedIngredients.put(item, suppliedIngredsTag.getInt(key));
            });
        });
    }
}
