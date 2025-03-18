package ivorius.psychedelicraft.block.entity;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.FluidMound;
import ivorius.psychedelicraft.recipe.HardeningRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldAccess;

public class TrayBlockEntity extends SyncedBlockEntity implements PipeInsertable {
    static final int MAX_CAPACITY = 50;

    private final Resovoir fluid = new Resovoir(MAX_CAPACITY, (tank, level) -> {});
    private FluidMound impurities = new FluidMound();

    private boolean dirty;

    private int timeToHarden = -1;

    public final PropertyDelegate propertyDelegate = new ArrayPropertyDelegate(2);

    private Optional<HardeningRecipe> matchingRecipe = Optional.empty();
    private Optional<ItemStack> craftingResult = Optional.empty();

    public TrayBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.TRAY, pos, state);
    }

    public int getLevel() {
        return MathHelper.clamp(fluid.getContents().amount(), 0, MAX_CAPACITY);
    }

    public boolean isHardened() {
        return getCraftingResult().isPresent();
    }

    public Optional<ItemStack> getCraftingResult() {
        return craftingResult;
    }

    public void tick(ServerWorld world) {

        if (getLevel() >= MAX_CAPACITY && !isHardened()) {
            if (matchingRecipe.isEmpty()) {
                matchingRecipe = world.getRecipeManager()
                        .getFirstMatch(PSRecipes.TRAY, new HardeningRecipe.Input(fluid.getContents(), impurities), world)
                        .map(RecipeEntry::value);
            }
            matchingRecipe.ifPresent(recipe -> {
                if (timeToHarden < 0) {
                    timeToHarden = recipe.hardeningTime();
                }
                if (--timeToHarden <= 0) {
                    world.playSoundAtBlockCenter(this.getPos(), PSSounds.BLOCK_TRAY_HARDEN, SoundCategory.BLOCKS, 1, 1, true);
                    craftingResult = Optional.of(recipe.craft(
                            new HardeningRecipe.Input(fluid.getContents(), impurities),
                            world.getRegistryManager()).copyWithCount(recipe.amount().get(world.random)));
                    impurities = new FluidMound();
                    fluid.clear();
                    matchingRecipe = Optional.empty();
                } else {
                    world.playSoundAtBlockCenter(this.getPos(), PSSounds.BLOCK_TRAY_HARDEN, SoundCategory.BLOCKS, 0.2F, 1, true);
                }

                dirty = true;
            });

        }

        if (dirty) {
            markDirty();
            dirty = false;
        }
    }

    @Override
    public boolean acceptsConnectionFrom(WorldAccess world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return input && direction == Direction.UP;
    }

    @Override
    public Either<Optional<PipeFluids>, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (direction != Direction.DOWN) {
            return PipeInsertable.reject(fluids);
        }
        if (isHardened() || timeToHarden > 0) {
            return PipeInsertable.reject(fluids);
        }

        FluidMound remainder = new FluidMound(fluids.fluids());
        PipeFluids copy = new PipeFluids(fluids.fluids(), fluids.temperature());
        copy.splitCondensate().getFluids().forEach(fluid -> {
            if (getLevel() < MAX_CAPACITY && canAccept(world, fluid) && this.fluid.getContents().canCombine(fluid)) {
                int amountDeposited = this.fluid.deposit(fluid);

                if (amountDeposited > 0) {
                    remainder.remove(fluid.ofAmount(amountDeposited));
                    fluid = fluid.ofAmount(fluid.amount() - amountDeposited);
                    dirty = true;
                }
            }
            if (canAcceptImpurity(world, fluid)) {
                int maxDeposited = MathHelper.clamp(fluid.amount() - impurities.getAmount(fluid), 0, MAX_CAPACITY);
                if (maxDeposited > 0) {
                    fluid = fluid.ofAmount(maxDeposited);
                    remainder.remove(fluid);
                    this.impurities.add(fluid);
                    dirty = true;
                }
            }
        });

        matchingRecipe = Optional.empty();

        if (remainder.isEmpty()) {
            return PipeInsertable.STATUS_ACCEPT_ALL;
        }

        return PipeInsertable.reject(new PipeFluids(remainder, fluids.temperature()));
    }

    private boolean canAccept(ServerWorld world, ItemFluids fluids) {
        return !fluids.isEmpty() && world.getRecipeManager().listAllOfType(PSRecipes.TRAY).stream()
                .anyMatch(recipe -> recipe.value().isCoreFluid(fluids));
    }

    private boolean canAcceptImpurity(ServerWorld world, ItemFluids fluids) {
        return !fluids.isEmpty() && world.getRecipeManager().listAllOfType(PSRecipes.TRAY).stream()
                .anyMatch(recipe -> recipe.value().isCoreFluid(fluid.getContents()) && recipe.value().isValidImpurity(fluids));
    }

    @Override
    public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
        super.writeNbt(compound, lookup);
        FluidMound.CODEC.encodeStart(NbtOps.INSTANCE, impurities).result().ifPresent(nbt -> compound.put("impurities", nbt));
        compound.putInt("timeToHarden", timeToHarden);
        compound.put("fluid", fluid.toNbt(lookup));
        craftingResult.flatMap(s -> ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, s).result()).ifPresent(nbt -> compound.put("craftingResult", nbt));
    }

    @Override
    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        super.readNbt(compound, lookup);
        impurities = FluidMound.CODEC.decode(NbtOps.INSTANCE, compound.get("impurities")).result()
                .map(Pair::getFirst)
                .orElseGet(FluidMound::new);
        timeToHarden = compound.getInt("timeToHarden");
        fluid.fromNbt(compound.getCompound("fluid"), lookup);
        craftingResult = ItemStack.fromNbt(lookup, compound.getCompound("craftingResult"));
        matchingRecipe = Optional.empty();
    }
}
