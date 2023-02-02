/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.recipe.FillDrinkContainerRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.*;

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

    @Override
    protected void onProcessCompleted(ServerWorld world, Resovoir tank, ItemStack solids) {
        if (!solids.isEmpty()) {
            tank.clear();
            solidContents = solids;
        }

        super.onProcessCompleted(world, tank, solids);
    }

    public TypedActionResult<ItemStack> depositIngredient(ItemStack stack) {
        if (!FluidContainer.of(stack).getFluid(stack).isEmpty()) {
            Resovoir tank = getTank(Direction.UP);
            if (tank.getLevel() < tank.getCapacity()) {
                return TypedActionResult.success(getTank(Direction.UP).deposit(stack));
            }
            return TypedActionResult.fail(stack);
        }

        if (isValidIngredient(stack)) {
            suppliedIngredients.computeInt(stack.getItem(), (s, i) -> i == null ? 1 : (i + 1));
            checkIngredients();
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
        if (suppliedIngredients.isEmpty()) {
            return;
        }

        expectedRecipe = expectedRecipe.filter(this::testRecipe);


        List<FillDrinkContainerRecipe> matchedRecipes = world.getRecipeManager().listAllOfType(RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.getSerializer() == PSRecipes.FILL_DRINK_CONTAINER)
                .map(recipe -> (FillDrinkContainerRecipe)recipe)
                .filter(this::testRecipe)
                .toList();

        expectedRecipe = expectedRecipe.or(() -> matchedRecipes.stream().findFirst());

        if (expectedRecipe.isEmpty()) {
            onCraftingFailed();
            return;
        }

        if (matchedRecipes.size() == 1) {
            expectedRecipe.ifPresentOrElse(this::onCraftingSucceeded, this::onCraftingFailed);
        }
    }

    private boolean testRecipe(FillDrinkContainerRecipe recipe) {
        final List<Ingredient> expectedInputs = new ArrayList<>(recipe.getIngredients());
        final Object2IntMap<Item> unmatchedInputs = new Object2IntOpenHashMap<>(suppliedIngredients);

        for (Item item : suppliedIngredients.keySet()) {
            ItemStack stack = item.getDefaultStack();

            if (expectedInputs.isEmpty()) {
                // fail match as the supplied ingredients exceeds the expected inputs
                return false;
            }

            for (Ingredient ingredient : expectedInputs) {
                if (ingredient.test(stack)) {
                    expectedInputs.remove(ingredient);
                    unmatchedInputs.computeInt(item, (s, i) -> i <= 1 ? null : i - 1);

                    if (unmatchedInputs.isEmpty()) {
                        // succeed if all supplied inputs are matched.
                        // The recipe might be expecting more additional inputs.
                        // We don't actually care. Crafting is done when only one recipe fits our current selection.
                        return true;
                    }

                    break;
                }
            }
        }

        return unmatchedInputs.isEmpty();
    }

    private void onCraftingSucceeded(FillDrinkContainerRecipe recipe) {
        suppliedIngredients.clear();
        getTank(Direction.UP).getContents()
            .withFluid(recipe.getOutputFluid().fluid())
            .withAttributes(recipe.getOutputFluid().attributes());
    }

    private void onCraftingFailed() {
        suppliedIngredients.clear();
        getTank(Direction.UP).getContents()
            .withFluid(PSFluids.SLURRY);
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
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        solidContents = compound.contains("solidContents", NbtElement.COMPOUND_TYPE)
                ? ItemStack.fromNbt(compound.getCompound("solidContents"))
                : ItemStack.EMPTY;
    }
}
