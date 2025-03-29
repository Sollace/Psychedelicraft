package ivorius.psychedelicraft.datagen.providers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.item.ContainedFluidProperty;
import ivorius.psychedelicraft.client.item.FilledProperty;
import ivorius.psychedelicraft.client.item.FluidTintSource;
import ivorius.psychedelicraft.client.item.FlyingProperty;
import ivorius.psychedelicraft.client.item.PlacementProperty;
import ivorius.psychedelicraft.client.item.UsingProperty;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.block.Block;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.property.numeric.DamageProperty;
import net.minecraft.client.render.item.tint.DyeTintSource;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import static net.minecraft.client.data.ItemModels.*;

public interface ItemModels {
    TintSource UNTINTED = constantTintSource(Colors.WHITE);
    Model GENERATED = Models.GENERATED;
    Model HANDHELD = Models.HANDHELD;
    Model SMOKEABLE_TEMPLATE = item("smokeable_template", TextureKey.LAYER0);
    Model SMOKEABLE_USING_TEMPLATE = item("smokeable_using_template", TextureKey.LAYER0);

    TextureKey LATTICE = BlockModels.LATTICE;
    Model CROP_LATTICE_TEMPLATE = item("crop_lattice_template", LATTICE, TextureKey.CROP);
    Model LATTICE_TEMPLATE = item("lattice_template", LATTICE);

    static Identifier getGroundModelId(String type, Item item) {
        return getGroundModelId(type, Registries.ITEM.getId(item));
    }

    static Identifier getGroundModelFluidId(String type, Item item) {
        return getGroundModelFluidId(type, Registries.ITEM.getId(item));
    }

    static Identifier getGroundModelId(String type, Identifier item) {
        return item.withPath(p -> "item/" + p + "_on_" + type);
    }

    static Identifier getGroundModelFluidId(String type, Identifier item) {
        return getGroundModelId(type, item).withSuffixedPath("_fluid");
    }

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

        var basic = basic(itemModelGenerator.upload(item, SMOKEABLE_TEMPLATE));
        var filled = basic(SMOKEABLE_TEMPLATE.upload(ModelIds.getItemSubModelId(item, "_filled"), filledTextures, itemModelGenerator.modelCollector));
        var usingFilled = basic(SMOKEABLE_USING_TEMPLATE.upload(ModelIds.getItemSubModelId(item, "_filled_using"), filledTextures, itemModelGenerator.modelCollector));

