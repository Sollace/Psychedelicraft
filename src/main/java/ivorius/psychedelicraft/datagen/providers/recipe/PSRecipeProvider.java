package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.datagen.providers.PSBlockFamilies;
import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

public class PSRecipeProvider extends FabricRecipeProvider {

    private final CompletableFuture<WrapperLookup> registries;

    public PSRecipeProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registries) {
        super(output, registries);
        this.registries = registries;
    }

    @Override
    public void generate(RecipeExporter exporter) {
        var items = registries.getNow(null).getWrapperOrThrow(RegistryKeys.ITEM);

        PSBlocks.ALL_BARRELS.stream().forEach(block -> {
            offerBarrel(exporter, block, lookupItem(items, Registries.BLOCK.getId(block).withPath(p -> p.replace("barrel", "planks"))));
        });

        offerJuniperWoodset(exporter);
        offerSmokingImpliments(exporter);
        offerDrinkHolders(exporter);
        offerDrugRecipes(exporter);
        offerDryingRecipes(exporter);
    }

    private void offerSmokingImpliments(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, PSItems.SMOKING_PIPE)
            .input('W', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .input('S', ConventionalItemTags.WOODEN_RODS).criterion("has_stick", conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .input('I', Items.IRON_INGOT)
            .pattern("  I")
            .pattern(" S ")
            .pattern("WS ")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, PSItems.CIGAR)
            .input('T', PSItems.DRIED_TOBACCO).criterion(hasItem(PSItems.DRIED_TOBACCO), conditionsFromItem(PSItems.DRIED_TOBACCO))
            .input('P', Items.PAPER)
            .pattern("TTT")
            .pattern("TTT")
            .pattern("PPP")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, PSItems.BONG)
            .input('G', Items.GLASS).criterion(hasItem(Items.GLASS), conditionsFromItem(Items.GLASS))
            .input('P', Items.GLASS_PANE)
            .pattern(" P ")
            .pattern("G G")
            .pattern("GGG")
            .offerTo(exporter);
        offerSmokeable(exporter, PSItems.CIGARETTE, PSItems.DRIED_TOBACCO);
        offerSmokeable(exporter, PSItems.JOINT, PSItems.DRIED_CANNABIS_BUDS);
        offerSmokeable(exporter, PSItems.PEYOTE_JOINT, PSItems.DRIED_PEYOTE);
        // TODO: blunt
    }

    private void offerDrinkHolders(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.WOODEN_MUG)
            .input('#', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.STONE_CUP)
            .input('#', Items.CLAY_BALL).criterion(hasItem(Items.CLAY_BALL), conditionsFromItem(Items.CLAY_BALL))
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.GLASS_CHALICE)
            .input('#', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .pattern("# #")
            .pattern(" # ")
            .pattern(" # ")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.BOTTLE)
            .input('#', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .pattern(" # ")
            .pattern("# #")
            .pattern("###")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.PAPER_BAG, 2)
            .input('#', Items.PAPER).criterion(hasItem(Items.PAPER), conditionsFromItem(Items.PAPER))
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .offerTo(exporter);
        offerSingleOutputShapelessRecipe(exporter, PSItems.SHOT_GLASS, Items.GLASS, "shot_glass");

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, PSItems.MASH_TUB)
            .input('#', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .input('I', Items.IRON_INGOT)
            .pattern("# #")
            .pattern("I I")
            .pattern("###")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, PSItems.FLASK)
            .input('#', Items.COPPER_INGOT).criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
            .input('G', ConventionalItemTags.GLASS_BLOCKS)
            .pattern(" # ")
            .pattern("#G#")
            .pattern("###")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, PSItems.DISTILLERY)
            .input('#', Items.COPPER_INGOT)
            .input('D', PSItems.FLASK).criterion(hasItem(PSItems.FLASK), conditionsFromItem(PSItems.FLASK))
            .pattern("##")
            .pattern("D ")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, PSItems.BOTTLE_RACK)
            .input('#', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .input('I', ConventionalItemTags.WOODEN_RODS).criterion("has_stick", conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .pattern("I#I")
            .pattern("#I#")
            .pattern("I#I")
            .offerTo(exporter);
    }

    private void offerJuniperWoodset(RecipeExporter exporter) {
        generateFamily(exporter, PSBlockFamilies.JUNIPER, FeatureSet.of(FeatureFlags.VANILLA));
        offerPlanksRecipe(exporter, PSBlocks.JUNIPER_PLANKS, PSTags.Items.JUNIPER_LOGS, 4);
        offerBarkBlockRecipe(exporter, PSBlocks.JUNIPER_WOOD, PSBlocks.JUNIPER_LOG);
        offerBarkBlockRecipe(exporter, PSBlocks.STRIPPED_JUNIPER_WOOD, PSBlocks.STRIPPED_JUNIPER_LOG);
        offerBoatRecipe(exporter, PSItems.JUNIPER_BOAT, PSBlocks.JUNIPER_PLANKS);
        offerChestBoatRecipe(exporter, PSItems.JUNIPER_CHEST_BOAT, PSItems.JUNIPER_BOAT);
        offerHangingSignRecipe(exporter, PSBlocks.JUNIPER_HANGING_SIGN, PSBlocks.JUNIPER_PLANKS);
    }

    private void offerDrugRecipes(RecipeExporter exporter) {

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.SYRINGE)
            .input('I', Items.IRON_INGOT).criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
            .input('G', ConventionalItemTags.GLASS_BLOCKS)
            .pattern("I")
            .pattern("G")
            .offerTo(exporter);

        offerSingleOutputShapelessRecipe(exporter, PSItems.COCAINE_POWDER, PSItems.DRIED_COCA_LEAVES, "drugs");
        offer2x2CompactingRecipe(exporter, RecipeCategory.MISC, PSItems.MORPHINE_TABLET, PSItems.HEROINE_POWDER);
        // TODO: Different pill designs using dyes
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.EXTACY, 2)
            .input('#', PSItems.CRYSTAL_METH)
            .pattern("##")
            .pattern("##")
            .criterion(hasItem(PSItems.CRYSTAL_METH), conditionsFromItem(PSItems.CRYSTAL_METH))
            .offerTo(exporter);

        offerSingleOutputShapelessRecipe(exporter, PSItems.OBSIDIAN_DUST, PSItems.OBSIDIAN_BOTTLE, "obsidian_bottle");
        offerCompactingRecipe(exporter, RecipeCategory.MISC, Items.OBSIDIAN, PSItems.OBSIDIAN_DUST);

        offerShapelessRecipe(exporter, PSItems.TOMATO_SEEDS, PSItems.TOMATO, "seeds", 6);
        offerShapelessRecipe(exporter, PSItems.BELLADONNA_SEEDS, PSItems.BELLADONNA_BERRIES, "seeds", 6);
        offerShapelessRecipe(exporter, PSItems.JIMSONWEED_SEEDS, PSItems.JIMSONWEED_SEED_POD, "seeds", 6);
        offerShapelessRecipe(exporter, PSItems.MORNING_GLORY_SEEDS, PSItems.MORNING_GLORY, "seeds", 6);
        offerSmelting(exporter, List.of(PSItems.COFFEA_CHERRIES), RecipeCategory.FOOD, PSItems.COFFEE_BEANS, 0.2F, 100, "drugs");

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.HASH_MUFFIN)
            .input('X', Items.LIGHT_BLUE_DYE)
            .input('#', Items.WHEAT).criterion(hasItem(Items.WHEAT), conditionsFromItem(Items.WHEAT))
            .input('L', PSItems.DRIED_CANNABIS_LEAF).criterion(hasItem(PSItems.DRIED_CANNABIS_LEAF), conditionsFromItem(PSItems.DRIED_CANNABIS_LEAF))
            .pattern("LLL")
            .pattern("#X#")
            .pattern("LLL")
            .offerTo(exporter, "hash_muffin_with_dye");
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.HASH_MUFFIN)
            .input('#', Items.WHEAT).criterion(hasItem(Items.WHEAT), conditionsFromItem(Items.WHEAT))
            .input('L', PSItems.DRIED_CANNABIS_LEAF).criterion(hasItem(PSItems.DRIED_CANNABIS_LEAF), conditionsFromItem(PSItems.DRIED_CANNABIS_LEAF))
            .pattern("LLL")
            .pattern("###")
            .pattern("LLL")
            .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.HARMONIUM)
            .input(Items.GLOWSTONE_DUST)
            .input(PSItems.DRIED_TOBACCO)
            .group("drugs")
            .criterion(hasItem(Items.GLOWSTONE_DUST), conditionsFromItem(Items.GLOWSTONE_DUST))
            .offerTo(exporter);
    }

    private void offerDryingRecipes(RecipeExporter exporter) {
        offerDrying(exporter, Items.BROWN_MUSHROOM, PSItems.BROWN_MAGIC_MUSHROOMS, 3, 0.2F, 0.4F, "magic_mushrooms");
        offerDrying(exporter, Items.RED_MUSHROOM, PSItems.RED_MAGIC_MUSHROOMS, 3, 0.2F, 0.4F, "magic_mushrooms");
        offerDrying(exporter, PSItems.COCA_LEAVES, PSItems.DRIED_COCA_LEAVES, 3, 0.2F, 1, "leaves");
        offerDrying(exporter, PSItems.BELLADONNA_LEAF, PSItems.DRIED_BELLADONNA_LEAF, 3, 0.2F, 0.3F, "leaves");
        offerDrying(exporter, PSItems.TOBACCO_LEAVES, PSItems.DRIED_TOBACCO, 3, 0.2F, 0.6F, "leaves");
        offerDrying(exporter, PSItems.CANNABIS_BUDS, PSItems.DRIED_CANNABIS_BUDS, 3, 0.2F, 1, "buds");
        offerDrying(exporter, PSItems.CANNABIS_LEAF, PSItems.DRIED_CANNABIS_LEAF, 3, 0.2F, 0.6F, "leaves");
        offerDrying(exporter, PSItems.JIMSONWEED_LEAF, PSItems.DRIED_JIMSONWEED_LEAF, 3, 0.2F, 1.1F, "leaves");
        offerDrying(exporter, PSItems.PEYOTE, PSItems.DRIED_PEYOTE, 3, 0.2F, 1.5F, "peyote");
    }

    private static void offerDrying(
            RecipeExporter exporter, ItemConvertible input,
            ItemConvertible output, int count, float experience, float cookingTime, String group) {
        DryingRecipeJsonBuilder.create(Ingredient.ofItems(input), RecipeCategory.FOOD, output, count, experience, cookingTime)
            .criterion(hasItem(input), conditionsFromItem(input))
            .group(group)
            .offerTo(exporter);
    }

    private static void offerSmokeable(RecipeExporter exporter, ItemConvertible result, ItemConvertible filling) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, result)
            .input('#', filling).criterion(hasItem(filling), conditionsFromItem(filling))
            .input('-', Items.PAPER)
            .pattern("-")
            .pattern("#")
            .pattern("-")
            .offerTo(exporter);
    }

    private static void offerBarrel(RecipeExporter exporter, ItemConvertible result, ItemConvertible planks) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, result)
            .input('#', planks).criterion(hasItem(planks), conditionsFromItem(planks))
            .input('I', Items.IRON_INGOT)
            .input('S', ConventionalItemTags.WOODEN_RODS).criterion("has_stick", conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .pattern(" I ")
            .pattern("# #")
            .pattern("S#S")
            .offerTo(exporter);
    }

    private static Item lookupItem(RegistryEntryLookup<Item> lookup, Identifier id) {
        return lookup
            .getOptional(RegistryKey.of(RegistryKeys.ITEM, id))
            .orElseGet(() -> lookup.getOrThrow(RegistryKey.of(RegistryKeys.ITEM, Identifier.ofVanilla(id.getPath()))))
            .value();
    }

}
