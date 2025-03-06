/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.List;
import java.util.function.Function;

import org.joml.Vector3f;

import com.terraformersmc.terraform.boat.api.item.TerraformBoatItemHelper;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.*;
import ivorius.psychedelicraft.entity.PSEntities;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.ConsumableFluid;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.container.FluidCauldronBehavior;
import ivorius.psychedelicraft.item.component.BagContentsComponent;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.item.component.RiftFractionComponent;
import ivorius.psychedelicraft.util.MathUtils;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Direction;

/**
 * Created by lukas on 25.04.14.
 * Updated by Sollace on 1 Jan 2023
 */
public interface PSItems {
    DrinkableItem WOODEN_MUG = register("wooden_mug", s -> new DrinkableItem(s
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.MUG)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem STONE_CUP = register("stone_cup", s -> new DrinkableItem(s
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.CUP)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem GLASS_CHALICE = register("glass_chalice", s -> new DrinkableItem(s
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.CHALLICE)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem SHOT_GLASS = register("shot_glass", s -> new DrinkableItem(s
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.SHOT)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME / 4, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem BOTTLE = register("bottle", s -> new DrinkableItem(s
            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(Colors.WHITE, true))
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BOTTLE)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.DRINK));
    MolotovCocktailItem MOLOTOV_COCKTAIL = register("molotov_cocktail", s -> new MolotovCocktailItem(s
            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(Colors.WHITE, true))
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BOTTLE))
            .maxCount(16)));

    FlaskItem OAK_BARREL = register("oak_barrel", s -> new FlaskItem(PSBlocks.OAK_BARREL, s.maxCount(16)
            .maxCount(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.MATURE)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BARREL))));
    FlaskItem SPRUCE_BARREL = register("spruce_barrel", s -> new FlaskItem(PSBlocks.SPRUCE_BARREL, s
            .maxCount(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.MATURE)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BARREL))));
    FlaskItem BIRCH_BARREL = register("birch_barrel", s -> new FlaskItem(PSBlocks.BIRCH_BARREL, s
            .maxCount(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.MATURE)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BARREL))));
    FlaskItem JUNGLE_BARREL = register("jungle_barrel", s -> new FlaskItem(PSBlocks.JUNGLE_BARREL, s
            .maxCount(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.MATURE)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BARREL))));
    FlaskItem ACACIA_BARREL = register("acacia_barrel", s -> new FlaskItem(PSBlocks.ACACIA_BARREL, s
            .maxCount(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.MATURE)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BARREL))));
    FlaskItem DARK_OAK_BARREL = register("dark_oak_barrel", s -> new FlaskItem(PSBlocks.DARK_OAK_BARREL, s
            .maxCount(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.MATURE)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BARREL))));

    FlaskItem MASH_TUB = register("mash_tub", s -> new MashTubItem(PSBlocks.MASH_TUB, s
            .maxCount(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.FERMENT)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.VAT))));
    FlaskItem FLASK = register("flask", s -> new FlaskItem(PSBlocks.FLASK, s
            .maxCount(16)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.FLASK))));
    FlaskItem DISTILLERY = register("distillery", s -> new FlaskItem(PSBlocks.DISTILLERY, s
            .maxCount(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.DISTILL)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.FLASK))));
    RiftJarItem RIFT_JAR = register("rift_jar", s -> new RiftJarItem(PSBlocks.RIFT_JAR, s
            .component(PSComponents.RIFT_FRACTION, RiftFractionComponent.DEFAULT)));

    DrinkableItem FILLED_GLASS_BOTTLE = register("filled_glass_bottle", s -> new ProxyDrinkableItem(Items.GLASS_BOTTLE, s.component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.GLASS_BOTTLE)), FluidVolumes.GLASS_BOTTLE, ConsumableFluid.ConsumptionType.DRINK));
    FilledBucketItem FILLED_BUCKET = register("filled_bucket", s -> new FilledBucketItem(s.maxCount(1).component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BUCKET))));
    DrinkableItem FILLED_BOWL = register("filled_bowl", s -> new ProxyDrinkableItem(Items.BOWL, s.component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BOWL)), FluidVolumes.BOWL, ConsumableFluid.ConsumptionType.DRINK));

    Item WINE_GRAPES = register("wine_grapes", s -> new WineGrapesItem(s.food(
            new FoodComponent.Builder().nutrition(1).saturationModifier(0.5F).build(), PSConsumableComponents.FAST_FOOD
    ), 15));

    Item CANNABIS_SEEDS = register("cannabis_seeds", s -> new BlockItem(PSBlocks.CANNABIS, s.translationKey(PSBlocks.CANNABIS.getTranslationKey())));
    Item CANNABIS_LEAF = register("cannabis_leaf");
    Item CANNABIS_BUDS = register("cannabis_buds");
    Item DRIED_CANNABIS_LEAF = register("dried_cannabis_leaf");
    Item DRIED_CANNABIS_BUDS = register("dried_cannabis_buds");

    Item HASH_MUFFIN = register("hash_muffin", s -> new EdibleItem(
            s.food(EdibleItem.HASH_MUFFIN),
            new DrugInfluence(DrugType.CANNABIS, DrugInfluence.DelayType.METABOLISED, 0.004, 0.002, 0.7f)
    ));

    Item HOP_CONES = register("hop_cones");
    Item HOP_SEEDS = register("hop_seeds", s -> new BlockItem(PSBlocks.HOP, s.translationKey(PSBlocks.HOP.getTranslationKey())));

    Item BROWN_MAGIC_MUSHROOMS = register("brown_magic_mushrooms", s -> new EdibleItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.BROWN_SHROOMS, DrugInfluence.DelayType.INGESTED, 0.005, 0.003, 0.25f)
    ));
    Item RED_MAGIC_MUSHROOMS = register("red_magic_mushrooms", s -> new EdibleItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.RED_SHROOMS, DrugInfluence.DelayType.INGESTED, 0.005, 0.003, 0.25f)
    ));

    Item JOLLY_RANCHER = register("jolly_rancher", s -> new EdibleItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.SUGAR, DrugInfluence.DelayType.INGESTED, 0.005, 0.003, 0.05f)
    ));

    Item TOBACCO_LEAVES = register("tobacco");
    Item TOBACCO_SEEDS = register("tobacco_seeds", s -> new BlockItem(PSBlocks.TOBACCO, s.translationKey(PSBlocks.TOBACCO.getTranslationKey())));
    Item DRIED_TOBACCO = register("dried_tobacco");

    SmokeableItem CIGARETTE = register("cigarette", s -> new SmokeableItem(
            s.maxCount(1).maxDamage(1), 2, SmokeableItem.WHITE,
            new DrugInfluence(DrugType.TOBACCO, DrugInfluence.DelayType.IMMEDIATE, 0.1, 0.02, 0.7F)
    ));
    SmokeableItem CIGAR = register("cigar", s -> new SmokeableItem(
            s.maxCount(1).maxDamage(3), 4, new Vector3f(0.6F, 0.6F, 0.5F),
            new DrugInfluence(DrugType.TOBACCO, DrugInfluence.DelayType.IMMEDIATE, 0.1, 0.02, 0.7F)
    ));
    SmokeableItem JOINT = register("joint", s -> new SmokeableItem(
            s.maxCount(1).maxDamage(2), 2, new Vector3f(0.9F, 0.9F, 0.9F),
            new DrugInfluence(DrugType.CANNABIS, DrugInfluence.DelayType.INHALED, 0.002, 0.001, 0.20F)
    ));

    Item COCA_SEEDS = register("coca_seeds", s -> new BlockItem(PSBlocks.COCA, s.translationKey(PSBlocks.COCA.getTranslationKey())));
    Item COCA_LEAVES = register("coca_leaves");
    Item DRIED_COCA_LEAVES = register("dried_coca_leaves");
    Item COCAINE_POWDER = register("cocaine_powder", s -> new CocainePowderItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.COCAINE, DrugInfluence.DelayType.IMMEDIATE, 0.002, 0.003, 0.35f)
    ));
    Item CRACK_COCAINE = register("crack_cocaine", s -> new CocainePowderItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.COCAINE, DrugInfluence.DelayType.IMMEDIATE, 0.002, 0.03, 0.65f)
    ));

    DrinkableItem SYRINGE = register("syringe", s -> new SyringeItem(s
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(DrinkableItem.FLUID_PER_INJECTION)
    )));

    Item JUNIPER_LEAVES = register("juniper_leaves", PSBlocks.JUNIPER_LEAVES);
    Item FRUITING_JUNIPER_LEAVES = register("fruiting_juniper_leaves", PSBlocks.FRUITING_JUNIPER_LEAVES);
    Item JUNIPER_LOG = register("juniper_log", PSBlocks.JUNIPER_LOG);
    Item JUNIPER_WOOD = register("juniper_wood", PSBlocks.JUNIPER_WOOD);
    Item STRIPPED_JUNIPER_LOG = register("stripped_juniper_log", PSBlocks.STRIPPED_JUNIPER_LOG);
    Item STRIPPED_JUNIPER_WOOD = register("stripped_juniper_wood", PSBlocks.STRIPPED_JUNIPER_WOOD);
    Item JUNIPER_BERRIES = register("juniper_berries", s -> new SpecialFoodItem(
            s.food(new FoodComponent.Builder().nutrition(1).saturationModifier(0.5F).build(), PSConsumableComponents.FAST_FOOD), 15
    ));
    Item JUNIPER_SAPLING = register("juniper_sapling", PSBlocks.JUNIPER_SAPLING);
    Item JUNIPER_PLANKS = register("juniper_planks", PSBlocks.JUNIPER_PLANKS);
    Item JUNIPER_STAIRS = register("juniper_stairs", PSBlocks.JUNIPER_STAIRS);
    Item JUNIPER_SIGN = register("juniper_sign", s -> new SignItem(PSBlocks.JUNIPER_WALL_SIGN, PSBlocks.JUNIPER_SIGN, s.maxCount(16)));
    Item JUNIPER_DOOR = register("juniper_door", PSBlocks.JUNIPER_DOOR);
    Item JUNIPER_HANGING_SIGN = register("juniper_hanging_sign", s -> new HangingSignItem(PSBlocks.JUNIPER_HANGING_SIGN, PSBlocks.JUNIPER_WALL_HANGING_SIGN, s.maxCount(16)));
    Item JUNIPER_PRESSURE_PLATE = register("juniper_pressure_plate", PSBlocks.JUNIPER_PRESSURE_PLATE);
    Item JUNIPER_FENCE = register("juniper_fence", PSBlocks.JUNIPER_FENCE);
    Item JUNIPER_TRAPDOOR = register("juniper_trapdoor", PSBlocks.JUNIPER_TRAPDOOR);
    Item JUNIPER_FENCE_GATE = register("juniper_fence_gate", PSBlocks.JUNIPER_FENCE_GATE);
    Item JUNIPER_BUTTON = register("juniper_button", PSBlocks.JUNIPER_BUTTON);
    Item JUNIPER_SLAB = register("juniper_slab", PSBlocks.JUNIPER_SLAB);
    Item JUNIPER_BOAT = TerraformBoatItemHelper.registerBoatItem(Psychedelicraft.id("juniper_boat"), false, false);
    Item JUNIPER_CHEST_BOAT = register("juniper_chest_boat", s -> new BoatItem(PSEntities.JUNIPER_CHEST_BOAT, s.maxCount(1)));

    Item COFFEA_CHERRIES = register("coffea_cherries", s -> new BlockItem(PSBlocks.COFFEA, s.translationKey(PSBlocks.COFFEA.getTranslationKey())));
    Item COFFEE_BEANS = register("coffee_beans");

    Item PEYOTE = register("peyote", PSBlocks.PEYOTE);
    Item DRIED_PEYOTE = register("dried_peyote", s -> new EdibleItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.PEYOTE, DrugInfluence.DelayType.INGESTED, 0.005, 0.003, 0.5f)
    ));
    Item PEYOTE_JOINT = register("peyote_joint", s -> new SmokeableItem(
            s.maxCount(1).maxDamage(2), 2, new Vector3f(0.5F, 0.9F, 0.4F),
            new DrugInfluence(DrugType.PEYOTE, DrugInfluence.DelayType.INHALED, 0.003, 0.0015, 0.4f),
            new DrugInfluence(DrugType.TOBACCO, DrugInfluence.DelayType.IMMEDIATE, 0.1, 0.02, 0.1f)
    ));

    Item LATTICE = register("lattice", PSBlocks.LATTICE);
    Item WINE_GRAPE_LATTICE = register("wine_grape_lattice", PSBlocks.WINE_GRAPE_LATTICE);
    Item MORNING_GLORY_LATTICE = register("morning_glory_lattice", PSBlocks.MORNING_GLORY_LATTICE);
    Item BOTTLE_RACK = register("bottle_rack", s -> new VerticallyAttachableBlockItem(PSBlocks.BOTTLE_RACK, PSBlocks.WALL_BOTTLE_RACK, Direction.DOWN, s));
    Item DRYING_TABLE = register("drying_table", PSBlocks.DRYING_TABLE);
    Item IRON_DRYING_TABLE = register("iron_drying_table", PSBlocks.IRON_DRYING_TABLE);

    Item HARMONIUM = register("harmonium", s -> new Item(s.component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(Colors.RED, true))));

    Item OBSIDIAN_BOTTLE = register("obsidian_bottle", s -> new Item(s.maxCount(16)));
    Item OBSIDIAN_DUST = register("obsidian_dust", s -> new CocainePowderItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE, PSConsumableComponents.DUST),
            new DrugInfluence(DrugType.BATH_SALTS, DrugInfluence.DelayType.IMMEDIATE, 0.002, 0.003, 0.35f)
    ));

    // TODO: https://www.erowid.org/plants/kava/kava.shtml
    //Item KAVA_SEEDS = register("kava_seeds", new Item(new Settings()));
    //Item KAVA_ROOT = register("kava_root", new Item(new Settings()));

    Item MORNING_GLORY = register("morning_glory");
    Item MORNING_GLORY_SEEDS = register("morning_glory_seeds", s -> new BlockItem(PSBlocks.MORNING_GLORY, s.translationKey(PSBlocks.MORNING_GLORY.getTranslationKey())));
    Item LSA_SQUARE = register("lsd_square", s -> new EdibleItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.LSD, DrugInfluence.DelayType.CONTACT, 0.05, 0.003, 0.1F)
    ));
    Item LSD_PILL = register("lsd_pill", s -> new EdibleItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.LSD, DrugInfluence.DelayType.INGESTED, 0.05, 0.003, 0.6F)
    ));

    Item JIMSONWEED_SEEDS = register("jimsonweed_seeds", s -> new BlockItem(PSBlocks.JIMSONWEEED, s.translationKey(PSBlocks.JIMSONWEEED.getTranslationKey())));
    Item JIMSONWEED_SEED_POD = register("jimsonweed_seed_pod");
    Item JIMSONWEED_LEAF = register("jimsonweed_leaf");
    Item DRIED_JIMSONWEED_LEAF = register("dried_jimsonweed_leaf");

    Item TOMATO_SEEDS = register("tomato_seeds", s -> new BlockItem(PSBlocks.TOMATOES, s.translationKey(PSBlocks.TOMATOES.getTranslationKey())));
    Item TOMATO = register("tomato", s -> new Item(s.food(EdibleItem.TOMATO)));
    Item TOMATO_LEAF = register("tomato_leaf");

    Item BELLADONNA_SEEDS = register("belladonna_seeds", s -> new BlockItem(PSBlocks.BELLADONNA, s.translationKey(PSBlocks.BELLADONNA.getTranslationKey())));
    Item BELLADONNA_LEAF = register("belladonna_leaf");
    Item DRIED_BELLADONNA_LEAF = register("dried_belladonna_leaf");
    Item BELLADONNA_BERRIES = register("belladonna_berries", s -> new EdibleItem(
            new Settings().food(new FoodComponent.Builder().nutrition(1).saturationModifier(1.5F).build(), PSConsumableComponents.FAST_FOOD),
            new DrugInfluence(DrugType.ATROPINE, DrugInfluence.DelayType.INGESTED, 0.005, 0.003, 0.5f)
    ));

    Item AGAVE_LEAF = register("agave_leaf", s -> new BlockItem(PSBlocks.AGAVE_PLANT, s.translationKey(PSBlocks.AGAVE_PLANT.getTranslationKey())));

    BongItem SMOKING_PIPE = register("smoking_pipe", s -> new BongItem(s.maxDamage(50)))
            .consumes(new BongItem.Consumable(DRIED_CANNABIS_BUDS.getDefaultStack(), new DrugInfluence(DrugType.CANNABIS, DrugInfluence.DelayType.INHALED, 0.002, 0.001, 0.25F)))
            .consumes(new BongItem.Consumable(DRIED_TOBACCO.getDefaultStack(), new DrugInfluence(DrugType.TOBACCO, DrugInfluence.DelayType.INHALED, 0.1, 0.02, 0.8F)))
            .consumes(new BongItem.Consumable(DRIED_BELLADONNA_LEAF.getDefaultStack(), new DrugInfluence(DrugType.ATROPINE, DrugInfluence.DelayType.INHALED, 0.4, 0.1, 0.9F)))
            .consumes(new BongItem.Consumable(DRIED_JIMSONWEED_LEAF.getDefaultStack(), new DrugInfluence(DrugType.ATROPINE, DrugInfluence.DelayType.INHALED, 0.5, 0.1, 0.2F)))
            .consumes(new BongItem.Consumable(HARMONIUM.getDefaultStack(), stack -> new DrugInfluence(DrugType.HARMONIUM, DrugInfluence.DelayType.INHALED, 0.04, 0.01, 0.65F, MathUtils.unpackRgb(DyedColorComponent.getColor(stack, Colors.WHITE)))));
    // TODO: Play around with the bongs benefits
    BongItem BONG = register("bong", s -> new BongItem(s.maxDamage(128)))
            .consumes(new BongItem.Consumable(DRIED_CANNABIS_BUDS.getDefaultStack(), new DrugInfluence(DrugType.CANNABIS, DrugInfluence.DelayType.IMMEDIATE, 0.002, 0.001, 0.2F)))
            .consumes(new BongItem.Consumable(DRIED_TOBACCO.getDefaultStack(), new DrugInfluence(DrugType.TOBACCO, DrugInfluence.DelayType.IMMEDIATE, 0.1, 0.02, 0.6F)))
            .consumes(new BongItem.Consumable(DRIED_BELLADONNA_LEAF.getDefaultStack(), new DrugInfluence(DrugType.ATROPINE, DrugInfluence.DelayType.IMMEDIATE, 0.4, 0.1, 0.4F)))
            .consumes(new BongItem.Consumable(DRIED_JIMSONWEED_LEAF.getDefaultStack(), new DrugInfluence(DrugType.ATROPINE, DrugInfluence.DelayType.IMMEDIATE, 0.5, 0.1, 0.1F)))
            .consumes(new BongItem.Consumable(HARMONIUM.getDefaultStack(), stack -> new DrugInfluence(DrugType.HARMONIUM, DrugInfluence.DelayType.IMMEDIATE, 0.04, 0.01, 0.9F, MathUtils.unpackRgb(DyedColorComponent.getColor(stack, Colors.WHITE)))));

    Item VOMIT = register("vomit");
    Item PAPER_BAG = register("paper_bag", s -> new PaperBagItem(s.component(PSComponents.BAG_CONTENTS, BagContentsComponent.EMPTY)));
    Item BAG_O_VOMIT = register("bag_o_vomit", s -> new SuspiciousItem(s
            .food(new FoodComponent.Builder().nutrition(8).saturationModifier(0.8f).alwaysEdible().build(), PSConsumableComponents.FAST_FOOD),
                SuspiciousItem.createForms(Items.COOKIE, Items.MUSHROOM_STEW, Items.GOLDEN_APPLE, Items.COOKED_BEEF, Items.COOKED_CHICKEN))
            );

    Item TRAY = register("tray", PSBlocks.TRAY);
    Item BUNSEN_BURNER = register("bunsen_burner", PSBlocks.BUNSEN_BURNER);
    Item GLASS_TUBE = register("glass_tube", PSBlocks.GLASS_TUBE);

    Item CRYSTAL_METH = register("methamphetamine_powder");
    Item EXTACY = register("extacy", s -> new EdibleItem(
            new Settings().food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.METHAMPHETAMINE, DrugInfluence.DelayType.INGESTED, 0.005, 0.003, 0.5f)
    ));
    Item PACIFIER = register("pacifier", s -> new PacifierItem(s.maxDamage(50).equippable(EquipmentSlot.OFFHAND)));

    Item HEROINE_POWDER = register("heroine_powder");
    Item MORPHINE_TABLET = register("morphine_tablet", s -> new EdibleItem(
            s.food(EdibleItem.NON_FILLING_EDIBLE),
            new DrugInfluence(DrugType.MORPHINE, DrugInfluence.DelayType.INGESTED, 0.005, 0.003, 0.5f)
    ));
    Item BROKEN_GLASS = register("broken_glass");

    static Item register(String name, Block block) {
        return register(name, s -> new BlockItem(block, s));
    }

    static Item register(String name) {
        return register(name, Item::new);
    }

    static <T extends Item> T register(String name, Function<Item.Settings, T> factory) {
        return register(name, factory, new Settings());
    }

    static <T extends Item> T register(String name, Function<Item.Settings, T> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Psychedelicraft.id(name));
        return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
    }

    static void bootstrap() {
        PSComponents.bootstrap();
        FuelRegistryEvents.BUILD.register((builder, ctx) -> {
            int baseSmeltTime = ctx.baseSmeltTime();
            builder.add(LATTICE, baseSmeltTime * 8 / 2);
            builder.add(SMOKING_PIPE, baseSmeltTime);
            builder.add(JOINT, baseSmeltTime / 10);
            builder.add(PEYOTE_JOINT, baseSmeltTime / 10);
            builder.add(CIGAR, baseSmeltTime * 4 / 10);
            builder.add(CIGARETTE, baseSmeltTime / 4);
            builder.add(WOODEN_MUG, baseSmeltTime / 4);
        });

        List.of(
            WOODEN_MUG, STONE_CUP, GLASS_CHALICE, SHOT_GLASS, BOTTLE, FILLED_BUCKET, FILLED_BOWL, FILLED_GLASS_BOTTLE
        ).forEach(FluidCauldronBehavior::register);

        /*UseBlockCallback.EVENT.register((player, world, hand, hit) -> {

            if (player.isSpectator()) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(Items.GLASS_BOTTLE) || stack.isOf(Items.))

            return ActionResult.PASS;
        });*/
    }
}
