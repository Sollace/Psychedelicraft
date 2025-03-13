package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import ivorius.psychedelicraft.PSConventionalTags;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.datagen.providers.PSBlockFamilies;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSSubPredicates;
import ivorius.psychedelicraft.recipe.FluidIngredient;
import ivorius.psychedelicraft.recipe.FluidModifyingResult;
import ivorius.psychedelicraft.recipe.OptionalFluidIngredient;
import ivorius.psychedelicraft.recipe.PouringRecipe;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
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
        offerChemistryUpdateRecipes(exporter);
        offerDryingRecipes(exporter);
        offerLiquirRecipes(exporter);
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
        RecepticalAlteringShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.BOTTLE)
            .input(PSItems.MOLOTOV_COCKTAIL).criterion(hasItem(PSItems.MOLOTOV_COCKTAIL), conditionsFromItem(PSItems.MOLOTOV_COCKTAIL))
            .offerTo(exporter, Psychedelicraft.id(convertBetween(PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL)));
        RecepticalAlteringShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.MOLOTOV_COCKTAIL)
            .input(PSItems.BOTTLE).criterion(hasItem(PSItems.BOTTLE), conditionsFromItem(PSItems.BOTTLE))
            .input(ItemTags.WOOL)
            .offerTo(exporter, Psychedelicraft.id(convertBetween(PSItems.MOLOTOV_COCKTAIL, PSItems.BOTTLE)));
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

        FluidAwareShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.OBSIDIAN_BOTTLE)
            .input(FluidIngredient.builder().fluid(SimpleFluid.of(Fluids.WATER)).level(FluidVolumes.BUCKET).build(), PSItems.FILLED_BUCKET)
            .input(FluidIngredient.builder().fluid(SimpleFluid.of(Fluids.LAVA)).level(FluidVolumes.GLASS_BOTTLE).build(), PSItems.FILLED_GLASS_BOTTLE)
            .criterion("has_lava_bottle", conditionsFromPredicates(ItemPredicate.Builder.create()
                    .items(PSItems.FILLED_GLASS_BOTTLE)
                    .subPredicate(PSSubPredicates.FLUIDS, ItemFluids.Predicate.builder()
                            .fluid(SimpleFluid.of(Fluids.LAVA))
                            .amount(IntRange.atLeast(FluidVolumes.GLASS_BOTTLE))
                            .build())))
            .discard(Items.GLASS_BOTTLE)
            .offerTo(exporter, Psychedelicraft.id(convertBetween(PSItems.OBSIDIAN_BOTTLE, PSItems.FILLED_GLASS_BOTTLE)));
        FluidAwareShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.OBSIDIAN_BOTTLE)
            .input(Items.WATER_BUCKET)
            .input(FluidIngredient.builder().fluid(SimpleFluid.of(Fluids.LAVA)).level(FluidVolumes.GLASS_BOTTLE).build(), PSItems.FILLED_GLASS_BOTTLE)
            .criterion("has_lava_bottle", conditionsFromPredicates(ItemPredicate.Builder.create()
                    .items(PSItems.FILLED_GLASS_BOTTLE)
                    .subPredicate(PSSubPredicates.FLUIDS, ItemFluids.Predicate.builder()
                            .fluid(SimpleFluid.of(Fluids.LAVA))
                            .amount(IntRange.atLeast(FluidVolumes.GLASS_BOTTLE))
                            .build())))
            .discard(Items.GLASS_BOTTLE)
            .offerTo(exporter);
        offerSingleOutputShapelessRecipe(exporter, PSItems.OBSIDIAN_DUST, PSItems.OBSIDIAN_BOTTLE, "obsidian_bottle");
        offerCompactingRecipe(exporter, RecipeCategory.MISC, Items.OBSIDIAN, PSItems.OBSIDIAN_BOTTLE);

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
            .offerTo(exporter, Psychedelicraft.id("hash_muffin_with_dye"));
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

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.RIFT_JAR)
            .input('O', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .input('-', ItemTags.PLANKS)
            .input('G', Items.GOLD_INGOT)
            .input('I', Items.IRON_INGOT)
            .pattern("O-O")
            .pattern("GO ")
            .pattern("OIO")
            .offerTo(exporter);
    }

    private void offerChemistryUpdateRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.BUNSEN_BURNER)
            .input('r', Items.REDSTONE).criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
            .input('n', Items.IRON_NUGGET).criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
            .pattern("nrn")
            .pattern("n n")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.TRAY)
            .input('n', Items.IRON_NUGGET).criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
            .input('i', Items.IRON_INGOT).criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
            .pattern("n n")
            .pattern("iii")
            .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, Items.GLASS_PANE)
            .input('#', PSItems.BROKEN_GLASS).criterion(hasItem(PSItems.BROKEN_GLASS), conditionsFromItem(PSItems.BROKEN_GLASS))
            .pattern("###")
            .pattern("###")
            .offerTo(exporter, Psychedelicraft.id(convertBetween(Items.GLASS_PANE, PSItems.BROKEN_GLASS)));
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.GLASS_TUBE)
            .input('-', Items.GLASS_PANE).criterion(hasItem(Items.GLASS_PANE), conditionsFromItem(Items.GLASS_PANE))
            .pattern("---")
            .pattern("   ")
            .pattern("---")
            .offerTo(exporter);

        FluidAwareShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.LSA_SQUARE)
            .input(Items.PAPER).criterion(hasItem(Items.PAPER), conditionsFromItem(Items.PAPER))
            .input(FluidIngredient.builder().fluid(PSFluids.MORNING_GLORY_EXTRACT).attribute("distillation", 2).build(), PSTags.Items.DRINK_RECEPTICALS)
            .offerTo(exporter);

        offerReactingRecipes(exporter);
    }

    private void offerReactingRecipes(RecipeExporter exporter) {
        offerReacting(exporter, PSFluids.BELLADONA_EXTRACT, PSItems.BELLADONNA_SEEDS);
        offerReacting(exporter, PSFluids.JIMSONWEED_EXTRACT, PSItems.JIMSONWEED_SEEDS);
        offerReacting(exporter, PSFluids.MORNING_GLORY_EXTRACT, PSTags.Items.MORNING_GLORY_INGREDIENTS);
        offerReacting(exporter, PSFluids.MORPHINE, Items.POPPY);

        ReactingRecipeJsonBuilder.create(RecipeCategory.BREWING, PSFluids.PETROLIUM.getDefaultStack(5))
            .input(Items.COAL).criterion(hasItem(Items.COAL), conditionsFromItem(Items.COAL))
            .offerTo(exporter);
        ReactingRecipeJsonBuilder.create(RecipeCategory.BREWING, PSFluids.PETROLIUM.getDefaultStack(45))
            .input(Items.COAL_BLOCK).criterion(hasItem(Items.COAL_BLOCK), conditionsFromItem(Items.COAL_BLOCK))
            .offerTo(exporter, Psychedelicraft.id("petroleum_from_coal_block"));
    }

    private void offerDryingRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.DRYING_TABLE)
            .input('#', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .input('R', Items.REDSTONE)
            .pattern("###")
            .pattern("#R#")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.IRON_DRYING_TABLE)
            .input('#', ItemTags.PLANKS)
            .input('R', Items.REDSTONE)
            .input('I', Items.IRON_INGOT).criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
            .pattern("#I#")
            .pattern("IRI")
            .offerTo(exporter);

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

    private void offerLiquirRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PSItems.LATTICE)
            .input('O', ItemTags.PLANKS)
            .input('I', ConventionalItemTags.WOODEN_RODS).criterion("has_stick", conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .pattern("III")
            .pattern("IOI")
            .pattern("OIO")
            .offerTo(exporter);

        ComplexRecipeJsonBuilder.create(PouringRecipe::new).offerTo(exporter, "pour_drink");

        SmeltingFluidRecipeJsonBuilder.create(OptionalFluidIngredient.of(FluidIngredient.builder()
                    .fluid(PSFluids.COFFEE).build()), RecipeCategory.FOOD, 0.2F, 200)
            .modification("warmth", FluidModifyingResult.Ops.ADD, 1)
            .criterion("has_cold_coffee", conditionsFromPredicates(ItemPredicate.Builder.create()
                    .subPredicate(PSSubPredicates.FLUIDS, ItemFluids.Predicate.builder()
                            .fluid(PSFluids.COFFEE)
                            .attribute("warmth", IntRange.atMost(1))
                            .build())))
            .offerTo(exporter, Psychedelicraft.id("hot_coffee"));

        offerMashingRecipes(exporter);
        offerMixingRecipes(exporter);
    }

    private void offerMashingRecipes(RecipeExporter exporter) {
        offerMashing(exporter, PSFluids.AGAVE, PSItems.AGAVE_LEAF);
        offerMashing(exporter, PSFluids.APPLE, PSConventionalTags.Items.APPLES);
        offerMashing(exporter, PSFluids.BANANA, PSConventionalTags.Items.BANANAS);
        offerMashing(exporter, PSFluids.CORN, PSConventionalTags.Items.CORN);
        offerMashing(exporter, PSFluids.HONEY, PSConventionalTags.Items.HONEY);
        offerMashing(exporter, PSFluids.PINEAPPLE, PSConventionalTags.Items.PINEAPPLES);
        offerMashing(exporter, PSFluids.POTATO, PSConventionalTags.Items.POTATO);
        offerMashing(exporter, PSFluids.RED_GRAPES, PSConventionalTags.Items.GRAPES);
        offerMashing(exporter, PSFluids.RICE, PSConventionalTags.Items.RICE);
        offerMashing(exporter, PSFluids.TOMATO, PSConventionalTags.Items.TOMATOES);
        offerMashing(exporter, PSFluids.SUGAR_CANE, Items.SUGAR_CANE);
        offerMashing(exporter, PSFluids.WHEAT, Items.WHEAT);
        MashingRecipeJsonBuilder.create(RecipeCategory.FOOD, PSFluids.JUNIPER.getDefaultStack())
            .input(PSItems.JUNIPER_BERRIES, 4).criterion(hasItem(PSItems.JUNIPER_BERRIES), conditionsFromItem(PSItems.JUNIPER_BERRIES))
            .input(PSConventionalTags.Items.GRAPES, 2)
            .input(Items.SUGAR)
            .input(Items.WHEAT)
            .offerTo(exporter);
        MashingRecipeJsonBuilder.create(RecipeCategory.FOOD, PSFluids.WHEAT_HOP.getDefaultStack())
            .input(Items.WHEAT, 6)
            .input(PSItems.HOP_CONES, 2).criterion(hasItem(PSItems.HOP_CONES), conditionsFromItem(PSItems.HOP_CONES))
            .offerTo(exporter);
    }

    private void offerMixingRecipes(RecipeExporter exporter) {
        offerMixing(exporter, PSFluids.AGAVE, PSItems.AGAVE_LEAF);
        offerMixing(exporter, PSFluids.CANNABIS_TEA, PSItems.CANNABIS_LEAF);
        offerMixing(exporter, PSFluids.COCA_TEA, PSItems.COCA_LEAVES);
        offerMixing(exporter, PSFluids.PEYOTE_JUICE, PSItems.PEYOTE);
        offerMixing(exporter, PSFluids.COFFEE, PSItems.COFFEE_BEANS, PSTags.Items.SUITABLE_HOT_DRINK_RECEPTICALS);

        MixingRecipeJsonBuilder.create(RecipeCategory.FOOD, PSFluids.BATH_SALTS, 500)
            .input(Items.LAVA_BUCKET)
            .input(PSItems.OBSIDIAN_DUST).criterion(hasItem(PSItems.OBSIDIAN_DUST), conditionsFromItem(PSItems.OBSIDIAN_DUST))
            .receptical(PSTags.Items.DRUG_RECEPTICALS)
            .offerTo(exporter);
        MixingRecipeJsonBuilder.create(RecipeCategory.FOOD, PSFluids.CAFFEINE, 500)
            .input(PSItems.COFFEE_BEANS, 2).criterion(hasItem(PSItems.COFFEE_BEANS), conditionsFromItem(PSItems.COFFEE_BEANS))
            .receptical(PSTags.Items.DRUG_RECEPTICALS)
            .offerTo(exporter);
        offerMixing(exporter, PSFluids.COCAINE, PSItems.COCAINE_POWDER, PSTags.Items.DRUG_RECEPTICALS);

    }

    private static void offerReacting(RecipeExporter exporter, SimpleFluid fluid, ItemConvertible input) {
        ReactingRecipeJsonBuilder.create(RecipeCategory.BREWING, fluid.getDefaultStack(50))
            .input(input).criterion(hasItem(input), conditionsFromItem(input))
            .byProduct(Items.COAL)
            .offerTo(exporter);
    }

    private static void offerReacting(RecipeExporter exporter, SimpleFluid fluid, TagKey<Item> input) {
        ReactingRecipeJsonBuilder.create(RecipeCategory.BREWING, fluid.getDefaultStack(50))
            .input(input).criterion("has_" + input.id().getPath(), conditionsFromTag(input))
            .byProduct(Items.COAL)
            .offerTo(exporter);
    }

    private static void offerMixing(RecipeExporter exporter, SimpleFluid output, ItemConvertible input) {
        offerMixing(exporter, output, input, PSTags.Items.DRINK_RECEPTICALS);
    }

    private static void offerMixing(RecipeExporter exporter, SimpleFluid output, ItemConvertible input, TagKey<Item> receptical) {
        MixingRecipeJsonBuilder.create(RecipeCategory.FOOD, output, 500)
            .input(input, 2).criterion(hasItem(input), conditionsFromItem(input))
            .receptical(receptical)
            .offerTo(exporter);
    }

    private static void offerMashing(RecipeExporter exporter, SimpleFluid output, ItemConvertible input) {
        MashingRecipeJsonBuilder.create(RecipeCategory.FOOD, output.getDefaultStack())
            .input(input, 8).criterion(hasItem(input), conditionsFromItem(input))
            .offerTo(exporter);
    }

    private static void offerMashing(RecipeExporter exporter, SimpleFluid output, TagKey<Item> input) {
        MashingRecipeJsonBuilder.create(RecipeCategory.FOOD, output.getDefaultStack())
            .input(input, 8).criterion("has_" + input.id().getPath(), conditionsFromTag(input))
            .offerTo(exporter);
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
