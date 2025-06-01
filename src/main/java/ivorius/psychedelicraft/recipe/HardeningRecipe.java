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
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public record HardeningRecipe(
        String hardeningGroup,
        FluidIngredient coreFluid,
        List<FluidIngredient> impurities,
        Impurities cuts,
        ItemStack result,
        IntProvider amount,
        int hardeningTime
    ) implements Recipe<HardeningRecipe.Input> {
    public static final MapCodec<HardeningRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("group").forGetter(HardeningRecipe::hardeningGroup),
            FluidIngredient.CODEC.fieldOf("core_fluid").forGetter(HardeningRecipe::coreFluid),
            FluidIngredient.CODEC.listOf().fieldOf("impurities").forGetter(HardeningRecipe::impurities),
            Impurities.CODEC.optionalFieldOf("cuts", Impurities.EMPTY).forGetter(HardeningRecipe::cuts),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(HardeningRecipe::result),
            IntProvider.POSITIVE_CODEC.fieldOf("amount").forGetter(HardeningRecipe::amount),
            Codec.INT.optionalFieldOf("hardening_time", 20).forGetter(HardeningRecipe::hardeningTime)
    ).apply(i, HardeningRecipe::new));
    public static final PacketCodec<RegistryByteBuf, HardeningRecipe> PACKET_CODEC = PacketCodecUtils.tuple(
            PacketCodecs.STRING, HardeningRecipe::hardeningGroup,
            FluidIngredient.PACKET_CODEC, HardeningRecipe::coreFluid,
            FluidIngredient.PACKET_CODEC.collect(PacketCodecs.toList()), HardeningRecipe::impurities,
            Impurities.PACKET_CODEC, HardeningRecipe::cuts,
            ItemStack.OPTIONAL_PACKET_CODEC, HardeningRecipe::result,
            PacketCodecUtils.INT_PROVIDER_VALUE_CODEC, HardeningRecipe::amount,
            PacketCodecs.INTEGER, HardeningRecipe::hardeningTime,
            HardeningRecipe::new
    );

    @Override
    public RecipeSerializer<HardeningRecipe> getSerializer() {
        return PSRecipes.HARDENING;
    }

    @Override
    public RecipeType<HardeningRecipe> getType() {
        return PSRecipes.TRAY;
    }

    @Override
    public boolean matches(Input input, World world) {
        FluidMound fluids = FluidMound.of(input.impurities());
        return isCoreFluid(input.coreFluid()) && impurities.stream().allMatch(i -> fluids.removeMatch(i) > 0);
    }

    public boolean isCoreFluid(ItemFluids fluids) {
        return coreFluid.test(fluids);
    }

    public boolean isValidImpurity(ItemFluids fluids) {
        return impurities.stream().anyMatch(i -> i.test(fluids)) || fluids.isOf(PSFluids.PETROLIUM) || fluids.isOf(PSFluids.ETHANOL) || fluids.isOf(PSFluids.GASOLINE);
    }

    @Override
    public String getGroup() {
        return hardeningGroup;
    }

    @Override
    public ItemStack craft(Input input, WrapperLookup lookup) {
        FluidMound fluids = FluidMound.of(input.impurities());
        fluids.split(fluid -> impurities.stream().anyMatch(i -> i.test(fluid)));

        Impurities inputCuts = Impurities.overlap(input.cuts(), cuts);

        int count = amount().get(input.random()) * (int)Math.pow(2, inputCuts.impurities().size());
        return Impurities.set(result.copyWithCount(count), inputCuts);
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(
            new ShapelessCraftingRecipeDisplay(
                List.of(),
                new SlotDisplay.StackSlotDisplay(result),
                new SlotDisplay.ItemSlotDisplay(PSItems.TRAY)
            )
        );
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public record Input(Random random, ItemFluids coreFluid, FluidMound impurities, Impurities cuts) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return coreFluid.isEmpty() && impurities.isEmpty();
        }
    }
}
