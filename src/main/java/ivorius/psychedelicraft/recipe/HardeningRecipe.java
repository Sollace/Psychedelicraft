package ivorius.psychedelicraft.recipe;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.util.PacketCodecUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public record HardeningRecipe(
        String hardeningGroup,
        FluidIngredient coreFluid,
        List<FluidIngredient> solutions,
        ImpuritiesPredicate impurities,
        ItemStack result,
        IntProvider amount,
        int hardeningTime
    ) implements Recipe<HardeningRecipe.Input> {
    public static final MapCodec<HardeningRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("group").forGetter(HardeningRecipe::hardeningGroup),
            FluidIngredient.CODEC.fieldOf("core_fluid").forGetter(HardeningRecipe::coreFluid),
            FluidIngredient.CODEC.listOf().fieldOf("solutions").forGetter(HardeningRecipe::solutions),
            ImpuritiesPredicate.CODEC.optionalFieldOf("impurities", ImpuritiesPredicate.EMPTY).forGetter(HardeningRecipe::impurities),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(HardeningRecipe::result),
            IntProvider.POSITIVE_CODEC.fieldOf("amount").forGetter(HardeningRecipe::amount),
            Codec.INT.optionalFieldOf("hardening_time", 20).forGetter(HardeningRecipe::hardeningTime)
    ).apply(i, HardeningRecipe::new));
    public static final PacketCodec<RegistryByteBuf, HardeningRecipe> PACKET_CODEC = PacketCodecUtils.tuple(
            PacketCodecs.STRING, HardeningRecipe::hardeningGroup,
            FluidIngredient.PACKET_CODEC, HardeningRecipe::coreFluid,
            FluidIngredient.PACKET_CODEC.collect(PacketCodecs.toList()), HardeningRecipe::solutions,
            ImpuritiesPredicate.PACKET_CODEC, HardeningRecipe::impurities,
            ItemStack.PACKET_CODEC, HardeningRecipe::result,
            PacketCodecUtils.INT_PROVIDER_VALUE_CODEC, HardeningRecipe::amount,
            PacketCodecs.INTEGER, HardeningRecipe::hardeningTime,
            HardeningRecipe::new
    );

    @Override
    public ItemStack createIcon() {
        return PSItems.TRAY.getDefaultStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.HARDENING;
    }

    @Override
    public RecipeType<?> getType() {
        return PSRecipes.TRAY;
    }

    @Override
    public boolean matches(Input input, World world) {
        FluidMound fluids = FluidMound.of(input.impurities());
        return isCoreFluid(input.coreFluid())
            && solutions.stream().allMatch(i -> fluids.removeMatch(i, -1) > 0)
            && impurities.test(input.cuts());
    }

    public boolean isCoreFluid(ItemFluids fluids) {
        return coreFluid.test(fluids);
    }

    public boolean isValidImpurity(ItemFluids fluids) {
        return solutions.stream().anyMatch(i -> i.test(fluids))
            || fluids.isOf(PSFluids.PETROLIUM)
            || fluids.isOf(PSFluids.ETHANOL)
            || fluids.isOf(PSFluids.GASOLINE);
    }

    @Override
    public String getGroup() {
        return hardeningGroup;
    }

    @Override
    public ItemStack craft(Input input, WrapperLookup lookup) {
        ItemStack result = getResult(lookup);
        FluidMound fluids = FluidMound.of(input.impurities());
        fluids.split(fluid -> solutions.stream().anyMatch(i -> i.test(fluid)));

        Impurities inputCuts = impurities.permitted(input.cuts());

        int count = amount().get(input.random()) * (int)Math.pow(2, inputCuts.impurities().size());
        return Impurities.set(result.copyWithCount(count), inputCuts);
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResult(WrapperLookup lookup) {
        return result;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return coreFluid.isEmpty() && solutions.isEmpty();
    }

    public record Input(Random random, ItemFluids coreFluid, FluidMound impurities, Impurities cuts) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return coreFluid.isEmpty() && impurities.isEmpty();
        }
    }

}
