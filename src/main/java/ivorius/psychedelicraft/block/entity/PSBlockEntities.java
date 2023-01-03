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
    BlockEntityType<MashTubBlockEntity> MASH_TUB = create("mash_tub", BlockEntityType.Builder.create(MashTubBlockEntity::new, PSBlocks.mashTub));
    BlockEntityType<TileEntityRiftJar> RIFT_JAR = create("rift_jar", BlockEntityType.Builder.create(TileEntityRiftJar::new, PSBlocks.riftJar));
    BlockEntityType<DistilleryBlockEntity> DISTILLERY = create("distillery", BlockEntityType.Builder.create(DistilleryBlockEntity::new, PSBlocks.distillery));
    BlockEntityType<TileEntityBottleRack> BOTTLE_RACK = create("bottle_rack", BlockEntityType.Builder.create(TileEntityBottleRack::new, PSBlocks.bottleRack));
    BlockEntityType<FlaskBlockEntity> FLASK = create("flask", BlockEntityType.Builder.create(FlaskBlockEntity::new, PSBlocks.flask));
    BlockEntityType<BarrelBlockEntity> BARREL = create("barrel", BlockEntityType.Builder.create(BarrelBlockEntity::new,
            PSBlocks.oak_barrel, PSBlocks.spruce_barrel,
            PSBlocks.birch_barrel, PSBlocks.jungle_barrel,
            PSBlocks.acacia_barrel, PSBlocks.dark_oak_barrel
    ));

    static <T extends BlockEntity> BlockEntityType<T> create(String id, Builder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, builder.build(null));
    }

    static void bootstrap() { }
}

