package ivorius.psychedelicraft.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.block.PSBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public class PSBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public PSBlockTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected TagBuilder getTagBuilder(TagKey<Block> tag) {
        return super.getTagBuilder(tag);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        addJuniperWoodset();

        valueLookupBuilder(PSTags.Blocks.BARRELS).add(PSBlocks.ALL_BARRELS.toArray(Block[]::new));
        valueLookupBuilder(PSTags.Blocks.DRYING_TABLES).add(
                PSBlocks.DRYING_TABLE, PSBlocks.IRON_DRYING_TABLE
        );
        valueLookupBuilder(PSTags.Blocks.LATTICES).add(
                PSBlocks.LATTICE,
                PSBlocks.WINE_GRAPE_LATTICE,
                PSBlocks.MORNING_GLORY_LATTICE
        );

        valueLookupBuilder(BlockTags.FLOWER_POTS).add(
                PSBlocks.POTTED_CANNABIS, PSBlocks.POTTED_COCA, PSBlocks.POTTED_COFFEA, PSBlocks.POTTED_HOP,
                PSBlocks.POTTED_MORNING_GLORY, PSBlocks.POTTED_TOBACCO
        );

        valueLookupBuilder(PSTags.Blocks.NIGHTSHADE).add(
                PSBlocks.BELLADONNA, PSBlocks.JIMSONWEED, PSBlocks.TOMATOES
        );

        valueLookupBuilder(BlockTags.AXE_MINEABLE).add(
                PSBlocks.DRYING_TABLE,
                PSBlocks.MASH_TUB,
                PSBlocks.MASH_TUB_EDGE,
                PSBlocks.DISTILLERY,
                PSBlocks.FLASK
        ).addTag(PSTags.Blocks.BARRELS).addTag(PSTags.Blocks.LATTICES);

        valueLookupBuilder(BlockTags.PICKAXE_MINEABLE).add(
                PSBlocks.IRON_DRYING_TABLE,
                PSBlocks.BUNSEN_BURNER,
                PSBlocks.TRAY,
                PSBlocks.GLASS_TUBE,
                PSBlocks.GLASS_VALVE
        );
    }

    private void addJuniperWoodset() {
        valueLookupBuilder(BlockTags.LEAVES).add(PSBlocks.JUNIPER_LEAVES);
        valueLookupBuilder(BlockTags.HOE_MINEABLE).add(PSBlocks.JUNIPER_LEAVES);
        valueLookupBuilder(PSTags.Blocks.JUNIPER_LOGS).add(PSBlocks.JUNIPER_LOG, PSBlocks.JUNIPER_WOOD, PSBlocks.STRIPPED_JUNIPER_LOG, PSBlocks.STRIPPED_JUNIPER_WOOD);
        valueLookupBuilder(BlockTags.LOGS).addTag(PSTags.Blocks.JUNIPER_LOGS);
        valueLookupBuilder(BlockTags.LOGS_THAT_BURN).addTag(PSTags.Blocks.JUNIPER_LOGS);
        valueLookupBuilder(BlockTags.PLANKS).add(PSBlocks.JUNIPER_PLANKS);
        addSign(PSBlocks.JUNIPER_SIGN, PSBlocks.JUNIPER_WALL_SIGN, PSBlocks.JUNIPER_HANGING_SIGN, PSBlocks.JUNIPER_WALL_HANGING_SIGN);
        addSapling(PSBlocks.JUNIPER_SAPLING, PSBlocks.POTTED_JUNIPER_SAPLING);
        valueLookupBuilder(BlockTags.WOODEN_BUTTONS).add(PSBlocks.JUNIPER_BUTTON);
        valueLookupBuilder(BlockTags.WOODEN_DOORS).add(PSBlocks.JUNIPER_DOOR);
        valueLookupBuilder(BlockTags.FENCE_GATES).add(PSBlocks.JUNIPER_FENCE_GATE);
        valueLookupBuilder(BlockTags.WOODEN_FENCES).add(PSBlocks.JUNIPER_FENCE);
        valueLookupBuilder(BlockTags.PRESSURE_PLATES).add(PSBlocks.JUNIPER_PRESSURE_PLATE);
        valueLookupBuilder(BlockTags.WOODEN_PRESSURE_PLATES).add(PSBlocks.JUNIPER_PRESSURE_PLATE);
        valueLookupBuilder(BlockTags.SLABS).add(PSBlocks.JUNIPER_SLAB);
        valueLookupBuilder(BlockTags.WOODEN_SLABS).add(PSBlocks.JUNIPER_SLAB);
        valueLookupBuilder(BlockTags.STAIRS).add(PSBlocks.JUNIPER_STAIRS);
        valueLookupBuilder(BlockTags.WOODEN_STAIRS).add(PSBlocks.JUNIPER_STAIRS);
        valueLookupBuilder(BlockTags.TRAPDOORS).add(PSBlocks.JUNIPER_TRAPDOOR);
        valueLookupBuilder(BlockTags.WOODEN_TRAPDOORS).add(PSBlocks.JUNIPER_TRAPDOOR);
    }

    private void addSign(Block standing, Block wall, Block hanging, Block wallHanging) {
        valueLookupBuilder(BlockTags.STANDING_SIGNS).add(standing);
        valueLookupBuilder(BlockTags.WALL_SIGNS).add(wall);

        valueLookupBuilder(BlockTags.CEILING_HANGING_SIGNS).add(hanging);
        valueLookupBuilder(BlockTags.WALL_HANGING_SIGNS).add(wallHanging);
    }

    private void addSapling(Block sapling, Block potted) {
        valueLookupBuilder(BlockTags.SAPLINGS).add(sapling);
        valueLookupBuilder(BlockTags.FLOWER_POTS).add(potted);
    }
}