        itemModelGenerator.output.accept(item, condition(new UsingProperty(), usingFilled, select(new FilledProperty(), basic, List.of(
                switchCase(FilledProperty.FillPercentage.FULL, filled)
            )
        )));
    }

    static void registerMolotov(ItemModelGenerator itemModelGenerator, Item item) {
        var base = TextureMap.getId(item);
        var overlay = TextureMap.getSubId(item, "_overlay");
        var dyeTint = new DyeTintSource(Colors.WHITE);

        var basic = tinted(itemModelGenerator.uploadTwoLayers(item, base, overlay), dyeTint);
        var filled = tinted(Models.GENERATED_THREE_LAYERS.upload(
                ModelIds.getItemSubModelId(item, "_filled"),
                TextureMap.layered(base, TextureMap.getSubId(item, "_liquid"), overlay),
                itemModelGenerator.modelCollector
        ), dyeTint, new FluidTintSource(Colors.WHITE), UNTINTED);
        var filledLava = tinted(
                Models.GENERATED_THREE_LAYERS.upload(ModelIds.getItemSubModelId(item, "_filled_lava"),
                TextureMap.layered(base, TextureMap.getSubId(item, "_liquid_lava"), overlay),
                itemModelGenerator.modelCollector
        ), dyeTint);
        var flying = basic(Models.GENERATED.upload(
                ModelIds.getItemSubModelId(item, "_thrown"),
                TextureMap.layer0(TextureMap.getSubId(item, "_thrown")),
                itemModelGenerator.modelCollector)
        );

        itemModelGenerator.output.accept(item, applyPlacements(item, condition(new FlyingProperty(),
                flying,
                condition(new ContainedFluidProperty(SimpleFluid.of(Fluids.LAVA)), filledLava,
                        select(new FilledProperty(), basic, List.of(
                            switchCase(FilledProperty.FillPercentage.FULL, filled)
                        ))
                )
        ), new String[] {"ground", "ground_fluid"}, dyeTint));
    }

    static void registerSmokeable(ItemModelGenerator itemModelGenerator, Item item) {
        itemModelGenerator.output.accept(item, condition(new UsingProperty(),
                basic(itemModelGenerator.upload(item, SMOKEABLE_TEMPLATE)),
                basic(itemModelGenerator.registerSubModel(item, "_using", SMOKEABLE_USING_TEMPLATE))
        ));
    }

    static void registerSniffable(ItemModelGenerator itemModelGenerator, Item item) {
        itemModelGenerator.output.accept(item, condition(new UsingProperty(),
                basic(itemModelGenerator.upload(item, SMOKEABLE_TEMPLATE)),
                basic(SMOKEABLE_USING_TEMPLATE.upload(ModelIds.getItemSubModelId(item, "_using"), TextureMap.layer0(item), itemModelGenerator.modelCollector))
        ));
    }

    static void registerCigar(ItemModelGenerator itemModelGenerator, Item item) {
        itemModelGenerator.output.accept(item, condition(new UsingProperty(),
                rangeDispatch(new DamageProperty(true), 3, List.of(
                        rangeDispatchEntry(basic(itemModelGenerator.registerSubModel(item, "_using", SMOKEABLE_USING_TEMPLATE)), 0),
                        rangeDispatchEntry(basic(itemModelGenerator.registerSubModel(item, "_using_1", SMOKEABLE_USING_TEMPLATE)), 1),
                        rangeDispatchEntry(basic(itemModelGenerator.registerSubModel(item, "_using_2", SMOKEABLE_USING_TEMPLATE)), 2),
                        rangeDispatchEntry(basic(itemModelGenerator.registerSubModel(item, "_using_3", SMOKEABLE_USING_TEMPLATE)), 3)
                )),
                rangeDispatch(new DamageProperty(true), 3, List.of(
                        rangeDispatchEntry(basic(itemModelGenerator.upload(item, SMOKEABLE_USING_TEMPLATE)), 0),
                        rangeDispatchEntry(basic(itemModelGenerator.registerSubModel(item, "_1", SMOKEABLE_TEMPLATE)), 1),
                        rangeDispatchEntry(basic(itemModelGenerator.registerSubModel(item, "_2", SMOKEABLE_TEMPLATE)), 2),
                        rangeDispatchEntry(basic(itemModelGenerator.registerSubModel(item, "_3", SMOKEABLE_TEMPLATE)), 3)
                ))
        ));
    }

    static void registerPlantLattice(ItemModelGenerator itemModelGenerator, Block lattice, Item item) {
        Block crop = Block.getBlockFromItem(item);
        itemModelGenerator.output.accept(item, rangeDispatch(new DamageProperty(false), 3, List.of(
                rangeDispatchEntry(basic(uploadLatticeModel(itemModelGenerator, lattice, crop, item, 0)), 0),
                rangeDispatchEntry(basic(uploadLatticeModel(itemModelGenerator, lattice, crop, item, 1)), 1),
                rangeDispatchEntry(basic(uploadLatticeModel(itemModelGenerator, lattice, crop, item, 2)), 2),
                rangeDispatchEntry(basic(uploadLatticeModel(itemModelGenerator, lattice, crop, item, 3)), 3)
        )));
    }

    static Identifier uploadLatticeModel(ItemModelGenerator itemModelGenerator, Block lattice, Block crop, Item item, int index) {
        return CROP_LATTICE_TEMPLATE.upload(ModelIds.getItemSubModelId(item, "_stage" + index), new TextureMap()
                .put(LATTICE, TextureMap.getId(lattice))
                .put(TextureKey.CROP, TextureMap.getSubId(crop, "_stage" + index)), itemModelGenerator.modelCollector);
    }

    static void registerLayered(ItemModelGenerator itemModelGenerator, Item item, String overlay1, TintSource...tint) {
        itemModelGenerator.output.accept(item, tinted(Models.GENERATED_TWO_LAYERS.upload(ModelIds.getItemModelId(item), TextureMap.layered(
                TextureMap.getId(item), TextureMap.getSubId(item, overlay1)
        ), itemModelGenerator.modelCollector), tint));
    }

    static void registerPaperBag(ItemModelGenerator itemModelGenerator, Item item) {
        var empty = basic(itemModelGenerator.upload(item, GENERATED));
        var filled = basic(itemModelGenerator.registerSubModel(item, "_filled", GENERATED));
        var overflowing = basic(itemModelGenerator.registerSubModel(item, "_overflowing", GENERATED));
        var bursting = basic(itemModelGenerator.registerSubModel(item, "_bursting", GENERATED));

        itemModelGenerator.output.accept(item, select(new FilledProperty(), empty,
                List.of(
                        switchCase(FilledProperty.FillPercentage.ONE_QUARTER, filled),
                        switchCase(FilledProperty.FillPercentage.HALF, filled),
                        switchCase(FilledProperty.FillPercentage.THREE_QUARTER, overflowing),
                        switchCase(FilledProperty.FillPercentage.FULL, bursting)
                )
        ));
    }

    static void registerDrinkHolder(ItemModelGenerator itemModelGenerator, Item item, String...placements) {
        var empty = basic(itemModelGenerator.upload(item, GENERATED));
        var lavaFilled = basic(Models.GENERATED_TWO_LAYERS.upload(
                ModelIds.getItemSubModelId(item, "_filled_with_lava"),
                TextureMap.layered(TextureMap.getId(item), TextureMap.getSubId(item, "_liquid_lava")),
                itemModelGenerator.modelCollector
        ));
        var filled = tinted(Models.GENERATED_TWO_LAYERS.upload(
                ModelIds.getItemSubModelId(item, "_filled"),
                TextureMap.layered(TextureMap.getId(item), TextureMap.getSubId(item, "_liquid")),
                itemModelGenerator.modelCollector
        ), UNTINTED, new FluidTintSource(Colors.WHITE));

        itemModelGenerator.output.accept(item, applyPlacements(item, condition(new ContainedFluidProperty(SimpleFluid.of(Fluids.LAVA)),
                lavaFilled,
                select(new FilledProperty(), empty, List.of(
                        switchCase(FilledProperty.FillPercentage.FULL, filled)
                    )
                )
        ), placements));
    }

    static ItemModel.Unbaked applyPlacements(Item item, ItemModel.Unbaked model, String[] placements, TintSource... tints) {
        if (placements.length > 0) {
            TintSource[] fluidTints = tints.length == 0
                    ? new TintSource[] {new FluidTintSource(Colors.WHITE)}
                    : Stream.concat(Arrays.stream(tints), Stream.of(new FluidTintSource(Colors.WHITE))).toArray(TintSource[]::new);
            return select(new PlacementProperty(), model, Arrays.stream(placements).map(placement -> {
                return switchCase(placement, placement.endsWith("_fluid")
                        ? tinted(getGroundModelFluidId(placement, item), fluidTints)
                        : tints.length == 0 ? basic(getGroundModelId(placement, item)) : tinted(getGroundModelId(placement, item), tints)
                );
            }).toList());
        }
        return model;
    }

    static void registerDyeableDrinkHolder(ItemModelGenerator itemModelGenerator, Item item, String...placements) {
        var overlay = TextureMap.getSubId(item, "_overlay");
        var base = TextureMap.getId(item);

        var dyeTint = new DyeTintSource(Colors.WHITE);

        var empty = tinted(itemModelGenerator.uploadTwoLayers(item, base, overlay), dyeTint);
        var lavaFilled = tinted(Models.GENERATED_THREE_LAYERS.upload(
                ModelIds.getItemSubModelId(item, "_filled_with_lava"),
                TextureMap.layered(base, TextureMap.getSubId(item, "_liquid_lava"), overlay),
                itemModelGenerator.modelCollector
        ), dyeTint);
        var filled = tinted(Models.GENERATED_THREE_LAYERS.upload(
                ModelIds.getItemSubModelId(item, "_filled"),
                TextureMap.layered(base, TextureMap.getSubId(item, "_liquid"), overlay),
                itemModelGenerator.modelCollector
        ), dyeTint, new FluidTintSource(Colors.WHITE));

        itemModelGenerator.output.accept(item, applyPlacements(item, condition(new ContainedFluidProperty(SimpleFluid.of(Fluids.LAVA)),
                lavaFilled,
                select(new FilledProperty(), empty, List.of(
                        switchCase(FilledProperty.FillPercentage.FULL, filled)
                    )
                )
        ), placements, dyeTint));
    }

    static void registerParentedDrinkHolder(ItemModelGenerator itemModelGenerator, Item item, Item parent, Identifier withLava, Identifier withWater, String...placements) {
        var overlayTextureKey = parent == Items.POTION ? TextureKey.LAYER0 : TextureKey.LAYER1;
        var parentModel = new Model(Optional.of(ModelIds.getItemModelId(parent)), Optional.empty(), overlayTextureKey);
        var parentId = Registries.ITEM.getId(parent);

        var empty = tinted(parentModel.upload(item, TextureMap.of(
                overlayTextureKey,
                parentId.withPath(p -> "item/" + p + (parent == Items.POTION ? "_overlay" : "_liquid"))
        ), itemModelGenerator.modelCollector),
                parent == Items.POTION ? new FluidTintSource(Colors.WHITE) : UNTINTED,
                parent == Items.POTION ? UNTINTED : new FluidTintSource(Colors.WHITE)
        );
        var lavaFilled = basic(withLava);
        var waterFilled = basic(withWater);

        itemModelGenerator.output.accept(item, applyPlacements(item, condition(new ContainedFluidProperty(SimpleFluid.of(Fluids.LAVA)),
                lavaFilled,
                condition(new ContainedFluidProperty(SimpleFluid.of(Fluids.WATER)), waterFilled, empty)
        ), placements));
    }

    static void registerParentedDrinkHolder(ItemModelGenerator itemModelGenerator, Item item, Item parent, Identifier withLava) {
        var overlayTextureKey = parent == Items.POTION ? TextureKey.LAYER0 : TextureKey.LAYER1;
        var parentModel = new Model(Optional.of(ModelIds.getItemModelId(parent)), Optional.empty(), overlayTextureKey);
        var parentId = Registries.ITEM.getId(parent);

        var empty = tinted(parentModel.upload(item, TextureMap.of(
                overlayTextureKey,
                parentId.withPath(p -> "item/" + p + (parent == Items.POTION ? "_overlay" : "_liquid"))
        ), itemModelGenerator.modelCollector),
                parent == Items.POTION ? new FluidTintSource(Colors.WHITE) : UNTINTED,
                parent == Items.POTION ? UNTINTED : new FluidTintSource(Colors.WHITE)
        );
        var lavaFilled = basic(withLava);

        itemModelGenerator.output.accept(item, condition(new ContainedFluidProperty(SimpleFluid.of(Fluids.LAVA)), lavaFilled, empty));
    }
}
