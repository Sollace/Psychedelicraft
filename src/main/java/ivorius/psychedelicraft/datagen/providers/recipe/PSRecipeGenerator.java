package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.List;
import java.util.Optional;

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
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

class PSRecipeGenerator extends RecipeGenerator {
    private final RegistryEntryLookup<Item> items;
    PSRecipeGenerator(WrapperLookup registries, RecipeExporter exporter) {
        super(registries, exporter);
        items = registries.getOrThrow(RegistryKeys.ITEM);
    }

    @Override
    public void generate() {
        PSBlocks.ALL_BARRELS.stream().forEach(block -> {
            lookupItem(Registries.BLOCK.getId(block).withPath(p -> p.replace("barrel", "planks"))).ifPresent(planks -> {
                offerBarrel(block, planks);
            });
        });

        offerJuniperWoodset();
        offerSmokingImpliments();
        offerDrinkHolders();
        offerDrugRecipes();
        offerChemistryUpdateRecipes();
        offerDryingRecipes();
        offerLiquirRecipes();
    }

    private void offerSmokingImpliments() {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.TOOLS, PSItems.SMOKING_PIPE)
            .input('W', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .input('S', ConventionalItemTags.WOODEN_RODS).criterion("has_stick", conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .input('I', Items.IRON_INGOT)
            .pattern("  I")
            .pattern(" S ")
            .pattern("WS ")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.TOOLS, PSItems.CIGAR)
            .input('T', PSItems.DRIED_TOBACCO).criterion(hasItem(PSItems.DRIED_TOBACCO), conditionsFromItem(PSItems.DRIED_TOBACCO))
            .input('P', Items.PAPER)
            .pattern("TTT")
            .pattern("TTT")
            .pattern("PPP")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.TOOLS, PSItems.BONG)
            .input('G', Items.GLASS).criterion(hasItem(Items.GLASS), conditionsFromItem(Items.GLASS))
            .input('P', Items.GLASS_PANE)
            .pattern(" P ")
            .pattern("G G")
            .pattern("GGG")
            .offerTo(exporter);
        offerSmokeable(PSItems.CIGARETTE, PSItems.DRIED_TOBACCO);
        offerSmokeable(PSItems.JOINT, PSItems.DRIED_CANNABIS_BUDS);
        offerSmokeable(PSItems.PEYOTE_JOINT, PSItems.DRIED_PEYOTE);
        // TODO: blunt
    }

    private void offerDrinkHolders() {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.WOODEN_MUG)
            .input('#', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.STONE_CUP)
            .input('#', Items.CLAY_BALL).criterion(hasItem(Items.CLAY_BALL), conditionsFromItem(Items.CLAY_BALL))
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.GLASS_CHALICE)
            .input('#', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .pattern("# #")
            .pattern(" # ")
            .pattern(" # ")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.BOTTLE)
            .input('#', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .pattern(" # ")
            .pattern("# #")
            .pattern("###")
            .offerTo(exporter);
        RecepticalAlteringShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.BOTTLE)
            .input(PSItems.MOLOTOV_COCKTAIL).criterion(hasItem(PSItems.MOLOTOV_COCKTAIL), conditionsFromItem(PSItems.MOLOTOV_COCKTAIL))
            .offerTo(exporter, id(convertBetween(PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL)));
        RecepticalAlteringShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.MOLOTOV_COCKTAIL)
            .input(PSItems.BOTTLE).criterion(hasItem(PSItems.BOTTLE), conditionsFromItem(PSItems.BOTTLE))
            .input(ItemTags.WOOL)
            .offerTo(exporter, id(convertBetween(PSItems.MOLOTOV_COCKTAIL, PSItems.BOTTLE)));
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.PAPER_BAG, 2)
            .input('#', Items.PAPER).criterion(hasItem(Items.PAPER), conditionsFromItem(Items.PAPER))
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .offerTo(exporter);
        offerSingleOutputShapelessRecipe(PSItems.SHOT_GLASS, Items.GLASS, "shot_glass");

        ShapedRecipeJsonBuilder.create(items, RecipeCategory.REDSTONE, PSItems.MASH_TUB)
            .input('#', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .input('I', Items.IRON_INGOT)
            .pattern("# #")
            .pattern("I I")
            .pattern("###")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.REDSTONE, PSItems.FLASK)
            .input('#', Items.COPPER_INGOT).criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
            .input('G', ConventionalItemTags.GLASS_BLOCKS)
            .pattern(" # ")
            .pattern("#G#")
            .pattern("###")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.REDSTONE, PSItems.DISTILLERY)
            .input('#', Items.COPPER_INGOT)
            .input('D', PSItems.FLASK).criterion(hasItem(PSItems.FLASK), conditionsFromItem(PSItems.FLASK))
            .pattern("##")
            .pattern("D ")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, PSItems.BOTTLE_RACK)
            .input('#', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .input('I', ConventionalItemTags.WOODEN_RODS).criterion("has_stick", conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .pattern("I#I")
            .pattern("#I#")
            .pattern("I#I")
            .offerTo(exporter);
    }

    private void offerJuniperWoodset() {
        generateFamily(PSBlockFamilies.JUNIPER, FeatureSet.of(FeatureFlags.VANILLA));
        offerPlanksRecipe(PSBlocks.JUNIPER_PLANKS, PSTags.Items.JUNIPER_LOGS, 4);
        offerBarkBlockRecipe(PSBlocks.JUNIPER_WOOD, PSBlocks.JUNIPER_LOG);
        offerBarkBlockRecipe(PSBlocks.STRIPPED_JUNIPER_WOOD, PSBlocks.STRIPPED_JUNIPER_LOG);
        offerBoatRecipe(PSItems.JUNIPER_BOAT, PSBlocks.JUNIPER_PLANKS);
        offerChestBoatRecipe(PSItems.JUNIPER_CHEST_BOAT, PSItems.JUNIPER_BOAT);
        offerHangingSignRecipe(PSBlocks.JUNIPER_HANGING_SIGN, PSBlocks.JUNIPER_PLANKS);
    }

    private void offerDrugRecipes() {

        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.SYRINGE)
            .input('I', Items.IRON_INGOT).criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
            .input('G', ConventionalItemTags.GLASS_BLOCKS)
            .pattern("I")
            .pattern("G")
            .offerTo(exporter);

        offerSingleOutputShapelessRecipe(PSItems.COCAINE_POWDER, PSItems.DRIED_COCA_LEAVES, "drugs");

        FluidAwareShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.OBSIDIAN_BOTTLE)
            .input(FluidIngredient.builder().fluid(SimpleFluid.of(Fluids.WATER)).level(FluidVolumes.BUCKET).build(), PSItems.FILLED_BUCKET)
            .input(FluidIngredient.builder().fluid(SimpleFluid.of(Fluids.LAVA)).level(FluidVolumes.GLASS_BOTTLE).build(), PSItems.FILLED_GLASS_BOTTLE)
            .criterion("has_lava_bottle", conditionsFromPredicates(ItemPredicate.Builder.create()
                    .items(items, PSItems.FILLED_GLASS_BOTTLE)
                    .components(ComponentsPredicate.Builder.create().partial(PSSubPredicates.FLUIDS, ItemFluids.Predicate.builder()
                            .fluid(SimpleFluid.of(Fluids.LAVA))
                            .amount(IntRange.atLeast(FluidVolumes.GLASS_BOTTLE))
                            .build()).build())))
            .discard(Items.GLASS_BOTTLE)
            .offerTo(exporter, id(convertBetween(PSItems.OBSIDIAN_BOTTLE, PSItems.FILLED_GLASS_BOTTLE)));
        FluidAwareShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.OBSIDIAN_BOTTLE)
            .input(Items.WATER_BUCKET)
            .input(FluidIngredient.builder().fluid(SimpleFluid.of(Fluids.LAVA)).level(FluidVolumes.GLASS_BOTTLE).build(), PSItems.FILLED_GLASS_BOTTLE)
            .criterion("has_lava_bottle", conditionsFromPredicates(ItemPredicate.Builder.create()
                    .items(items, PSItems.FILLED_GLASS_BOTTLE)
                    .components(ComponentsPredicate.Builder.create().partial(PSSubPredicates.FLUIDS, ItemFluids.Predicate.builder()
                            .fluid(SimpleFluid.of(Fluids.LAVA))
                            .amount(IntRange.atLeast(FluidVolumes.GLASS_BOTTLE))
                            .build()).build())))
            .discard(Items.GLASS_BOTTLE)
            .offerTo(exporter);
        offerSingleOutputShapelessRecipe(PSItems.OBSIDIAN_DUST, PSItems.OBSIDIAN_BOTTLE, "obsidian_bottle");
        offerCompactingRecipe(RecipeCategory.MISC, Items.OBSIDIAN, PSItems.OBSIDIAN_BOTTLE);

        offerShapelessRecipe(PSItems.TOMATO_SEEDS, PSItems.TOMATO, "seeds", 6);
        offerShapelessRecipe(PSItems.BELLADONNA_SEEDS, PSItems.BELLADONNA_BERRIES, "seeds", 6);
        offerShapelessRecipe(PSItems.JIMSONWEED_SEEDS, PSItems.JIMSONWEED_SEED_POD, "seeds", 6);
        offerShapelessRecipe(PSItems.MORNING_GLORY_SEEDS, PSItems.MORNING_GLORY, "seeds", 6);
        offerSmelting(List.of(PSItems.COFFEA_CHERRIES), RecipeCategory.FOOD, PSItems.COFFEE_BEANS, 0.2F, 100, "drugs");

        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.HASH_MUFFIN)
            .input('X', Items.LIGHT_BLUE_DYE)
            .input('#', Items.WHEAT).criterion(hasItem(Items.WHEAT), conditionsFromItem(Items.WHEAT))
            .input('L', PSItems.DRIED_CANNABIS_LEAF).criterion(hasItem(PSItems.DRIED_CANNABIS_LEAF), conditionsFromItem(PSItems.DRIED_CANNABIS_LEAF))
            .pattern("LLL")
            .pattern("#X#")
            .pattern("LLL")
            .offerTo(exporter, id("hash_muffin_with_dye"));
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.HASH_MUFFIN)
            .input('#', Items.WHEAT).criterion(hasItem(Items.WHEAT), conditionsFromItem(Items.WHEAT))
            .input('L', PSItems.DRIED_CANNABIS_LEAF).criterion(hasItem(PSItems.DRIED_CANNABIS_LEAF), conditionsFromItem(PSItems.DRIED_CANNABIS_LEAF))
            .pattern("LLL")
            .pattern("###")
            .pattern("LLL")
            .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.HARMONIUM)
            .input(Items.GLOWSTONE_DUST)
            .input(PSItems.DRIED_TOBACCO)
            .group("drugs")
            .criterion(hasItem(Items.GLOWSTONE_DUST), conditionsFromItem(Items.GLOWSTONE_DUST))
            .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.RIFT_JAR)
            .input('O', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .input('-', ItemTags.PLANKS)
            .input('G', Items.GOLD_INGOT)
            .input('I', Items.IRON_INGOT)
            .pattern("O-O")
            .pattern("GO ")
            .pattern("OIO")
            .offerTo(exporter);
    }

    private void offerChemistryUpdateRecipes() {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.BUNSEN_BURNER)
            .input('r', Items.REDSTONE).criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
            .input('n', Items.IRON_NUGGET).criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
            .pattern("nrn")
            .pattern("n n")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.TRAY)
            .input('n', Items.IRON_NUGGET).criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
            .input('i', Items.IRON_INGOT).criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
            .pattern("n n")
            .pattern("iii")
            .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, Items.GLASS_PANE)
            .input('#', PSItems.BROKEN_GLASS).criterion(hasItem(PSItems.BROKEN_GLASS), conditionsFromItem(PSItems.BROKEN_GLASS))
            .pattern("###")
            .pattern("###")
            .offerTo(exporter, id(convertBetween(Items.GLASS_PANE, PSItems.BROKEN_GLASS)));
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.GLASS_TUBE)
            .input('-', Items.GLASS_PANE).criterion(hasItem(Items.GLASS_PANE), conditionsFromItem(Items.GLASS_PANE))
            .pattern("---")
            .pattern("   ")
            .pattern("---")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.GLASS_VALVE)
            .input('-', PSItems.GLASS_TUBE).criterion(hasItem(PSItems.GLASS_TUBE), conditionsFromItem(PSItems.GLASS_TUBE))
            .input('*', Items.IRON_INGOT).criterion(hasItem(PSItems.GLASS_TUBE), conditionsFromItem(PSItems.GLASS_TUBE))
            .pattern("*")
            .pattern("-")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.PUMP)
            .input('#', ItemTags.STONE_CRAFTING_MATERIALS)
            .input('-', PSItems.GLASS_TUBE).criterion(hasItem(PSItems.GLASS_VALVE), conditionsFromItem(PSItems.GLASS_VALVE))
            .input('*', Items.REDSTONE)
            .pattern("###")
            .pattern("#-#")
            .pattern("#*#")
            .offerTo(exporter);

        offer2x2CompactingRecipe(RecipeCategory.MISC, PSItems.MORPHINE_TABLET, PSItems.HEROINE_POWDER);
        // TODO: Different pill designs using dyes
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.EXTACY, 2)
            .input('#', PSItems.CRYSTAL_METH)
            .pattern("##")
            .pattern("##")
            .criterion(hasItem(PSItems.CRYSTAL_METH), conditionsFromItem(PSItems.CRYSTAL_METH))
            .offerTo(exporter);

        FluidAwareShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.LSA_SQUARE)
            .input(Items.PAPER).criterion(hasItem(Items.PAPER), conditionsFromItem(Items.PAPER))
            .input(FluidIngredient.builder().fluid(PSFluids.MORNING_GLORY_EXTRACT).attribute("distillation", 2).build())
            .offerTo(exporter);

        offerReactingRecipes();
        offerTrayRecipes();
    }

    private void offerReactingRecipes() {
        offerReacting(PSFluids.BELLADONA_EXTRACT, PSItems.BELLADONNA_SEEDS);
        offerReacting(PSFluids.JIMSONWEED_EXTRACT, PSItems.JIMSONWEED_SEEDS);
        offerReacting(PSFluids.MORNING_GLORY_EXTRACT, PSTags.Items.MORNING_GLORY_INGREDIENTS);
        offerReacting(PSFluids.MORPHINE, Items.POPPY);

        ReactingRecipeJsonBuilder.create(items, RecipeCategory.BREWING, PSFluids.PETROLIUM.getDefaultStack(5))
            .input(Items.COAL).criterion(hasItem(Items.COAL), conditionsFromItem(Items.COAL))
            .offerTo(exporter);
        ReactingRecipeJsonBuilder.create(items, RecipeCategory.BREWING, PSFluids.PETROLIUM.getDefaultStack(45))
            .input(Items.COAL_BLOCK).criterion(hasItem(Items.COAL_BLOCK), conditionsFromItem(Items.COAL_BLOCK))
            .offerTo(exporter, id("petroleum_from_coal_block"));
    }

    private void offerTrayRecipes() {
        HardeningRecipeJsonBuilder.create(RecipeCategory.BREWING, PSItems.CRACK_COCAINE)
            .base(FluidIngredient.builder().fluid(PSFluids.ETHANOL))
            .impurity(FluidIngredient.builder().fluid(PSFluids.COCAINE))
            .criterion(hasItem(PSItems.COCAINE_POWDER), conditionsFromItem(PSItems.COCAINE_POWDER))
            .offerTo(exporter);
        HardeningRecipeJsonBuilder.create(RecipeCategory.BREWING, PSItems.CRYSTAL_METH)
            .base(FluidIngredient.builder().fluid(PSFluids.MORNING_GLORY_EXTRACT))
            .criterion("has_morning_glory", conditionsFromTag(PSTags.Items.MORNING_GLORY_INGREDIENTS))
            .offerTo(exporter);
        HardeningRecipeJsonBuilder.create(RecipeCategory.BREWING, PSItems.HEROINE_POWDER)
            .base(FluidIngredient.builder().fluid(PSFluids.MORPHINE))
            .criterion(hasItem(Items.POPPY), conditionsFromItem(Items.POPPY))
            .offerTo(exporter);
        HardeningRecipeJsonBuilder.create(RecipeCategory.BREWING, PSItems.LSD_PILL)
            .base(FluidIngredient.builder().fluid(PSFluids.ACID))
            .criterion("has_morning_glory", conditionsFromTag(PSTags.Items.MORNING_GLORY_INGREDIENTS))
            .offerTo(exporter);
    }

    private void offerDryingRecipes() {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.DRYING_TABLE)
            .input('#', ItemTags.PLANKS).criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
            .input('R', Items.REDSTONE)
            .pattern("###")
            .pattern("#R#")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.IRON_DRYING_TABLE)
            .input('#', ItemTags.PLANKS)
            .input('R', Items.REDSTONE)
            .input('I', Items.IRON_INGOT).criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
            .pattern("#I#")
            .pattern("IRI")
            .offerTo(exporter);

        offerDrying(Items.BROWN_MUSHROOM, PSItems.BROWN_MAGIC_MUSHROOMS, 3, 0.2F, 0.4F, "magic_mushrooms");
        offerDrying(Items.RED_MUSHROOM, PSItems.RED_MAGIC_MUSHROOMS, 3, 0.2F, 0.4F, "magic_mushrooms");
        offerDrying(PSItems.COCA_LEAVES, PSItems.DRIED_COCA_LEAVES, 3, 0.2F, 1, "leaves");
        offerDrying(PSItems.BELLADONNA_LEAF, PSItems.DRIED_BELLADONNA_LEAF, 3, 0.2F, 0.3F, "leaves");
        offerDrying(PSItems.TOBACCO_LEAVES, PSItems.DRIED_TOBACCO, 3, 0.2F, 0.6F, "leaves");
        offerDrying(PSItems.CANNABIS_BUDS, PSItems.DRIED_CANNABIS_BUDS, 3, 0.2F, 1, "buds");
        offerDrying(PSItems.CANNABIS_LEAF, PSItems.DRIED_CANNABIS_LEAF, 3, 0.2F, 0.6F, "leaves");
        offerDrying(PSItems.JIMSONWEED_LEAF, PSItems.DRIED_JIMSONWEED_LEAF, 3, 0.2F, 1.1F, "leaves");
        offerDrying(PSItems.PEYOTE, PSItems.DRIED_PEYOTE, 3, 0.2F, 1.5F, "peyote");
    }

    private void offerLiquirRecipes() {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, PSItems.LATTICE)
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
                    .components(ComponentsPredicate.Builder.create().partial(PSSubPredicates.FLUIDS, ItemFluids.Predicate.builder()
                            .fluid(PSFluids.COFFEE)
                            .attribute("warmth", IntRange.atMost(1))
                            .build()).build())))
            .offerTo(exporter, id("hot_coffee"));

        offerMashingRecipes();
        offerMixingRecipes();
    }

    private void offerMashingRecipes() {
        offerMashing(PSFluids.AGAVE, PSItems.AGAVE_LEAF);
        offerMashing(PSFluids.APPLE, PSConventionalTags.Items.APPLES);
        offerMashing(PSFluids.BANANA, PSConventionalTags.Items.BANANAS);
        offerMashing(PSFluids.CORN, PSConventionalTags.Items.CORN);
        offerMashing(PSFluids.HONEY, PSConventionalTags.Items.HONEY);
        offerMashing(PSFluids.PINEAPPLE, PSConventionalTags.Items.PINEAPPLES);
        offerMashing(PSFluids.POTATO, PSConventionalTags.Items.POTATO);
        offerMashing(PSFluids.RED_GRAPES, PSConventionalTags.Items.GRAPES);
        offerMashing(PSFluids.RICE, PSConventionalTags.Items.RICE);
        offerMashing(PSFluids.TOMATO, PSConventionalTags.Items.TOMATOES);
        offerMashing(PSFluids.SUGAR_CANE, Items.SUGAR_CANE);
        offerMashing(PSFluids.WHEAT, Items.WHEAT);
        MashingRecipeJsonBuilder.create(items, RecipeCategory.FOOD, PSFluids.JUNIPER.getDefaultStack())
            .input(PSItems.JUNIPER_BERRIES, 4).criterion(hasItem(PSItems.JUNIPER_BERRIES), conditionsFromItem(PSItems.JUNIPER_BERRIES))
            .input(PSConventionalTags.Items.GRAPES, 2)
            .input(Items.SUGAR)
            .input(Items.WHEAT)
            .offerTo(exporter);
        MashingRecipeJsonBuilder.create(items, RecipeCategory.FOOD, PSFluids.WHEAT_HOP.getDefaultStack())
            .input(Items.WHEAT, 6)
            .input(PSItems.HOP_CONES, 2).criterion(hasItem(PSItems.HOP_CONES), conditionsFromItem(PSItems.HOP_CONES))
            .offerTo(exporter);
    }

    private void offerMixingRecipes() {
        offerMixing(PSFluids.AGAVE, PSItems.AGAVE_LEAF);
        offerMixing(PSFluids.CANNABIS_TEA, PSItems.CANNABIS_LEAF);
        offerMixing(PSFluids.COCA_TEA, PSItems.COCA_LEAVES);
        offerMixing(PSFluids.PEYOTE_JUICE, PSItems.PEYOTE);
        offerMixing(PSFluids.COFFEE, PSItems.COFFEE_BEANS, PSTags.Items.SUITABLE_HOT_DRINK_RECEPTICALS);

        MixingRecipeJsonBuilder.create(items, RecipeCategory.FOOD, PSFluids.BATH_SALTS, 500)
            .input(Items.LAVA_BUCKET)
            .input(PSItems.OBSIDIAN_DUST).criterion(hasItem(PSItems.OBSIDIAN_DUST), conditionsFromItem(PSItems.OBSIDIAN_DUST))
            .receptical(PSTags.Items.DRUG_RECEPTICALS)
            .offerTo(exporter);
        MixingRecipeJsonBuilder.create(items, RecipeCategory.FOOD, PSFluids.CAFFEINE, 500)
            .input(PSItems.COFFEE_BEANS, 2).criterion(hasItem(PSItems.COFFEE_BEANS), conditionsFromItem(PSItems.COFFEE_BEANS))
            .receptical(PSTags.Items.DRUG_RECEPTICALS)
            .offerTo(exporter);
        offerMixing(PSFluids.COCAINE, PSItems.COCAINE_POWDER, PSTags.Items.DRUG_RECEPTICALS);

    }

    private void offerReacting(SimpleFluid fluid, ItemConvertible input) {
        ReactingRecipeJsonBuilder.create(items, RecipeCategory.BREWING, fluid.getDefaultStack(50))
            .input(input).criterion(hasItem(input), conditionsFromItem(input))
            .byProduct(Items.COAL)
            .offerTo(exporter);
    }

    private void offerReacting(SimpleFluid fluid, TagKey<Item> input) {
        ReactingRecipeJsonBuilder.create(items, RecipeCategory.BREWING, fluid.getDefaultStack(50))
            .input(input).criterion("has_" + input.id().getPath(), conditionsFromTag(input))
            .byProduct(Items.COAL)
            .offerTo(exporter);
    }

    private void offerMixing(SimpleFluid output, ItemConvertible input) {
        offerMixing(output, input, PSTags.Items.DRINK_RECEPTICALS);
    }

    private void offerMixing(SimpleFluid output, ItemConvertible input, TagKey<Item> receptical) {
        MixingRecipeJsonBuilder.create(items, RecipeCategory.FOOD, output, 500)
            .input(input, 2).criterion(hasItem(input), conditionsFromItem(input))
            .receptical(receptical)
            .offerTo(exporter);
    }

    private void offerMashing(SimpleFluid output, ItemConvertible input) {
        MashingRecipeJsonBuilder.create(items, RecipeCategory.FOOD, output.getDefaultStack())
            .input(input, 8).criterion(hasItem(input), conditionsFromItem(input))
            .offerTo(exporter);
    }

    private void offerMashing(SimpleFluid output, TagKey<Item> input) {
        MashingRecipeJsonBuilder.create(items, RecipeCategory.FOOD, output.getDefaultStack())
            .input(input, 8).criterion("has_" + input.id().getPath(), conditionsFromTag(input))
            .offerTo(exporter);
    }

    private void offerDrying(
            ItemConvertible input,
            ItemConvertible output, int count, float experience, float cookingTime, String group) {
        DryingRecipeJsonBuilder.create(Ingredient.ofItems(input), RecipeCategory.FOOD, output, count, experience, cookingTime)
            .criterion(hasItem(input), conditionsFromItem(input))
            .group(group)
            .offerTo(exporter);
    }

    private void offerSmokeable(ItemConvertible result, ItemConvertible filling) {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, result)
            .input('#', filling).criterion(hasItem(filling), conditionsFromItem(filling))
            .input('-', Items.PAPER)
            .pattern("-")
            .pattern("#")
            .pattern("-")
            .offerTo(exporter);
    }

    private void offerBarrel(ItemConvertible result, ItemConvertible planks) {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.REDSTONE, result)
            .input('#', planks).criterion(hasItem(planks), conditionsFromItem(planks))
            .input('I', Items.IRON_INGOT)
            .input('S', ConventionalItemTags.WOODEN_RODS).criterion("has_stick", conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .pattern(" I ")
            .pattern("# #")
            .pattern("S#S")
            .offerTo(exporter);
    }

    private static RegistryKey<Recipe<?>> id(String name) {
        return RegistryKey.of(RegistryKeys.RECIPE, Psychedelicraft.id(name));
    }

    private Optional<Item> lookupItem(Identifier id) {
        return items
            .getOptional(RegistryKey.of(RegistryKeys.ITEM, id))
            .or(() -> items.getOptional(RegistryKey.of(RegistryKeys.ITEM, Identifier.ofVanilla(id.getPath()))))
            .map(RegistryEntry::value);
    }
}