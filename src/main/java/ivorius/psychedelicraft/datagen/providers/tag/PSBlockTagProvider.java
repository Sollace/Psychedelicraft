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

        getOrCreateTagBuilder(PSTags.Blocks.BARRELS).add(PSBlocks.ALL_BARRELS.toArray(Block[]::new));
        getOrCreateTagBuilder(PSTags.Blocks.DRYING_TABLES).add(
                PSBlocks.DRYING_TABLE, PSBlocks.IRON_DRYING_TABLE
        );
        getOrCreateTagBuilder(PSTags.Blocks.LATTICES).add(
                PSBlocks.LATTICE,
                PSBlocks.WINE_GRAPE_LATTICE,
                PSBlocks.MORNING_GLORY_LATTICE
        );

        getOrCreateTagBuilder(BlockTags.FLOWER_POTS).add(
                PSBlocks.POTTED_CANNABIS, PSBlocks.POTTED_COCA, PSBlocks.POTTED_COFFEA, PSBlocks.POTTED_HOP,
                PSBlocks.POTTED_MORNING_GLORY, PSBlocks.POTTED_TOBACCO
        );

        getOrCreateTagBuilder(PSTags.Blocks.NIGHTSHADE).add(
                PSBlocks.BELLADONNA, PSBlocks.JIMSONWEEED, PSBlocks.TOMATOES
        );

        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(
                PSBlocks.DRYING_TABLE,
                PSBlocks.MASH_TUB,
                PSBlocks.MASH_TUB_EDGE,
                PSBlocks.DISTILLERY,
                PSBlocks.FLASK
        ).addTag(PSTags.Blocks.BARRELS).addTag(PSTags.Blocks.LATTICES);

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(
                PSBlocks.IRON_DRYING_TABLE,
                PSBlocks.BUNSEN_BURNER,
                PSBlocks.TRAY,
                PSBlocks.GLASS_TUBE
        );
    }

    private void addJuniperWoodset() {
        getOrCreateTagBuilder(BlockTags.LEAVES).add(PSBlocks.JUNIPER_LEAVES);
        getOrCreateTagBuilder(BlockTags.HOE_MINEABLE).add(PSBlocks.JUNIPER_LEAVES);
        getOrCreateTagBuilder(PSTags.Blocks.JUNIPER_LOGS).add(PSBlocks.JUNIPER_LOG, PSBlocks.JUNIPER_WOOD, PSBlocks.STRIPPED_JUNIPER_LOG, PSBlocks.STRIPPED_JUNIPER_WOOD);
        getOrCreateTagBuilder(BlockTags.LOGS).addTag(PSTags.Blocks.JUNIPER_LOGS);
        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN).addTag(PSTags.Blocks.JUNIPER_LOGS);
        getOrCreateTagBuilder(BlockTags.PLANKS).add(PSBlocks.JUNIPER_PLANKS);
        addSign(PSBlocks.JUNIPER_SIGN, PSBlocks.JUNIPER_WALL_SIGN, PSBlocks.JUNIPER_HANGING_SIGN, PSBlocks.JUNIPER_WALL_HANGING_SIGN);
        addSapling(PSBlocks.JUNIPER_SAPLING, PSBlocks.POTTED_JUNIPER_SAPLING);
        getOrCreateTagBuilder(BlockTags.WOODEN_BUTTONS).add(PSBlocks.JUNIPER_BUTTON);
        getOrCreateTagBuilder(BlockTags.WOODEN_DOORS).add(PSBlocks.JUNIPER_DOOR);
        getOrCreateTagBuilder(BlockTags.FENCE_GATES).add(PSBlocks.JUNIPER_FENCE_GATE);
        getOrCreateTagBuilder(BlockTags.WOODEN_FENCES).add(PSBlocks.JUNIPER_FENCE);
        getOrCreateTagBuilder(BlockTags.PRESSURE_PLATES).add(PSBlocks.JUNIPER_PRESSURE_PLATE);
        getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES).add(PSBlocks.JUNIPER_PRESSURE_PLATE);
        getOrCreateTagBuilder(BlockTags.SLABS).add(PSBlocks.JUNIPER_SLAB);
        getOrCreateTagBuilder(BlockTags.WOODEN_SLABS).add(PSBlocks.JUNIPER_SLAB);
        getOrCreateTagBuilder(BlockTags.STAIRS).add(PSBlocks.JUNIPER_STAIRS);
        getOrCreateTagBuilder(BlockTags.WOODEN_STAIRS).add(PSBlocks.JUNIPER_STAIRS);
        getOrCreateTagBuilder(BlockTags.TRAPDOORS).add(PSBlocks.JUNIPER_TRAPDOOR);
        getOrCreateTagBuilder(BlockTags.WOODEN_TRAPDOORS).add(PSBlocks.JUNIPER_TRAPDOOR);
    }

    private void addSign(Block standing, Block wall, Block hanging, Block wallHanging) {
        getOrCreateTagBuilder(BlockTags.STANDING_SIGNS).add(standing);
        getOrCreateTagBuilder(BlockTags.WALL_SIGNS).add(wall);

        getOrCreateTagBuilder(BlockTags.CEILING_HANGING_SIGNS).add(hanging);
        getOrCreateTagBuilder(BlockTags.WALL_HANGING_SIGNS).add(wallHanging);
    }

    private void addSapling(Block sapling, Block potted) {
        getOrCreateTagBuilder(BlockTags.SAPLINGS).add(sapling);
        getOrCreateTagBuilder(BlockTags.FLOWER_POTS).add(potted);
    }
}
