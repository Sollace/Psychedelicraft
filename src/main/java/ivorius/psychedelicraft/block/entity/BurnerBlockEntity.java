/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.BurnerBlock;
import ivorius.psychedelicraft.block.GlassTubeBlock;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.Processable.ByProductConsumer;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;

public class BurnerBlockEntity extends FlaskBlockEntity implements BlockWithFluid.DirectionalFluidResovoir, Resovoir.ChangeListener {

    private int temperature;
    private ItemStack container;
    //private final Map<Direction, Resovoir> ingredientTanks = new HashMap<>();

    public BurnerBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BUNSEN_BURNER, pos, state, FluidVolumes.GLASS_BOTTLE);
    }

    public void setContainer(ItemStack container) {
        this.container = container;
        markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (getWorld() instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(getPos());
        }
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int change) {
        super.onLevelChange(resovoir, change);
        if (change > 0) {
            getWorld().playSound(null, getPos(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS);
        }
    }

    public ItemStack getContainer() {
        return container;
    }

    public int getTemperature() {
        return temperature;
    }

    public boolean interact(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {

        if (hand != Hand.MAIN_HAND) {
            return false;
        }

        if (!getContainer().isEmpty() && stack.isEmpty()) {
            Resovoir tank = getPrimaryTank();
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(getContainer());
            tank.withdraw(t, FluidVolumes.GLASS_BOTTLE);
            player.setStackInHand(hand, t.toItemStack());
            setContainer(ItemStack.EMPTY);
        }

        ItemFluids fluid = ItemFluids.of(stack);
        boolean consumeItem = false;

        if (getContainer().isEmpty() && (
                stack.isOf(Items.GLASS_BOTTLE) || stack.isOf(PSItems.FILLED_GLASS_BOTTLE) || stack.isOf(Items.POTION) || stack.isOf(PSItems.BOTTLE)
        )) {
            setContainer(ItemFluids.set(stack.splitUnlessCreative(1, player), ItemFluids.EMPTY));
            consumeItem = true;
        }

        if (!getContainer().isEmpty() && !fluid.isEmpty()) {
            Resovoir tank = getTankOnSide(side);
            if (consumeItem) {
                tank.deposit(fluid);
                return true;
            }

            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
            return tank.deposit(t, (int)tank.getCapacity()) > 0;
        }

        return false;
    }

    @Override
    public void tick(ServerWorld world) {
        if (!getContainer().isEmpty()) {
            if (world.getTime() % 14 == 0) {
                if (getCachedState().get(BurnerBlock.LIT)) {
                    if (temperature < (getPrimaryTank().getContents().isEmpty() ? 200 : 100)) {
                        temperature++;
                        markDirty();
                    }
                } else {
                    if (temperature > 0) {
                        temperature--;
                        markDirty();
                    }
                }
            }

            if (temperature > 50 && world.getTime() % 5 == world.random.nextInt(3)) {
                if (getPrimaryTank().getContents().isEmpty()) {
                    world.playSound(null, getPos(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.25F, 0.02F);
                    world.spawnParticles(ParticleTypes.SMOKE,
                            pos.getX() + world.getRandom().nextTriangular(0.5F, 0.1F),
                            pos.getY() + 0.6F,
                            pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.1F),
                            2, 0, 0, 0, 0);

                    if (temperature > 180 && world.random.nextInt(3) == 0) {
                        world.playSound(null, getPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.25F, 0.02F);
                        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, getPos(), Block.getRawIdFromState(Blocks.GLASS_PANE.getDefaultState()));
                        setContainer(ItemStack.EMPTY);
                    }
                } else {
                    if (temperature > 100) {
                        temperature -= 2;
                        markDirty();
                    }
                    world.playSound(null, getPos(), SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1.25F, 0.02F);
                    BlockPos pipePos = getPos().up();
                    Resovoir tank = getPrimaryTank();

                    if (tank.getContents().fluid() instanceof Processable processable) {
                        processable.process(tank, ProcessType.REACT, new ByProductConsumer() {
                            @Override
                            public void accept(ItemStack stack) {
                                Block.dropStack(world, getPos(), stack);
                            }

                            @Override
                            public void accept(ItemFluids fluids) {
                                int amount = PipeInsertable.tryInsert(world, pipePos, Direction.UP, fluids);
                                if (amount < fluids.amount()) {
                                    onFluidWasted(world);
                                }
                            }

                        });
                    } else {
                        if (PipeInsertable.tryInsert(world, pipePos, Direction.UP, getPrimaryTank()) == GlassTubeBlock.SPILL_STATUS) {
                            getPrimaryTank().drain(1);
                            onFluidWasted(world);
                        }
                    }
                }
            }
        }
    }

    private void onFluidWasted(ServerWorld world) {
        world.spawnParticles(ParticleTypes.DUST_PLUME,
                pos.getX() + world.getRandom().nextTriangular(0.5F, 0.1F),
                pos.getY() + 0.6F,
                pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.1F),
                2, 0, 0, 0, 0);
    }

    @Override
    public Resovoir getTankOnSide(Direction direction) {
        //if (direction == Direction.DOWN) {
            return super.getTankOnSide(direction);
        //}
        //return ingredientTanks.computeIfAbsent(direction, d -> new Resovoir(FluidVolumes.BOTTLE, this));
    }
    @Override
    public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
        super.writeNbt(compound, lookup);
        ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, container).result().ifPresent(container -> compound.put("container", container));
        compound.putInt("temperature", temperature);
        /*NbtCompound ingredientTanksNbt = new NbtCompound();
        ingredientTanks.forEach((direction, tank) -> {
            ingredientTanksNbt.put(direction.asString(), tank.toNbt(lookup));
        });
        compound.put("ingredients", ingredientTanksNbt);*/
    }

    @Override
    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        super.readNbt(compound, lookup);
        container = ItemStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, compound.get("container")).result().map(Pair::getFirst).orElse(ItemStack.EMPTY);
        temperature = compound.getInt("temperature");
        //ingredientTanks.clear();
        /*NbtCompound ingredients = compound.getCompound("ingredients");
        ingredients.getKeys().forEach(key -> {
            Direction direction = Direction.byName(key);
            if (direction != null) {
                Resovoir tank = new Resovoir(FluidVolumes.BOTTLE, this);
                tank.fromNbt(ingredients.getCompound(key), lookup);
                ingredientTanks.put(direction, tank);
            }
        });*/
    }

}
