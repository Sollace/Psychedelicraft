/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;

import com.google.common.base.Suppliers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ivorius.psychedelicraft.ParticleHelper;
import ivorius.psychedelicraft.block.MashTubBlock;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.particle.BubbleParticleEffect;
import ivorius.psychedelicraft.recipe.MashingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.util.MathUtils;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 27.10.14.
 */
public class MashTubBlockEntity extends FluidProcessingBlockEntity {
    public ItemStack solidContents = ItemStack.EMPTY;

    private Optional<Stew> currentStew = Optional.empty();

    private Optional<RecipeEntry<MashingRecipe>> expectedRecipe = Optional.empty();
    private final Object2IntMap<Item> suppliedIngredients = new Object2IntOpenHashMap<>();

    public MashTubBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.MASH_TUB, pos, state, FluidVolumes.VAT);
    }

    public Object2IntMap<Item> getSuppliedIngredients() {
        return suppliedIngredients;
    }

    @Override
    public void accept(ItemStack stack) {
        getPrimaryTank().clear();
        solidContents = stack;
    }

    @Override
    public void accept(MutableFluidContainer fluid) {
        getPrimaryTank().clear();
        getPrimaryTank().deposit(fluid.asStack());
    }

    @Override
    public Processable.ProcessType getProcessType() {
        return Processable.ProcessType.FERMENT;
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);
        currentStew = currentStew.filter(Stew::tick);
    }

    @Override
    public void onIdle(Resovoir resovoir) {
        super.onIdle(resovoir);
        int luminance = resovoir.getFluidType().getPhysical().getDefaultState().getBlockState().getLuminance();

        int currentLuminance = getCachedState().get(MashTubBlock.LIGHT);
        if (luminance != currentLuminance) {
            world.setBlockState(getPos(), getCachedState().with(MashTubBlock.LIGHT, luminance));
        }
    }

    public void tickAnimations() {
        if (!suppliedIngredients.isEmpty() && world.getRandom().nextFloat() < 0.33F && world.getTime() % 3 == 0) {
            spawnBubbles(1 + (int)(suppliedIngredients.size() * 1.5), 0, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
        }
    }

    public TypedActionResult<ItemStack> depositIngredient(ItemStack stack) {
        if (!FluidContainer.of(stack).getFluid(stack).isEmpty()) {
            Resovoir tank = getPrimaryTank();
            if (tank.getLevel() < tank.getCapacity()) {
                getWorld().playSound(null, getPos(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1, 1);
                onIdle(tank);
                return TypedActionResult.success(tank.deposit(stack));
            }
            return TypedActionResult.fail(stack);
        }

        if (isValidIngredient(stack)) {
            ItemStack consumed = stack.split(1);
            suppliedIngredients.computeInt(consumed.getItem(), (s, i) -> i == null ? 1 : (i + 1));
            checkIngredients();
            spawnBubbles(20, 0, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
            getWorld().playSound(null, getPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1, 1);
            onIdle(getPrimaryTank());
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(stack);
    }

    public boolean isValidIngredient(ItemStack stack) {
        return FluidContainer.of(stack, null) == null
            && world.getRecipeManager().listAllOfType(PSRecipes.MASHING_TYPE).stream()
                .map(RecipeEntry::value)
                .filter(recipe -> recipe.getPoolFluid().test(getPrimaryTank()))
                .flatMap(recipe -> recipe.getIngredients().stream())
                .anyMatch(ingredient -> ingredient.test(stack));
    }

    private void checkIngredients() {
        if (suppliedIngredients.isEmpty() || getWorld().isClient()) {
            return;
        }

        var expectedRecipeMatchPair = expectedRecipe.map(recipe -> Map.entry(recipe, recipe.value().matchPartially(suppliedIngredients)));
        var matchedRecipes = world.getRecipeManager().listAllOfType(PSRecipes.MASHING_TYPE).stream()
                .filter(recipe -> recipe.value().getPoolFluid().test(getPrimaryTank()))
                .map(recipe -> Map.entry(recipe, recipe.value().matchPartially(suppliedIngredients)))
                .filter(pair -> pair.getValue().isMatch())
                .toList();

        expectedRecipeMatchPair = expectedRecipeMatchPair.or(() -> matchedRecipes.stream().findFirst());
        expectedRecipe = expectedRecipeMatchPair.filter(pair -> pair.getValue().isMatch()).map(Map.Entry::getKey);

        if (expectedRecipeMatchPair.isEmpty()) {
            onCraftingFailed();
            return;
        }

        if (matchedRecipes.size() == 1) {
            currentStew = expectedRecipeMatchPair
                .filter(pair -> pair.getValue().isCraftable())
                .map(Map.Entry::getKey)
                .map(Stew::new);
            markForUpdate();
        }
    }

    private void onCraftingFailed() {
        suppliedIngredients.clear();
        currentStew = Optional.empty();
        getPrimaryTank().getContents().withFluid(PSFluids.SLURRY);
        spawnBubbles(90, 0.5F, SoundEvents.BLOCK_MUD_BREAK);
        onIdle(getPrimaryTank());
    }

    private void spawnBubbles(int count, float spread, SoundEvent sound) {
        Random random = getWorld().getRandom();
        Vec3d center = ParticleHelper.apply(getPos().toCenterPos(), x -> random.nextTriangular(x, 0.25));

        Resovoir tank = getPrimaryTank();
        ParticleHelper.spawnParticles(getWorld(),
                new BubbleParticleEffect(MathUtils.unpackRgbVector(tank.getFluidType().getColor(tank.getStack())), 1F),
                () -> ParticleHelper.apply(center, x -> random.nextTriangular(x, 0.5 + spread)).add(0, 0.5, 0),
                Suppliers.ofInstance(new Vec3d(
                        random.nextTriangular(0, 0.125),
                        random.nextTriangular(0.1, 0.125),
                        random.nextTriangular(0, 0.125)
                )),
                count
        );

        if (getWorld() instanceof ServerWorld) {
            getWorld().playSound(null, getPos(), sound, SoundCategory.BLOCKS,
                    0.5F + getWorld().getRandom().nextFloat(),
                    0.3F + getWorld().getRandom().nextFloat()
            );
        } else {
            getWorld().playSoundAtBlockCenter(getPos(), sound, SoundCategory.BLOCKS,
                    0.5F + getWorld().getRandom().nextFloat(),
                    0.3F + getWorld().getRandom().nextFloat(), true);
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(FluidContainer container) {
        if (!solidContents.isEmpty()) {
            return List.of(solidContents);
        }
        return List.of();
    }

    @Override
    public void onDrain(Resovoir resovoir) {
        if (!solidContents.isEmpty() && resovoir.isEmpty()) {
            setTimeProcessed(0);
        }
        onIdle(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        if (!solidContents.isEmpty()) {
            super.onFill(resovoir, amountFilled);
        } else {
            onIdle(resovoir);
        }
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        if (!solidContents.isEmpty()) {
            compound.put("solidContents", solidContents.writeNbt(new NbtCompound()));
        }
        NbtCompound suppliedIngredsTag = new NbtCompound();
        suppliedIngredients.forEach((item, count) -> {
            suppliedIngredsTag.putInt(Registries.ITEM.getId(item).toString(), count);
        });
        compound.put("suppliedIngredients", suppliedIngredsTag);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        solidContents = compound.contains("solidContents", NbtElement.COMPOUND_TYPE)
                ? ItemStack.fromNbt(compound.getCompound("solidContents"))
                : ItemStack.EMPTY;
        NbtCompound suppliedIngredsTag = compound.getCompound("suppliedIngredients");
        suppliedIngredients.clear();
        suppliedIngredsTag.getKeys().forEach(key -> {
            Optional.ofNullable(Identifier.tryParse(key)).map(Registries.ITEM::get).filter(Objects::nonNull).ifPresent(item -> {
                suppliedIngredients.put(item, suppliedIngredsTag.getInt(key));
            });
        });
    }

    class Stew implements NbtSerialisable {

        private RecipeEntry<MashingRecipe> recipe;
        private int stewTime;

        public Stew(RecipeEntry<MashingRecipe> recipe) {
            this.recipe = recipe;
            this.stewTime = -(2 + world.getRandom().nextInt(4));
        }

        public boolean tick() {
            markDirty();

            if (recipe == null) {
                return false;
            }
            if (world.getTime() % 30 == 0) {
                spawnBubbles(9, 0.5F, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE);

                if (++stewTime >= recipe.value().getStewTime()) {
                    suppliedIngredients.clear();
                    getPrimaryTank().getContents()
                        .withFluid(recipe.value().getOutputFluid().fluid())
                        .withAttributes(recipe.value().getOutputFluid().attributes());
                    onIdle(getPrimaryTank());

                    return false;
                }
            }

            return true;
        }

        @Override
        public void toNbt(NbtCompound compound) {
            compound.putInt("stewTime", stewTime);
            compound.putString("recipe", recipe.id().toString());
        }

        @SuppressWarnings("unchecked")
        @Override
        public void fromNbt(NbtCompound compound) {
            stewTime = compound.getInt("stewTime");
            recipe = (RecipeEntry<MashingRecipe>)Optional
                    .ofNullable(Identifier.tryParse(compound.getString("recipe")))
                    .flatMap(world.getRecipeManager()::get)
                    .orElse(null);
        }
    }
}
