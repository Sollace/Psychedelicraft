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
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

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
        option("placement", PlacementProperty.TYPE);
        option("filled", FilledProperty.TYPE);
        flag("contained_fluid", ContainedFluidProperty.CODEC);
        range("age", AgeProperty.CODEC);

        tint("fluid", FluidTintSource.CODEC);

        model("hallucination", HallucinatedItemModel.Unbaked.CODEC);
        specialModel("rift_jar", RiftJarItemModelRenderer.Unbaked.CODEC);
        specialModel("vat", VatItemModelRenderer.Unbaked.CODEC);
    }

    interface ValueSupplier<T> {
        T getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ItemDisplayContext displayContext);
    }
}
