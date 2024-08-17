package ivorius.psychedelicraft.block.entity.contents;

import java.util.ArrayList;
import java.util.List;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.GlassTubeBlock;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity.Contents;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.Processable.ByProductConsumer;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.ItemFluidsMixture;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;

public class SmallContents implements BurnerBlockEntity.Contents, BlockWithFluid.DirectionalFluidResovoir, Resovoir.ChangeListener {
    public static final Identifier ID = Psychedelicraft.id("small");
    private final List<Resovoir> auxiliaryTanks = new ArrayList<>();

    protected int capacity;
    protected final BurnerBlockEntity entity;

    public SmallContents(BurnerBlockEntity entity, int capacity, ItemStack stack) {
        this.entity = entity;
        this.capacity = capacity;
        auxiliaryTanks.add(createTank());
        loadContents(stack);
    }

    public SmallContents(BurnerBlockEntity entity, NbtCompound compound, WrapperLookup lookup) {
        this.entity = entity;
        fromNbt(compound, lookup);
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
            entity.getWorld().playSound(null, entity.getPos(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS);
        }
    }

    @Override
    public TypedActionResult<Contents> interact(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        if (stack.isEmpty()) {
            player.setStackInHand(hand, ItemFluidsMixture.set(entity.getContainer(), getAuxiliaryTanks().stream().map(Resovoir::getContents).toList()));
            clear();
            entity.setContainer(ItemStack.EMPTY);
            entity.playSound(player, SoundEvents.ENTITY_ITEM_PICKUP);
            return TypedActionResult.success(new EmptyContents(entity));
        }

        if (FluidCapacity.get(stack) > 0) {
            return interactWithFluidVessel(stack, player, hand, side);
        }

        return TypedActionResult.fail(this);
    }

    protected TypedActionResult<Contents> interactWithFluidVessel(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
        if (!t.fluids().isEmpty() && getPrimaryTank().deposit(t, t.fluids().amount()) > 0) {
            entity.playSound(player, SoundEvents.ITEM_BOTTLE_EMPTY);
            player.setStackInHand(hand, t.toItemStack());
            return TypedActionResult.success(this);
        }

        return TypedActionResult.fail(this);
    }

    @Override
    public int tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, ItemFluids fluids) {
        return direction != Direction.UP ? SPILL_STATUS : getPrimaryTank().deposit(fluids);
    }

    @Override
    public void tick(ServerWorld world) {
        BlockPos pos = entity.getPos();
        int temperature = entity.getTemperature();
        if (entity.getTemperature() <= 50 || world.getTime() % 5 != world.random.nextInt(3)) {
            return;
        }

        if (getTotalFluidVolume() == 0) {
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.25F, 0.02F);
            world.spawnParticles(ParticleTypes.SMOKE,
                    pos.getX() + world.getRandom().nextTriangular(0.5F, 0.1F),
                    pos.getY() + 0.6F,
                    pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.1F),
                    2, 0, 0, 0, 0);

            if (temperature > 180 && world.random.nextInt(3) == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.25F, 0.02F);
                world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(Blocks.GLASS_PANE.getDefaultState()));
                entity.clear();
            }
        } else {
            if (entity.getTemperature() > 100) {
                temperature -= 2;
                entity.setTemperature(temperature);
            }
            world.playSound(null, entity.getPos(), SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1.25F, 0.02F);
            BlockPos pipePos = entity.getPos().up();
            Resovoir tank = getPrimaryTank();
            if (tank.getContents().fluid() instanceof Processable processable) {
                processable.process(this, ProcessType.PURIFY, new ByProductConsumer() {
                    @Override
                    public void accept(ItemStack stack) {
                        Block.dropStack(world, entity.getPos(), stack);
                    }

                    @Override
                    public void accept(ItemFluids fluids) {
                        int amount = PipeInsertable.tryInsert(world, pipePos, Direction.UP, fluids);
                        if (amount < fluids.amount()) {
                            onFluidWasted(world);
                        }
                    }
                });
            } else if (shouldProduceEvaporate(world)) {
                tank.drain(1);
                if (PipeInsertable.tryInsert(world, pipePos, Direction.UP, tank) == GlassTubeBlock.SPILL_STATUS) {
                    onFluidWasted(world);
                }
            }
        }
    }

    protected boolean shouldProduceEvaporate(ServerWorld world) {
        return true;
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
        if (auxiliaryTanks.isEmpty()) {
            auxiliaryTanks.add(createTank());
        }
        return auxiliaryTanks.get(0);
    }

    public Resovoir getLastTank() {
        return auxiliaryTanks.isEmpty() ? getPrimaryTank() : auxiliaryTanks.get(auxiliaryTanks.size() - 1);
    }

    @Override
    public Resovoir getTankOnSide(Direction direction) {
        return getPrimaryTank();
    }

    @Override
    public List<Resovoir> getAuxiliaryTanks() {
        return auxiliaryTanks;
    }

    @Override
    public void clear() {
        auxiliaryTanks.clear();
        markDirty();
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        compound.putInt("capacity", capacity);
        compound.put("fluids", NbtSerialisable.fromList(auxiliaryTanks, lookup));
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        capacity = compound.getInt("capacity");
        NbtSerialisable.toList(auxiliaryTanks, compound.getList("fluids", NbtElement.COMPOUND_TYPE), lookup, this::createTank);
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
        return getPrimaryTank().getContents().isEmpty();
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
            if (auxiliaryTanks.size() == 1) {
                return List.of(ItemFluids.set(container.copy(), getPrimaryTank().getContents()));
            }
            return List.of(ItemFluidsMixture.set(container.copy(), auxiliaryTanks.stream().map(Resovoir::getContents).toList()));
        }
        return List.of(container);
    }
}
