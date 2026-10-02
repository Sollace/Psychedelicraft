package ivorius.psychedelicraft.block.entity;

import java.util.Optional;

import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.FluidMound;
import ivorius.psychedelicraft.recipe.HardeningRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldView;

public class TrayBlockEntity extends SyncedBlockEntity implements PipeInsertable {
    static final int MAX_CAPACITY = 50;

    private final Resovoir fluid = new Resovoir(MAX_CAPACITY, (tank, level) -> {});
    private FluidMound impurities = FluidMound.of();
    private Impurities cuts = Impurities.EMPTY;

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
                        .getFirstMatch(PSRecipes.TRAY, new HardeningRecipe.Input(world.random, fluid.getContents(), impurities, cuts), world)
                        .map(RecipeEntry::value);
            }

            matchingRecipe.ifPresent(recipe -> {
                if (timeToHarden < 0) {
                    timeToHarden = recipe.hardeningTime();
                }
                if (--timeToHarden <= 0) {
                    world.playSound(null, getPos(), PSSounds.BLOCK_TRAY_HARDEN, SoundCategory.BLOCKS, 1, 1);
                    craftingResult = Optional.of(recipe.craft(new HardeningRecipe.Input(world.random, fluid.getContents(), impurities, cuts), world.getRegistryManager()));
                    impurities = FluidMound.of();

                    fluid.clear();
                    matchingRecipe = Optional.empty();
                    cuts = Impurities.EMPTY;

                    for (ServerPlayerEntity player : world.getNonSpectatingEntities(ServerPlayerEntity.class, Box.of(getPos().toCenterPos(), 17, 17, 17))) {
                        PSCriteria.TRAY_HARDEN.trigger(player);
                    }
                } else {
                    world.playSound(null, getPos(), PSSounds.BLOCK_TRAY_HARDEN, SoundCategory.BLOCKS, 0.2F, 1);
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
    public boolean acceptsConnectionFrom(WorldView world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return input && direction == Direction.UP;
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (direction != Direction.DOWN || isHardened() || timeToHarden > 0) {
            return PipeInsertable.reject(fluids);
        }

        FluidMound remainder = FluidMound.of(fluids.fluids());
        PipeFluids copy = PipeFluids.of(fluids.fluids(), fluids.impurities(), fluids.temperature());
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

        cuts = Impurities.combine(copy.impurities(), cuts);
        matchingRecipe = Optional.empty();

        if (remainder.isEmpty()) {
            return PipeInsertable.STATUS_ACCEPT_ALL;
        }

        return PipeInsertable.reject(fluids.withFluids(remainder));
    }

    private boolean canAccept(ServerWorld world, ItemFluids fluids) {
        return !fluids.isEmpty() && world.getRecipeManager().getAllOfType(PSRecipes.TRAY).stream()
                .anyMatch(recipe -> recipe.value().isCoreFluid(fluids));
    }

    private boolean canAcceptImpurity(ServerWorld world, ItemFluids fluids) {
        return !fluids.isEmpty() && world.getRecipeManager().getAllOfType(PSRecipes.TRAY).stream()
                .anyMatch(recipe -> recipe.value().isCoreFluid(fluid.getContents()) && recipe.value().isValidImpurity(fluids));
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put("impurities", FluidMound.CODEC, impurities);
        view.putInt("timeToHarden", timeToHarden);
        fluid.writeData(view.get("fluid"));
        craftingResult.filter(s -> !s.isEmpty()).ifPresent(s -> view.put("craftingResult", ItemStack.CODEC, s));
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        impurities = view.read("impurities", FluidMound.CODEC).orElseGet(FluidMound::of);
        timeToHarden = view.getInt("timeToHarden", 0);
        fluid.readData(view.getReadView("fluid"));
        craftingResult = view.read("craftingResult", ItemStack.OPTIONAL_CODEC);
        matchingRecipe = Optional.empty();
    }
}
