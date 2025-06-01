package ivorius.psychedelicraft.block;

import java.util.Map;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.SyncedBlockEntity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FluidCauldronBlock extends LeveledCauldronBlock implements BlockEntityProvider {
    public static final Map<Item, CauldronBehavior> BEHAVIOUR = CauldronBehavior.createMap();

    public FluidCauldronBlock(Settings settings) {
        super(settings, i -> false, BEHAVIOUR);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected boolean canBeFilledByDripstone(Fluid fluid) {
        return fluid != Fluids.EMPTY;
    }

    @Override
    protected void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid) {
        if (world.getBlockEntity(pos, PSBlockEntities.CAULDRON).filter(data -> data.fluid.isOf(fluid)).isPresent()) {
            super.fillFromDripstone(state, world, pos, fluid);
        }
    }

    @Override
    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return Items.CAULDRON.getDefaultStack();
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    public static class Data extends SyncedBlockEntity {
        public ItemFluids fluid = ItemFluids.EMPTY;

        public Data(BlockPos pos, BlockState state) {
            super(PSBlockEntities.CAULDRON, pos, state);
        }

        public void setFluid(ItemFluids fluid) {
            this.fluid = fluid;
            markDirty();
        }

        public ItemFluids getFluid() {
            return fluid;
        }

        @Override
        public void readNbt(NbtCompound nbt) {
            fluid = ItemFluids.decode(nbt.get("fluid"));
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
            nbt.put("fluid", fluid.encode());
        }
    }
}
