/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ivorius.psychedelicraft.ParticleHelper;
import ivorius.psychedelicraft.block.MashTubBlock;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.recipe.MashingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.util.MathUtils;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
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
    public Processable.ProcessType getProcessType() {
        return Processable.ProcessType.FERMENT;
    }

    @Override
    public void tick(ServerWorld world) {
        if (currentStew.isEmpty()) {

            Vec3d center = getPos().toCenterPos();
            Box box = Box.of(center, 1.5, 0.5, 1.5);
            for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, box, EntityPredicates.VALID_ENTITY)) {
                ItemStack stack = item.getStack();
                if (isValidIngredient(stack)) {
                    suppliedIngredients.put(stack.getItem(), suppliedIngredients.getInt(stack.getItem()) + stack.getCount());
                    beginStewing();
                    markForUpdate();
                    spawnBubbles(20, 0, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
                    getWorld().playSound(null, getPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1, 1);
                    item.discard();
                }
            }

            super.tick(world);
        }
        currentStew = currentStew.filter(Stew::tick);
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int difference) {
        super.onLevelChange(resovoir, difference);
        int luminance = resovoir.getContents().fluid().getPhysical().getDefaultState().getBlockState().getLuminance();

        int currentLuminance = getCachedState().get(MashTubBlock.LIGHT);
        if (luminance != currentLuminance) {
            world.setBlockState(getPos(), getCachedState().with(MashTubBlock.LIGHT, luminance));
        }

        if (!solidContents.isEmpty()) {
            super.accept(solidContents);
            solidContents = ItemStack.EMPTY;
        }
        if (difference > 0) {
            setTimeProcessed(0);
        }
    }

    public void tickAnimations() {
        if (!suppliedIngredients.isEmpty() && world.getRandom().nextFloat() < 0.33F && world.getTime() % 3 == 0) {
            spawnBubbles(1 + (int)(suppliedIngredients.size() * 1.5), 0, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
        }
    }

    public TypedActionResult<ItemStack> depositIngredient(ItemStack stack) {

        if (!currentStew.isEmpty()) {
            return TypedActionResult.fail(stack);
        }

        ItemFluids fluids = ItemFluids.of(stack);
        if (!fluids.isEmpty()) {
            Resovoir tank = getPrimaryTank();
            if (tank.getAmount() < tank.getCapacity()) {
                getWorld().playSound(null, getPos(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1, 1);
                return TypedActionResult.success(tank.deposit(stack));
            }
            return TypedActionResult.fail(stack);
        }

        if (isValidIngredient(stack)) {
            ItemStack consumed = stack.split(1);
            suppliedIngredients.computeInt(consumed.getItem(), (s, i) -> i == null ? 1 : (i + 1));
            beginStewing();
            markForUpdate();
            spawnBubbles(20, 0, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
            getWorld().playSound(null, getPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1, 1);
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    public boolean isValidIngredient(ItemStack stack) {
        return FluidCapacity.get(stack) == 0
            && (world.getRecipeManager()
                .listAllOfType(PSRecipes.MASHING_TYPE).stream()
                .filter(recipe -> recipe.value().baseFluid().canCombine(getPrimaryTank().getContents()))
                .flatMap(recipe -> recipe.value().getIngredients().stream())
                .anyMatch(i -> i.test(stack)));
    }

    public void beginStewing() {
        if (suppliedIngredients.isEmpty() || getWorld().isClient()) {
            return;
        }

        var input = new MashingRecipe.Input(this.getPrimaryTank().getContents(), solidContents, suppliedIngredients);
        var matchedRecipe = world.getRecipeManager().getAllMatches(PSRecipes.MASHING_TYPE, input, getWorld());

        if (matchedRecipe.isEmpty()) {
            onCraftingFailed();
        } else if (matchedRecipe.size() == 1 && matchedRecipe.get(0).value().hasMinimumRequirements(input)) {
            currentStew = Optional.of(new Stew(matchedRecipe.get(0)));
        }
    }

    private void onCraftingFailed() {
        suppliedIngredients.clear();
        currentStew = Optional.empty();
        getPrimaryTank().setContents(PSFluids.SLURRY.getDefaultStack(getPrimaryTank().getContents().amount()));
        spawnBubbles(90, 0.5F, SoundEvents.BLOCK_MUD_BREAK);
    }

    private void spawnBubbles(int count, float spread, SoundEvent sound) {
        Random random = getWorld().getRandom();
        Vec3d center = ParticleHelper.apply(getPos().toCenterPos(), x -> random.nextTriangular(x, 0.25));

        Resovoir tank = getPrimaryTank();
        ParticleHelper.spawnParticles(getWorld(),
                new DrugDustParticleEffect(PSParticles.BUBBLE, MathUtils.unpackRgb(tank.getContents().fluid().getColor(tank.getContents())), 1F),
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
    public List<ItemStack> getDroppedStacks(ItemStack container) {
        if (!solidContents.isEmpty()) {
            return List.of(solidContents);
        }
        return List.of();
    }

    @Override
    public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
        super.writeNbt(compound, lookup);
        if (!solidContents.isEmpty()) {
            compound.put("solidContents", solidContents.encodeAllowEmpty(lookup));
        }
        NbtCompound suppliedIngredsTag = new NbtCompound();
        suppliedIngredients.forEach((item, count) -> {
            suppliedIngredsTag.putInt(Registries.ITEM.getId(item).toString(), count);
        });
        compound.put("suppliedIngredients", suppliedIngredsTag);
    }

    @Override
    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        super.readNbt(compound, lookup);
        solidContents = compound.contains("solidContents", NbtElement.COMPOUND_TYPE)
                ? ItemStack.fromNbtOrEmpty(lookup, compound.getCompound("solidContents"))
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
        @Nullable
        private Identifier recipe;
        private int stewTime;

        public Stew(RecipeEntry<MashingRecipe> recipe) {
            this.recipe = recipe.id();
            this.stewTime = (2 + world.getRandom().nextInt(4)) + recipe.value().stewTime();
        }

        public boolean tick() {
            if (recipe == null) {
                markDirty();
                return false;
            }

            if (world.getTime() % 30 == 0) {
                spawnBubbles(9, 0.5F, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE);
                markDirty();
                if (--stewTime <= 0) {
                    if (world.getRecipeManager().get(recipe).map(RecipeEntry::value).orElse(null) instanceof MashingRecipe recipe) {
                        var input = new MashingRecipe.Input(getPrimaryTank().getContents(), solidContents, suppliedIngredients);
                        getPrimaryTank().setContents(recipe.result().ofAmount(getPrimaryTank().getContents().amount()));

                        recipe.getRemainder(input).forEach(stack -> {
                           Block.dropStack(world, getPos(), stack);
                        });
                    }
                    suppliedIngredients.clear();

                    return false;
                }
            }

            return true;
        }

        @Override
        public void toNbt(NbtCompound compound, WrapperLookup lookup) {
            compound.putInt("stewTime", stewTime);
            compound.putString("recipe", recipe.toString());
        }

        @Override
        public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
            stewTime = compound.getInt("stewTime");
            recipe = Identifier.validate(compound.getString("recipe")).result().orElse(null);
        }
    }
}
