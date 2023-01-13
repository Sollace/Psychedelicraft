package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityType.Builder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSBlockEntities {
    BlockEntityType<DryingTableBlockEntity> DRYING_TABLE = create("drying_table", BlockEntityType.Builder.create(DryingTableBlockEntity::new, PSBlocks.DRYING_TABLE, PSBlocks.IRON_DRYING_TABLE));
    BlockEntityType<MashTubBlockEntity> MASH_TUB = create("mash_tub", BlockEntityType.Builder.create(MashTubBlockEntity::new, PSBlocks.MASH_TUB));
    BlockEntityType<RiftJarBlockEntity> RIFT_JAR = create("rift_jar", BlockEntityType.Builder.create(RiftJarBlockEntity::new, PSBlocks.RIFT_JAR));
    BlockEntityType<DistilleryBlockEntity> DISTILLERY = create("distillery", BlockEntityType.Builder.create(DistilleryBlockEntity::new, PSBlocks.DISTILLERY));
    BlockEntityType<BottleRackBlockEntity> BOTTLE_RACK = create("bottle_rack", BlockEntityType.Builder.create(BottleRackBlockEntity::new, PSBlocks.BOTTLE_RACK));
    BlockEntityType<FlaskBlockEntity> FLASK = create("flask", BlockEntityType.Builder.create(FlaskBlockEntity::new, PSBlocks.FLASK));
    BlockEntityType<BarrelBlockEntity> BARREL = create("barrel", BlockEntityType.Builder.create(BarrelBlockEntity::new,
            PSBlocks.OAK_BARREL, PSBlocks.SPRUCE_BARREL,
            PSBlocks.BIRCH_BARREL, PSBlocks.JUNGLE_BARREL,
            PSBlocks.ACACIA_BARREL, PSBlocks.DARK_OAK_BARREL
    ));

    static <T extends BlockEntity> BlockEntityType<T> create(String id, Builder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Psychedelicraft.id(id), builder.build(null));
    }

    static void bootstrap() { }
}

