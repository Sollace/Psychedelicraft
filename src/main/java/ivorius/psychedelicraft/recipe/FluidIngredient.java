package ivorius.psychedelicraft.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;
import java.util.stream.Stream;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.recipe.Ingredient;

public record FluidIngredient (SimpleFluid fluid, int level, NbtCompound attributes) {
    public static final Codec<FluidIngredient> CODEC = Codecs.xor(
            SimpleFluid.CODEC.xmap(fluid -> new FluidIngredient(fluid, -1, new NbtCompound()), FluidIngredient::fluid),
            RecordCodecBuilder.<FluidIngredient>create(instance -> instance.group(
                    SimpleFluid.CODEC.fieldOf("fluid").forGetter(FluidIngredient::fluid),
                    Codec.INT.optionalFieldOf("level", -1).forGetter(FluidIngredient::level),
                    NbtCompound.CODEC.optionalFieldOf("attributes", new NbtCompound()).forGetter(FluidIngredient::attributes)
            ).apply(instance, FluidIngredient::new))
        ).xmap(RecipeUtils::iDontCareWhich, Either::right);

    public FluidIngredient(PacketByteBuf buffer) {
        this(SimpleFluid.byId(buffer.readIdentifier()), buffer.readVarInt(), buffer.readNbt());
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(fluid.getId());
        buffer.writeVarInt(level);
        buffer.writeNbt(attributes);
    }

    public boolean test(Resovoir tank) {
        return test(tank.getContents());
    }

    public boolean test(MutableFluidContainer container) {
        boolean result = true;
        result &= fluid.isEmpty() || container.getFluid() == fluid;
        result &= attributes.isEmpty() || NbtHelper.matches(attributes, container.getAttributes(), true);
        result &= level <= 0 || container.getLevel() >= level;
        return result;
    }

    public Ingredient toVanillaIngredient(Ingredient receptical) {
        Stream<ItemStack> stacks = Stream.of(receptical)
                .flatMap(i -> Arrays.stream(i.getMatchingStacks()))
                .map(stack -> MutableFluidContainer.of(stack)
                        .withFluid(fluid)
                        .withAttributes(attributes)
                        .withLevel(level < 0 ? FluidContainer.of(stack).getMaxCapacity(stack) : level)
                        .asStack());
        receptical = Ingredient.ofStacks(stacks.toArray(ItemStack[]::new));
        if (receptical.getMatchingStacks().length == 0) {
            return Ingredient.fromTag(PSTags.Items.DRINK_RECEPTICALS);
        }
        return receptical;
    }

    public boolean test(ItemStack stack) {
        return test(MutableFluidContainer.of(stack));
    }
}

