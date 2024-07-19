package ivorius.psychedelicraft.block.entity.contents;

import java.util.List;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity.Contents;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.ItemMound;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.ReducingRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class LargeContents extends SmallContents {
    public static final Identifier ID = Psychedelicraft.id("large");
    static final int MAX_INGREDIENTS = 4;
    static final int[] CONTAINER_SLOT_ID = {0};
    static final int[] INGREDIENT_SLOT_ID = {1, 2, 3, 4};

    private final ItemMound ingredients = new ItemMound();
    private int processingTime;

    public LargeContents(BurnerBlockEntity entity, int capacity) {
        super(entity, capacity);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public ItemMound getIngredients() {
        return ingredients;
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int change) {
        super.onLevelChange(resovoir, change);
        if (resovoir.getContents().isEmpty()) {
            getAuxiliaryTanks().remove(resovoir);
            if (getAuxiliaryTanks().isEmpty()) {
                getAuxiliaryTanks().add(createTank());
            }
        }
    }

    @Override
    public TypedActionResult<Contents> interact(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        TypedActionResult<Contents> result = super.interact(stack, player, hand, side);
        if (result.getResult().isAccepted()) {
            return result;
        }

        if (ingredients.size() < MAX_INGREDIENTS && isValidIngredient(stack) && ingredients.getCounts().getInt(stack.getItem()) < 5) {
            ingredients.addStack(stack.splitUnlessCreative(1, player));
            player.setStackInHand(hand, stack);
            entity.playSound(player, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value());
            return TypedActionResult.success(this);
        }

        return TypedActionResult.fail(this);
    }

    @Override
    protected TypedActionResult<Contents> interactWithFluidVessel(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        if (!ItemFluids.of(stack).isEmpty()) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
            if (deposit(t)) {
                entity.playSound(player, SoundEvents.ITEM_BOTTLE_EMPTY);
                player.setStackInHand(hand, t.toItemStack());
                return TypedActionResult.success(this);
            }
            return TypedActionResult.fail(this);
        }

        Resovoir tank = player.isSneaking() ? getLastTank() : getPrimaryTank();
        if (!tank.getContents().isEmpty()) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copyWithCount(1));
            if (tank.withdraw(t, FluidCapacity.get(stack)) > 0) {
                if (stack.getCount() > 1) {
                    player.setStackInHand(hand, t.toItemStack());
                } else {
                    stack.decrementUnlessCreative(1, player);
                    player.giveItemStack(t.toItemStack());
                }
                entity.playSound(player, SoundEvents.ITEM_BOTTLE_FILL);
                return TypedActionResult.success(this);
            }
        }

        return TypedActionResult.fail(this);
    }

    protected boolean deposit(ItemFluids.Transaction t) {
        int maxToInsert = Math.max(0, capacity - getTotalFluidVolume());
        int transferred = 0;
        for (Resovoir auxTank : getAuxiliaryTanks()) {
            transferred = auxTank.deposit(t, maxToInsert);
            if (transferred > 0) {
                return true;
            }
        }
        if (getAuxiliaryTanks().size() < 4) {
            Resovoir auxTank = createTank();
            transferred = auxTank.deposit(t, maxToInsert);
            if (transferred > 0) {
                getAuxiliaryTanks().add(auxTank);
                entity.markDirty();
                return true;
            }
        }
        return false;
    }

    @Override
    public int tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, ItemFluids fluids) {
        if (direction != Direction.UP) {
            return SPILL_STATUS;
        }

        for (Resovoir auxTank : getAuxiliaryTanks()) {
            int transferred = auxTank.deposit(fluids);
            if (transferred > 0) {
                return transferred;
            }
        }
        if (getAuxiliaryTanks().size() < 4) {
            Resovoir auxTank = createTank();
            int transferred = auxTank.deposit(fluids);
            if (transferred > 0) {
                getAuxiliaryTanks().add(auxTank);
                entity.markDirty();
                return transferred;
            }
        }

        return 0;
    }

    private boolean isValidIngredient(ItemStack stack) {
        return FluidCapacity.get(stack) == 0
                && entity.getWorld().getRecipeManager().getFirstMatch(PSRecipes.REACTING_TYPE, new ReducingRecipe.Input(stack), entity.getWorld()).isPresent();
    }

    @Override
    protected boolean shouldProduceEvaporate(ServerWorld world) {
        return getTotalFluidVolume() >= capacity || ingredients.getCounts().object2IntEntrySet().stream().filter(ingredient -> {
            var input = new ReducingRecipe.Input(ingredient.getKey().getDefaultStack());
            return world.getRecipeManager().getFirstMatch(PSRecipes.REACTING_TYPE, input, world).filter(recipe -> {
                if (++processingTime >= recipe.value().stewTime()) {
                    ingredients.remove(ingredient.getKey(), 1);
                    if (!recipe.value().remainder().isEmpty()) {
                        ingredients.addStack(recipe.value().remainder());
                    }
                    processingTime = 0;
                }

                int amount = ingredient.getIntValue();
                int transferred = tryInsert(world, entity.getCachedState(), entity.getPos(), Direction.UP, recipe.value().result().ofAmount(recipe.value().result().amount() * amount));
                if (transferred < amount) {
                    onFluidWasted(world);
                }
                return true;
            }).isPresent();
        }).findAny().isEmpty();
    }

    @Override
    public Resovoir getTankOnSide(Direction direction) {
        //if (direction == Direction.DOWN) {
            return getPrimaryTank();
        //}
        //return ingredientTanks.computeIfAbsent(direction, d -> new Resovoir(FluidVolumes.BOTTLE, this));
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
        ingredients.fromNbt(compound.getCompound("ingredients"), lookup);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return INGREDIENT_SLOT_ID;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot > 0 && slot < MAX_INGREDIENTS;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot <= MAX_INGREDIENTS;
    }

    @Override
    public int size() {
        return MAX_INGREDIENTS + 1;
    }

    @Override
    public boolean isEmpty() {
        return ingredients.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot > ingredients.size()) {
            return ItemStack.EMPTY;
        }
        return ingredients.getCounts().keySet().stream().toList().get(slot).getDefaultStack();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ingredients.removeStack(slot - 1, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ingredients.removeStack(slot - 1, Integer.MAX_VALUE);
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
