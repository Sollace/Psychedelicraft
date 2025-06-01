package ivorius.psychedelicraft.datagen.providers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.block.Block;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public interface ItemModels {
    Model GENERATED = Models.GENERATED;
    Model HANDHELD = Models.HANDHELD;
    Model SMOKEABLE_TEMPLATE = item("smokeable_template", TextureKey.LAYER0);
    Model SMOKEABLE_USING_TEMPLATE = item("smokeable_using_template", TextureKey.LAYER0);
    Model SNIFFABLE_USING_TEMPLATE = item("sniffable_using_template", TextureKey.LAYER0);

    TextureKey LATTICE = BlockModels.LATTICE;
    Model CROP_LATTICE_TEMPLATE = item("crop_lattice_template", LATTICE, TextureKey.CROP);
    Model LATTICE_TEMPLATE = item("lattice_template", LATTICE);

    static Model item(String parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Psychedelicraft.id("item/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    static void register(ItemModelGenerator itemModelGenerator, Item... items) {
        register(itemModelGenerator, GENERATED, items);
    }

    static void register(ItemModelGenerator itemModelGenerator, Model model, Item... items) {
        for (Item item : items) {
            itemModelGenerator.register(item, model);
        }
    }

    static void registerBong(ItemModelGenerator itemModelGenerator, Item item) {
        var filledTextures = TextureMap.layer0(TextureMap.getSubId(item, "_filled"));
        ModelOverrides.of(SMOKEABLE_TEMPLATE)
            .addOverride(Map.of("psychedelicraft:using", 0F, "psychedelicraft:filled", 1F),
                    g -> SMOKEABLE_TEMPLATE.upload(ModelIds.getItemSubModelId(item, "_filled"), filledTextures, g.writer))
            .addOverride(Map.of("psychedelicraft:using", 1F, "psychedelicraft:filled", 1F),
                    g -> SMOKEABLE_USING_TEMPLATE.upload(ModelIds.getItemSubModelId(item, "_filled_using"), filledTextures, g.writer))
            .upload(item, itemModelGenerator);
    }

    static void registerMolotov(ItemModelGenerator itemModelGenerator, Item item) {
        var base = TextureMap.getId(item);
        var overlay = TextureMap.getSubId(item, "_overlay");
        ModelOverrides.of(Models.GENERATED_THREE_LAYERS)
            .addOverride(Map.of("psychedelicraft:flying", 0F, "psychedelicraft:filled", 1F),
                    g -> Models.GENERATED_THREE_LAYERS.upload(ModelIds.getItemSubModelId(item, "_filled"), TextureMap.layered(base, TextureMap.getSubId(item, "_liquid"), overlay), g.writer))
            .addOverride(Map.of("psychedelicraft:flying", 0F, "psychedelicraft:filled_with_lava", 1F),
                    g -> Models.GENERATED_THREE_LAYERS.upload(ModelIds.getItemSubModelId(item, "_filled_lava"), TextureMap.layered(base, TextureMap.getSubId(item, "_liquid_lava"), overlay), g.writer))
            .addOverride(Map.of("psychedelicraft:flying", 1F),
                    g -> Models.GENERATED.upload(ModelIds.getItemSubModelId(item, "_thrown"), TextureMap.layer0(TextureMap.getSubId(item, "_thrown")), g.writer))
            .upload(ModelIds.getItemModelId(item), TextureMap.layered(base, overlay, overlay), itemModelGenerator);
    }

    static void registerSmokeable(ItemModelGenerator itemModelGenerator, Item item) {
        ModelOverrides.of(SMOKEABLE_TEMPLATE)
            .addOverride("psychedelicraft:using", 1F,
                    g -> SMOKEABLE_USING_TEMPLATE.upload(ModelIds.getItemSubModelId(item, "_using"), TextureMap.layer0(TextureMap.getSubId(item, "_using")), g.writer))
            .upload(item, itemModelGenerator);
    }

    static void registerSniffable(ItemModelGenerator itemModelGenerator, Item item) {
        ModelOverrides.of(SMOKEABLE_TEMPLATE)
            .addOverride("psychedelicraft:using", 1F, generator -> SNIFFABLE_USING_TEMPLATE.upload(
                    ModelIds.getItemSubModelId(item, "_using"),
                    TextureMap.layer0(TextureMap.getId(item)),
                    itemModelGenerator.writer)
            ).upload(item, itemModelGenerator);
    }

    static void registerCigar(ItemModelGenerator itemModelGenerator, Item item) {
        ModelOverrides builder = ModelOverrides.of(SMOKEABLE_TEMPLATE);
        var damages = List.of(0F, 0.33F, 0.66F, 1F);

        for (int i = 0; i < damages.size(); i++) {
            for (int u = i == 0 ? 1 : 0; u < 2; u++) {
                final boolean using = u == 1;
                String name = (u == 1 ? "_using" : "") + (i == 0 ? "" : "_" + i);
                builder.addOverride(Map.of(
                        "psychedelicraft:using", (float)u,
                        "damage", damages.get(i)
                ), generator -> (using ? SMOKEABLE_USING_TEMPLATE : SMOKEABLE_TEMPLATE).upload(
                            ModelIds.getItemSubModelId(item, name),
                            TextureMap.layer0(TextureMap.getSubId(item, name)),
                            itemModelGenerator.writer));
            }
        }

        builder.upload(item, itemModelGenerator);
    }

    static void registerPlantLattice(ItemModelGenerator itemModelGenerator, Block lattice, Item item) {
        Block crop = Block.getBlockFromItem(item);
        ModelOverrides.of(CROP_LATTICE_TEMPLATE)
            .addUniform("psychedelicraft:age", 0.1F, 0.3F, 0.1F, (index, value) -> {
                return CROP_LATTICE_TEMPLATE.upload(ModelIds.getItemSubModelId(item, "_stage" + index), new TextureMap()
                        .put(LATTICE, TextureMap.getId(lattice))
                        .put(TextureKey.CROP, TextureMap.getSubId(crop, "_stage" + index)), itemModelGenerator.writer);
            })
            .upload(ModelIds.getItemModelId(item), new TextureMap()
                    .put(LATTICE, TextureMap.getId(lattice))
                    .put(TextureKey.CROP, TextureMap.getSubId(crop, "_stage0")), itemModelGenerator);
    }

    static Identifier registerLayered(ItemModelGenerator itemModelGenerator, Item item, String overlay1) {
        return Models.GENERATED_TWO_LAYERS.upload(ModelIds.getItemModelId(item), TextureMap.layered(
                TextureMap.getId(item), TextureMap.getSubId(item, overlay1)
        ), itemModelGenerator.writer);
    }

    static Identifier registerLayered(ItemModelGenerator itemModelGenerator, Item item, String overlay1, String overlay2) {
        return Models.GENERATED_TWO_LAYERS.upload(ModelIds.getItemModelId(item), TextureMap.layered(
                TextureMap.getId(item), TextureMap.getSubId(item, overlay1), TextureMap.getSubId(item, overlay2)
        ), itemModelGenerator.writer);
    }

    static void registerPaperBag(ItemModelGenerator itemModelGenerator, Item item) {
        ModelOverrides.of(GENERATED)
            .addOverride(ModelIds.getItemSubModelId(item, "_filled"), "psychedelicraft:filled", 0.5F)
            .addOverride(ModelIds.getItemSubModelId(item, "_overflowing"), "psychedelicraft:filled", 0.75F)
            .addOverride(ModelIds.getItemSubModelId(item, "_bursting"), "psychedelicraft:filled", 1F)
            .upload(item, itemModelGenerator);
    }

    static void registerDrinkHolder(ItemModelGenerator itemModelGenerator, Item item) {
        ModelOverrides.of(GENERATED)
            .addOverride(ModelIds.getItemSubModelId(item, "_filled"), Models.GENERATED_TWO_LAYERS,
                    TextureMap.layered(TextureMap.getId(item), TextureMap.getSubId(item, "_liquid")),
                    "psychedelicraft:filled", 1F)
            .addOverride(ModelIds.getItemSubModelId(item, "_filled_with_lava"), Models.GENERATED_TWO_LAYERS,
                    TextureMap.layered(TextureMap.getId(item), TextureMap.getSubId(item, "_liquid_lava")),
                    "psychedelicraft:filled_with_lava", 1F)
            .upload(item, itemModelGenerator);
    }

    static void registerDrinkHolderWithLabel(ItemModelGenerator itemModelGenerator, Item item) {
        var overlay = TextureMap.getSubId(item, "_overlay");
        var base = TextureMap.getId(item);
        ModelOverrides.of(Models.GENERATED_THREE_LAYERS)
            .addOverride(ModelIds.getItemSubModelId(item, "_filled"), Models.GENERATED_THREE_LAYERS,
                    TextureMap.layered(base, TextureMap.getSubId(item, "_liquid"), overlay),
                    "psychedelicraft:filled", 1F)
            .addOverride(ModelIds.getItemSubModelId(item, "_filled_with_lava"), Models.GENERATED_THREE_LAYERS,
                    TextureMap.layered(base, TextureMap.getSubId(item, "_liquid_lava"), overlay),
                    "psychedelicraft:filled_with_lava", 1F)
            .upload(ModelIds.getItemModelId(item), TextureMap.layered(base, overlay, overlay), itemModelGenerator);
    }

    static void registerParentedDrinkHolder(ItemModelGenerator itemModelGenerator, Item item, Item parent, Identifier withLava, Identifier withWater) {
        var overlayTextureKey = parent == Items.POTION ? TextureKey.LAYER0 : TextureKey.LAYER1;
        var parentModel = new Model(Optional.of(ModelIds.getItemModelId(parent)), Optional.empty(), overlayTextureKey);
        var parentId = Registries.ITEM.getId(parent);
        ModelOverrides.of(parentModel)
            .addOverride("psychedelicraft:filled_with_lava", 1F, o -> withLava)
            .addOverride("psychedelicraft:filled_with_water", 1F, o -> withWater)
            .upload(ModelIds.getItemModelId(item), TextureMap.of(overlayTextureKey, parentId.withPath(p -> "item/" + p + (parent == Items.POTION ? "_overlay" : "_liquid"))), itemModelGenerator);
    }

    static void registerParentedDrinkHolder(ItemModelGenerator itemModelGenerator, Item item, Item parent, Identifier withLava) {
        var overlayTextureKey = parent == Items.POTION ? TextureKey.LAYER0 : TextureKey.LAYER1;
        var parentModel = new Model(Optional.of(ModelIds.getItemModelId(parent)), Optional.empty(), overlayTextureKey);
        var parentId = Registries.ITEM.getId(parent);
        ModelOverrides.of(parentModel)
            .addOverride("psychedelicraft:filled_with_lava", 1F, o -> withLava)
            .upload(ModelIds.getItemModelId(item), TextureMap.of(overlayTextureKey, parentId.withPath(p -> "item/" + p + (parent == Items.POTION ? "_overlay" : "_liquid"))), itemModelGenerator);
    }
}
