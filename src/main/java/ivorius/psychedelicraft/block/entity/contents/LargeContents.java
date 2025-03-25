package ivorius.psychedelicraft.block.entity.contents;

import java.util.ArrayList;
import java.util.List;
import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.block.ShapeUtil;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity.Contents;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.ItemFluidsMixture;
import ivorius.psychedelicraft.recipe.BunsenBurnerRecipe;
import ivorius.psychedelicraft.recipe.FluidMound;
import ivorius.psychedelicraft.recipe.ItemMound;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class LargeContents extends SmallContents {
    public static final Identifier ID = Psychedelicraft.id("large");
    static final int MAX_INGREDIENTS = 4;
    static final int[] CONTAINER_SLOT_ID = {0};
    static final int[] INGREDIENT_SLOT_ID = {1, 2, 3, 4};
    static final VoxelShape SHAPE = ShapeUtil.createCenteredShape(5, 18, 5);

    private ItemMound ingredients = new ItemMound();

    public LargeContents(BurnerBlockEntity entity, int capacity, ItemStack stack) {
        super(entity, capacity, stack);
    }

    @Override
    public VoxelShape getOutlineShape() {
        return SHAPE;
    }

    @Override
    protected void loadContents(ItemStack stack) {
        ItemFluids.allOf(stack).forEach(this::deposit);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public ItemMound getIngredients() {
        return ingredients;
    }

    @Override
    public TypedActionResult<Contents> interact(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        TypedActionResult<Contents> result = super.interact(stack, player, hand, side);
        if (result.getResult().isAccepted()) {
            return result;
        }

        if (ingredients.size() < MAX_INGREDIENTS
                && ingredients.getCounts().getInt(stack.getItem()) < 5) {
            if (isValidIngredient(stack)) {
                if (!player.getWorld().isClient) {
                    ingredients.addStack(stack.splitUnlessCreative(1, player));
                    player.setStackInHand(hand, stack);
                    entity.playSound(null, PSSounds.BLOCK_BUNSEN_BURNER_FILL);
                }
                return TypedActionResult.success(this);
            }
        }

        return TypedActionResult.fail(this);
    }

    @Override
    protected TypedActionResult<Contents> interactWithFluidVessel(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        if (!ItemFluids.of(stack).isEmpty()) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
            if (deposit(t)) {
                entity.playSound(player, SoundEvents.ITEM_BOTTLE_EMPTY);
                if (!player.getWorld().isClient) {
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
                }
                return TypedActionResult.success(this);
            }
            return TypedActionResult.fail(this);
        }

        ItemFluidsMixture mixture = ItemFluidsMixture.of(stack);
        if (!mixture.isEmpty()) {
            if (!player.getWorld().isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, ItemFluidsMixture.set(stack.copyWithCount(1), mixture.fluids().stream().map(this::deposit).toList())));
            }
            entity.playSound(player, SoundEvents.ITEM_BOTTLE_EMPTY);
            return TypedActionResult.success(this);
        }

        Resovoir tank = player.isSneaking() ? getLastTank() : getPrimaryTank();
        if (!tank.getContents().isEmpty()) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copyWithCount(1));
            if (tank.withdraw(t, FluidCapacity.get(stack)) > 0) {
                if (!player.getWorld().isClient) {
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
                }
                entity.playSound(player, SoundEvents.ITEM_BOTTLE_FILL);
                return TypedActionResult.success(this);
            }
        }

        return TypedActionResult.fail(this);
    }

    protected boolean deposit(ItemFluids.Transaction t) {
        synchronized (auxiliaryTanks) {
            int maxToInsert = Math.max(0, capacity - getTotalFluidVolume());
            int transferred = 0;
            for (Resovoir auxTank : auxiliaryTanks) {
                transferred = auxTank.deposit(t, maxToInsert);
                if (transferred > 0) {
                    return true;
                }
            }
            if (auxiliaryTanks.size() < 4) {
                Resovoir auxTank = createTank();
                transferred = auxTank.deposit(t, maxToInsert);
                if (transferred > 0) {
                    auxiliaryTanks.add(auxTank);
                    entity.markDirty();
                    return true;
                }
            }
            return false;
        }
    }

    protected ItemFluids deposit(ItemFluids stack) {
        synchronized (auxiliaryTanks) {
            if (stack.isEmpty()) {
                return stack;
            }
            int maxToInsert = Math.min(stack.amount(), Math.max(0, capacity - getTotalFluidVolume()));
            ItemFluids toInsert = stack.ofAmount(maxToInsert);
            int transferred = 0;
            for (Resovoir auxTank : auxiliaryTanks) {
                transferred = auxTank.deposit(toInsert);
                if (transferred > 0) {
                    return stack.ofAmount(stack.amount() - transferred);
                }
            }
            if (auxiliaryTanks.size() < 4) {
                Resovoir auxTank = createTank();
                transferred = auxTank.deposit(toInsert);
                if (transferred > 0) {
                    auxiliaryTanks.add(auxTank);
                    entity.markDirty();
                    return stack.ofAmount(stack.amount() - transferred);
                }
            }
            return stack;
        }
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (direction != Direction.DOWN) {
            return PipeInsertable.reject(fluids);
        }

        FluidMound mound = FluidMound.of(fluids.fluids());

        synchronized (auxiliaryTanks) {
            fluids.fluids().getFluids().forEach(fluid -> {
                for (Resovoir auxTank : auxiliaryTanks) {
                    int transferred = auxTank.deposit(fluid);
                    if (transferred > 0) {
                        mound.remove(fluid.ofAmount(transferred));
                        return;
                    }
                }
                if (auxiliaryTanks.size() < 4) {
                    Resovoir auxTank = createTank();
                    int transferred = auxTank.deposit(fluid);
                    if (transferred > 0) {
                        auxiliaryTanks.add(auxTank);
                        mound.remove(fluid.ofAmount(transferred));
                        entity.markDirty();
                    }
                }
            });
        }

        return PipeInsertable.reject(PipeFluids.of(mound, fluids.temperature()));
    }

    private boolean isValidIngredient(ItemStack stack) {
        return FluidCapacity.get(stack) == 0 && entity.getWorld().getRecipeManager()
                .listAllOfType(PSRecipes.CHEMISTRY)
                .stream()
                .anyMatch(recipe -> recipe.value().getIngredients().stream().anyMatch(i -> i.test(stack)));
    }

    @Override
    public ItemMound getCraftingIngredients() {
        return new ItemMound(ingredients);
    }

    @Override
    public void onCraft(BunsenBurnerRecipe.Input input) {
        ingredients = input.input();
        auxiliaryTanks = new ArrayList<>();
        input.fluids().getFluids().forEach(fluid -> {
            fluid = deposit(fluid);
            if (!fluid.isEmpty()) {
                input.consumer().accept(fluid);
            }
        });
    }

    @Override
    public void produceProducts(ServerWorld world, BlockPos pipePos, BunsenBurnerRecipe.Product product) {
        product.items().forEach(stack -> ingredients.addStack(stack));
        if (!PipeInsertable.tryInsert(world, pipePos, Direction.UP, PipeFluids.of(product.fluids(), 15)).equals(STATUS_ACCEPT_ALL)) {
            onFluidWasted(world);
        }
    }

    @Override
    public void clear() {
        super.clear();
        ingredients.clear();
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        super.toNbt(compound, lookup);
        compound.put("ingredients", ingredients.toNbt(lookup));
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        super.fromNbt(compound, lookup);
        ingredients = new ItemMound(compound.getCompound("ingredients"), lookup);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return INGREDIENT_SLOT_ID;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot < MAX_INGREDIENTS;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot < MAX_INGREDIENTS;
    }

    @Override
    public int size() {
        return MAX_INGREDIENTS + 1;
    }

    @Override
    public boolean isEmpty() {
        return ingredients.isEmpty() && super.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= ingredients.size()) {
            return ItemStack.EMPTY;
        }
        return ingredients.getCounts().keySet().stream().toList().get(slot).getDefaultStack();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ingredients.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ingredients.removeStack(slot, Integer.MAX_VALUE);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (ingredients.size() < MAX_INGREDIENTS) {
            ingredients.addStack(stack);
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(ItemStack container) {
        DefaultedList<ItemStack> stacks = ingredients.convertToItemStacks();
        if (!container.isEmpty()) {
            stacks.add(container);
        }
        return stacks;
    }
}
