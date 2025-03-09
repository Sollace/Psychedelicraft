package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelTypes;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelTypes;
import net.minecraft.client.render.item.property.bool.BooleanProperties;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.render.item.property.select.SelectProperties;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.item.tint.TintSourceTypes;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;

public interface PSItemProperties {

    private static <P extends SelectProperty<T>, T> void option(String name, SelectProperty.Type<P, T> codec) {
        SelectProperties.ID_MAPPER.put(Psychedelicraft.id(name), codec);
    }

    private static void flag(String name, MapCodec<? extends BooleanProperty> codec) {
        BooleanProperties.ID_MAPPER.put(Psychedelicraft.id(name), codec);
    }

    private static void range(String name, MapCodec<? extends NumericProperty> codec) {
        NumericProperties.ID_MAPPER.put(Psychedelicraft.id(name), codec);
    }

    private static void tint(String name, MapCodec<? extends TintSource> codec) {
        TintSourceTypes.ID_MAPPER.put(Psychedelicraft.id(name), codec);
    }

    private static void model(String name, MapCodec<? extends ItemModel.Unbaked> codec) {
        ItemModelTypes.ID_MAPPER.put(Psychedelicraft.id(name), codec);
    }

    private static void specialModel(String name, MapCodec<? extends SpecialModelRenderer.Unbaked> codec) {
        SpecialModelTypes.ID_MAPPER.put(Psychedelicraft.id(name), codec);
    }

    static void bootstrap() {
        flag("tripping", TrippingProperty.CODEC);
        flag("flying", FlyingProperty.CODEC);
        flag("using", UsingProperty.CODEC);
        option("filled", FilledProperty.TYPE);
        flag("filled_with_lava", FilledWithLavaProperty.CODEC);
        range("age", AgeProperty.CODEC);

        tint("fluid", FluidTintSource.CODEC);

        model("hallucination", HallucinatedItemModel.Unbaked.CODEC);
        specialModel("rift_jar", RiftJarItemModelRenderer.Unbaked.CODEC);

        // layer 1,2,3,etc -> minecraft:dye
        // ColorProviderRegistry.ITEM.register((stack, layer) -> layer > 0 ? -1 : DyedColorComponent.getColor(stack, Colors.RED), PSItems.HARMONIUM);

        // layer0 -> minecraft:dye (default is white)
        // layer1 -> psychedelicraft:fluid (default is white)
        /*ColorProviderRegistry.ITEM.register((stack, layer) -> {
            if (layer == 0) {
                return DyedColorComponent.getColor(stack, Colors.WHITE);
            }
            if (layer == 1) {
                ItemFluids fluids = ItemFluids.of(stack);
                if (!fluids.isEmpty()) {
                    return FluidBoxRenderer.FluidAppearance.getItemColor(fluids);
                }
            }
            return Colors.WHITE;
        }, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.GLASS_CHALICE, PSItems.STONE_CUP, PSItems.WOODEN_MUG, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL, PSItems.SYRINGE);*/
        // layer0 -> psychedelicraft:fluid (default is white)
        /*ColorProviderRegistry.ITEM.register((stack, layer) -> {
            if (layer == 0) {
                ItemFluids fluids = ItemFluids.of(stack);
                if (!fluids.isEmpty()) {
                    return FluidBoxRenderer.FluidAppearance.getItemColor(fluids);
                }
            }
            return Colors.WHITE;
        }, PSItems.FILLED_GLASS_BOTTLE);*/
    }

    interface ValueSupplier<T> {
        T getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ModelTransformationMode modelTransformationMode);
    }
}
