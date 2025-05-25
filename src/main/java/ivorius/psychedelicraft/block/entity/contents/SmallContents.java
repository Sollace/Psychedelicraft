package ivorius.psychedelicraft.block.entity.contents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.block.ShapeUtil;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity.Contents;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.ItemFluidsMixture;
import ivorius.psychedelicraft.recipe.BunsenBurnerRecipe;
import ivorius.psychedelicraft.recipe.FluidMound;
import ivorius.psychedelicraft.recipe.ItemMound;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class SmallContents implements BurnerBlockEntity.CraftableContents, BlockWithFluid.DirectionalFluidResovoir, Resovoir.ChangeListener {
    public static final Identifier ID = Psychedelicraft.id("small");
    protected List<Resovoir> auxiliaryTanks = new ArrayList<>();
    private static final VoxelShape SHAPE = ShapeUtil.createCenteredShape(1.5F, 8, 1.5F);

    protected int capacity;
    protected final BurnerBlockEntity entity;

    public SmallContents(BurnerBlockEntity entity, int capacity, ItemStack stack) {
        this.entity = entity;
        this.capacity = capacity;
        auxiliaryTanks.add(createTank());
        loadContents(stack);
    }

    @Override
    public VoxelShape getOutlineShape() {
        return SHAPE;
    }

    protected void loadContents(ItemStack stack) {
        getPrimaryTank().deposit(ItemFluids.of(stack));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    protected Resovoir createTank() {
        return new Resovoir(capacity, this);
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int change) {
        markDirty();
        if (change > 0) {
            entity.setTemperature(entity.getTemperature() / 2);
        }
        if (resovoir.getContents().isEmpty()) {
            synchronized (auxiliaryTanks) {
                auxiliaryTanks.removeIf(r -> r.getContents().isEmpty());
                if (auxiliaryTanks.isEmpty()) {
                    auxiliaryTanks.add(createTank());
                }
            }
        }
        if (change > 0) {
            entity.getWorld().playSound(null, entity.getPos(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS);
        }
    }

    @Override
    public Optional<Contents> interact(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        if (stack.isEmpty()) {
            if (!player.getWorld().isClient) {
                player.setStackInHand(hand, ItemFluidsMixture.set(entity.getContainer(), getAuxiliaryTanks().stream().map(Resovoir::getContents).toList()));
                entity.setContainer(ItemStack.EMPTY);
                for (ItemStack ingredient : getCraftingIngredients().convertToItemStacks()) {
                    if (!player.giveItemStack(stack)) {
                        Block.dropStack(player.getWorld(), entity.getPos(), ingredient);
                    }
                }
                clear();
            }
            entity.playSound(player, SoundEvents.ENTITY_ITEM_PICKUP);

            return Optional.of(new EmptyContents(entity));
        }

        if (FluidCapacity.get(stack) > 0) {
            return interactWithFluidVessel(stack, player, hand, side);
        }

        return Optional.empty();
    }

    protected Optional<Contents> interactWithFluidVessel(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
        if (!t.fluids().isEmpty() && getPrimaryTank().deposit(t, t.fluids().amount()) > 0) {
            entity.playSound(player, SoundEvents.ITEM_BOTTLE_EMPTY);
            if (!player.getWorld().isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack()));
            }
            return Optional.of(this);
        }

        return Optional.empty();
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (direction != Direction.DOWN) {
            return PipeInsertable.reject(fluids);
        }

        FluidMound remainder = FluidMound.of(fluids.fluids());

        fluids.fluids().getFluids().forEach(fluid -> {
            int transferred = getPrimaryTank().deposit(fluid);
            if (transferred > 0) {
                remainder.remove(fluid.ofAmount(transferred));
            }
        });

        return PipeInsertable.reject(fluids.withFluids(remainder));
    }

    @Override
    public void clientTick(World world) {

    }

    @Override
    public void tick(ServerWorld world) {

    }

    @Override
    public ItemMound getCraftingIngredients() {
        return new ItemMound();
    }

    @Override
    public void onCraft(BunsenBurnerRecipe.Input input) {
        if (input.fluids().isEmpty()) {
            getPrimaryTank().setContents(ItemFluids.EMPTY);
        } else {
            ItemFluids first = input.fluids().get(0);
            getPrimaryTank().setContents(first.ofAmount(Math.min(capacity, first.amount())));
            if (first.amount() > capacity) {
                input.consumer().accept(first.ofAmount(first.amount() - capacity));
            }
            for (int i = 1; i < input.fluids().size(); i++) {
                input.consumer().accept(input.fluids().get(i));
            }
        }
    }

    @Override
    public void produceProducts(ServerWorld world, BlockPos pipePos, BunsenBurnerRecipe.Product product) {
        product.items().forEach(stack -> Block.dropStack(world, entity.getPos(), stack));
        if (!PipeInsertable.tryInsert(world, pipePos, Direction.UP, PipeFluids.of(product.fluids(), 15)).equals(STATUS_ACCEPT_ALL)) {
            onFluidWasted(world);
        }
    }

    @Override
    public void markDirty() {
        entity.markDirty();
    }

    protected void onFluidWasted(ServerWorld world) {
        world.spawnParticles(ParticleTypes.DUST_PLUME,
                entity.getPos().getX() + world.getRandom().nextTriangular(0.5F, 0.1F),
                entity.getPos().getY() + 0.6F,
                entity.getPos().getZ() + world.getRandom().nextTriangular(0.5F, 0.1F),
                2, 0, 0, 0, 0);
    }

    @Override
    public Resovoir getPrimaryTank() {
        synchronized (auxiliaryTanks) {
            if (auxiliaryTanks.isEmpty()) {
                auxiliaryTanks.add(createTank());
            }
            return auxiliaryTanks.get(0);
        }
    }

    public Resovoir getLastTank() {
        synchronized (auxiliaryTanks) {
            return auxiliaryTanks.isEmpty() ? getPrimaryTank() : auxiliaryTanks.get(auxiliaryTanks.size() - 1);
        }
    }

    @Override
    public Resovoir getTankOnSide(Direction direction) {
        return getPrimaryTank();
    }

    @Override
    public List<Resovoir> getAuxiliaryTanks() {
        synchronized (auxiliaryTanks) {
            return new ArrayList<>(auxiliaryTanks);
        }
    }

    @Override
    public void clear() {
        synchronized (auxiliaryTanks) {
            auxiliaryTanks.clear();
        }
        markDirty();
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        compound.putInt("capacity", capacity);
        compound.put("fluids", NbtSerialisable.fromList(getAuxiliaryTanks(), lookup));
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        capacity = compound.getInt("capacity", FluidVolumes.GLASS_BOTTLE);
        auxiliaryTanks = compound.getList("fluids").map(list -> NbtSerialisable.toList(new ArrayList<>(), list, lookup, this::createTank)).orElseGet(ArrayList::new);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[0];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return getTotalFluidVolume() == 0;
    }

    @Override
    public ItemStack getStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public List<ItemStack> getDroppedStacks(ItemStack container) {
        if (!isEmpty()) {
            synchronized (auxiliaryTanks) {
                if (auxiliaryTanks.size() == 1) {
                    return List.of(ItemFluids.set(container.copy(), getPrimaryTank().getContents()));
                }
                return List.of(ItemFluidsMixture.set(container.copy(), auxiliaryTanks.stream().map(Resovoir::getContents).toList()));
            }
        }
        return List.of(container);
    }

    @Override
    public ItemStack getFilled(ItemStack container, boolean dryRun, float drainPercentage) {
        if (!isEmpty()) {
            synchronized (auxiliaryTanks) {
                if (auxiliaryTanks.size() == 1) {
                    return ItemFluids.set(container.copy(), copyOrWithdraw(getPrimaryTank(), dryRun, drainPercentage));
                }

                return ItemFluidsMixture.set(container.copy(), auxiliaryTanks.stream()
                        .map(tank -> copyOrWithdraw(tank, dryRun, drainPercentage))
                        .toList());
            }
        }
        return container;
    }

    private ItemFluids copyOrWithdraw(Resovoir tank, boolean dryRun, float drainPercentage) {
        int amount = MathHelper.ceil(tank.getContents().amount() * drainPercentage);
        return dryRun ? tank.getContents().ofAmount(Math.min(amount, tank.getContents().amount())) : tank.drain(amount);
    }
}
