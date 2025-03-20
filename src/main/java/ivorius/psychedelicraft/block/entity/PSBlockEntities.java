package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.*;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSBlockEntities {
    BlockEntityType<DryingTableBlockEntity> DRYING_TABLE = create("drying_table", FabricBlockEntityTypeBuilder.create(DryingTableBlockEntity::new, PSBlocks.DRYING_TABLE, PSBlocks.IRON_DRYING_TABLE));
    BlockEntityType<MashTubBlockEntity> MASH_TUB = create("wooden_vat", FabricBlockEntityTypeBuilder.create(MashTubBlockEntity::new, PSBlocks.MASH_TUB));
    BlockEntityType<MashTubWallBlock.MasterPosition> MASH_TUB_EDGE = create("wooden_vat_edge", FabricBlockEntityTypeBuilder.create(MashTubWallBlock.MasterPosition::new, PSBlocks.MASH_TUB_EDGE));
    BlockEntityType<RiftJarBlockEntity> RIFT_JAR = create("rift_jar", FabricBlockEntityTypeBuilder.create(RiftJarBlockEntity::new, PSBlocks.RIFT_JAR));
    BlockEntityType<PeyoteBlockEntity> PEYOTE = create("peyote", FabricBlockEntityTypeBuilder.create(PeyoteBlockEntity::new, PSBlocks.PEYOTE));
    BlockEntityType<DistilleryBlockEntity> DISTILLERY = create("distillery", FabricBlockEntityTypeBuilder.create(DistilleryBlockEntity::new, PSBlocks.DISTILLERY));
    BlockEntityType<BottleRackBlockEntity> BOTTLE_RACK = create("bottle_rack", FabricBlockEntityTypeBuilder.create(BottleRackBlockEntity::new, PSBlocks.BOTTLE_RACK, PSBlocks.WALL_BOTTLE_RACK));
    BlockEntityType<FlaskBlockEntity> FLASK = create("flask", FabricBlockEntityTypeBuilder.create(FlaskBlockEntity::new, PSBlocks.FLASK));
    BlockEntityType<BarrelBlockEntity> BARREL = create("barrel", FabricBlockEntityTypeBuilder.create(BarrelBlockEntity::new, PSBlocks.ALL_BARRELS.toArray(Block[]::new)));
    BlockEntityType<BurnerBlockEntity> BUNSEN_BURNER = create("bunsen_burner", FabricBlockEntityTypeBuilder.create(BurnerBlockEntity::new, PSBlocks.BUNSEN_BURNER));
    BlockEntityType<TrayBlockEntity> TRAY = create("tray", FabricBlockEntityTypeBuilder.create(TrayBlockEntity::new, PSBlocks.TRAY));
    BlockEntityType<PlacedDrinksBlock.Data> PLACED_DRINK = create("placed_drink", FabricBlockEntityTypeBuilder.create(PlacedDrinksBlock.Data::new, PSBlocks.PLACED_DRINK));
    BlockEntityType<GlassTubeBlock.Data> GLASS_TUBE = create("glass_tube", FabricBlockEntityTypeBuilder.create(GlassTubeBlock.Data::new, PSBlocks.GLASS_TUBE, PSBlocks.GLASS_VALVE));

    static <T extends BlockEntity> BlockEntityType<T> create(String id, FabricBlockEntityTypeBuilder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Psychedelicraft.id(id), builder.build());
    }

    static void bootstrap() { }
}

