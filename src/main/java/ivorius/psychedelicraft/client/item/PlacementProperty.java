package ivorius.psychedelicraft.client.item;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

public record PlacementProperty() implements SelectProperty<String> {
    private static ThreadLocal<String> currentPlacement = new ThreadLocal<>();
    public static final SelectProperty.Type<PlacementProperty, String> TYPE = SelectProperty.Type.create(MapCodec.unit(new PlacementProperty()), Codec.STRING);

    public static void setCurrent(String placement) {
        currentPlacement.set(placement);
    }

    @Override
    public String getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ItemDisplayContext displayContext) {
        String value = currentPlacement.get();
        return value == null ? "" : value;
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public Type<? extends SelectProperty<String>, String> getType() {
        return TYPE;
    }
}
