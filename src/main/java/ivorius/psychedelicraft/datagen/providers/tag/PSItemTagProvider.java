package ivorius.psychedelicraft.datagen.providers.tag;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import ivorius.psychedelicraft.PSConventionalTags;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.datagen.Datagen;
import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public class PSItemTagProvider extends FabricTagProvider.ItemTagProvider {
    private final PSBlockTagProvider blockTagProvider;

    public PSItemTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture, PSBlockTagProvider blockTagProvider) {
        super(output, completableFuture);
        this.blockTagProvider = blockTagProvider;
    }

    @Override
    public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        TagBuilder blockTagBuilder = Objects.requireNonNull(blockTagProvider, "Pass Block tag provider via constructor to use copy").getTagBuilder(blockTag);
        TagBuilder itemTagBuilder = getTagBuilder(itemTag);
        blockTagBuilder.build().forEach(entry -> {
            if (entry.canAdd(Registries.ITEM::containsId, tagId -> getTagBuilder(TagKey.of(RegistryKeys.ITEM, tagId)) != null)) {
                itemTagBuilder.add(entry);
            } else {
                Datagen.LOGGER.warn("Cannot copy missing entry {} to item tag {}", entry, itemTag.id());
            }
        });
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        copyBlockTags();
        exportConventionalTags();

        getOrCreateTagBuilder(ItemTags.SIGNS).add(PSItems.JUNIPER_SIGN);
        getOrCreateTagBuilder(PSTags.Items.MORNING_GLORY_INGREDIENTS).add(PSItems.MORNING_GLORY, PSItems.MORNING_GLORY_SEEDS);
        getOrCreateTagBuilder(PSTags.Items.BOTTLE_RACK_INSERTABLE).add(PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL);
        getOrCreateTagBuilder(PSTags.Items.BUNSEN_BURNER_INSERTABLE).add(Items.GLASS_BOTTLE, PSItems.FILLED_GLASS_BOTTLE, Items.POTION, PSItems.BOTTLE);
        getOrCreateTagBuilder(PSTags.Items.PLACEABLE_RECEPTICALS).add(PSItems.WOODEN_MUG, PSItems.STONE_CUP, PSItems.GLASS_CHALICE, PSItems.BOTTLE, PSItems.SHOT_GLASS);

        getOrCreateTagBuilder(PSTags.Items.SUITABLE_ALCOHOLIC_DRINK_RECEPTICALS).add(
                PSItems.SHOT_GLASS, PSItems.GLASS_CHALICE, PSItems.WOODEN_MUG,
                PSItems.FILLED_BOWL, Items.BOWL
        ).addTag(PSTags.Items.BOTTLE_RACK_INSERTABLE);
        getOrCreateTagBuilder(PSTags.Items.SUITABLE_HOT_DRINK_RECEPTICALS).add(PSItems.STONE_CUP);
        getOrCreateTagBuilder(PSTags.Items.DRUG_RECEPTICALS).add(PSItems.SYRINGE, PSItems.FILLED_GLASS_BOTTLE, Items.GLASS_BOTTLE);
        getOrCreateTagBuilder(PSTags.Items.DRINK_RECEPTICALS)
            .addTag(PSTags.Items.SUITABLE_ALCOHOLIC_DRINK_RECEPTICALS)
            .addTag(PSTags.Items.SUITABLE_HOT_DRINK_RECEPTICALS);
        getOrCreateTagBuilder(PSTags.Items.DRUG_CROP_SEEDS).add(PSItems.CANNABIS_SEEDS, PSItems.HOP_SEEDS, PSItems.TOBACCO_SEEDS, PSItems.COCA_SEEDS, PSItems.COFFEA_CHERRIES, PSItems.MORNING_GLORY_SEEDS);
        getOrCreateTagBuilder(PSTags.Items.CAN_GO_INTO_PAPER_BAG).add(
                Items.NETHER_WART, Items.SWEET_BERRIES, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM,
                Items.PUFFERFISH, Items.TROPICAL_FISH, Items.DRIED_KELP,
                Items.CARROT, Items.GOLDEN_CARROT,
                Items.BEETROOT, Items.POTATO, Items.BAKED_POTATO, Items.POISONOUS_POTATO,
                Items.MELON_SLICE, Items.SPIDER_EYE, Items.CHORUS_FLOWER, Items.ROTTEN_FLESH,
                Items.BONE, Items.BONE_MEAL,

                PSItems.HASH_MUFFIN, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL,
                PSItems.RED_MAGIC_MUSHROOMS, PSItems.BROWN_MAGIC_MUSHROOMS,
                PSItems.AGAVE_LEAF,
                PSItems.SYRINGE,
                PSItems.JOINT, PSItems.CIGARETTE, PSItems.CIGAR, PSItems.MORNING_GLORY
        ).forceAddTag(ConventionalItemTags.COOKIES_FOODS)
            .forceAddTag(ConventionalItemTags.BERRIES_FOODS)
            .forceAddTag(ConventionalItemTags.CANDIES_FOODS)
            .forceAddTag(ConventionalItemTags.MUSIC_DISCS)
            .forceAddTag(ConventionalItemTags.GEMS)
            .forceAddTag(ConventionalItemTags.DUSTS)
            .forceAddTag(ConventionalItemTags.DYES)
            .forceAddTag(ConventionalItemTags.COOKED_FISHES_FOODS)
            .forceAddTag(ConventionalItemTags.RAW_FISHES_FOODS)
            .forceAddTag(ItemTags.VILLAGER_PLANTABLE_SEEDS)
            .forceAddTag(ItemTags.BUTTONS)
            .forceAddTag(ItemTags.SMALL_FLOWERS)
            .forceAddTag(PSTags.Items.DRUG_CROP_SEEDS)
            .forceAddTag(PSConventionalTags.Items.APPLES)
            .forceAddTag(PSConventionalTags.Items.TOMATOES);
    }

    private void copyBlockTags() {
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(BlockTags.SLABS, ItemTags.SLABS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);

        copy(PSTags.Blocks.BARRELS, PSTags.Items.BARRELS);
        copy(PSTags.Blocks.DRYING_TABLES, PSTags.Items.DRYING_TABLES);
        copy(PSTags.Blocks.JUNIPER_LOGS, PSTags.Items.JUNIPER_LOGS);
    }

    private void exportConventionalTags() {
        getOrCreateTagBuilder(PSConventionalTags.Items.APPLES).add(Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
        getOrCreateTagBuilder(PSConventionalTags.Items.BANANAS);
        getOrCreateTagBuilder(PSConventionalTags.Items.PINEAPPLES);
        getOrCreateTagBuilder(PSConventionalTags.Items.CORN);
        getOrCreateTagBuilder(PSConventionalTags.Items.RICE);
        getOrCreateTagBuilder(PSConventionalTags.Items.GRAPES).add(PSItems.WINE_GRAPES);
        getOrCreateTagBuilder(PSConventionalTags.Items.HONEY).add(Items.HONEYCOMB, Items.HONEY_BOTTLE);
        getOrCreateTagBuilder(PSConventionalTags.Items.TOMATOES).add(PSItems.TOMATO);
        getOrCreateTagBuilder(ConventionalItemTags.BERRIES_FOODS).add(PSItems.BELLADONNA_BERRIES, PSItems.JUNIPER_BERRIES);
        getOrCreateTagBuilder(ConventionalItemTags.DUSTS).add(
                PSItems.OBSIDIAN_DUST,
                PSItems.COCAINE_POWDER, PSItems.HEROINE_POWDER
        );
    }
}
