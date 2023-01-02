package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.blocks.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityType.Builder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSBlockEntities {
    BlockEntityType<DryingTableBlockEntity> DRYING_TABLE = create("drying_table", BlockEntityType.Builder.create(DryingTableBlockEntity::new,
            PSBlocks.dryingTable, PSBlocks.spruceDryingTable,
            PSBlocks.birchDryingTable, PSBlocks.jungleDryingTable,
            PSBlocks.acaciaDryingTable, PSBlocks.darkOakDryingTable, PSBlocks.dryingTableIron));
    BlockEntityType<TileEntityMashTub> MASH_TUB = create("mash_tub", BlockEntityType.Builder.create(TileEntityMashTub::new, PSBlocks.mashTub));
    BlockEntityType<TileEntityDistillery> DISTILLERY = create("distillery", BlockEntityType.Builder.create(TileEntityDistillery::new, PSBlocks.distillery));
    BlockEntityType<TileEntityFlask> FLASK = create("flask", BlockEntityType.Builder.create(TileEntityFlask::new, PSBlocks.flask));
    BlockEntityType<TileEntityBarrel> BARREL = create("barrel", BlockEntityType.Builder.create(TileEntityBarrel::new,
            PSBlocks.oak_barrel, PSBlocks.spruce_barrel,
            PSBlocks.birch_barrel, PSBlocks.jungle_barrel,
            PSBlocks.acacia_barrel, PSBlocks.dark_oak_barrel
    ));

    static <T extends BlockEntity> BlockEntityType<T> create(String id, Builder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, builder.build(null));
    }

    static void bootstrap() { }
}

