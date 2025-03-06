/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity.contents;

import java.util.Optional;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BurnerBlock;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity.Contents;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class EmptyContents implements BurnerBlockEntity.Contents {
    public static final Identifier ID = Psychedelicraft.id("empty");

    private final BurnerBlockEntity entity;

    public EmptyContents(BurnerBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public VoxelShape getOutlineShape() {
        return BurnerBlock.SHAPE;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Optional<Contents> interact(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        if (!isValidContainer(stack)) {
            return Optional.empty();
        }

        ItemStack container = stack.copyWithCount(1);
        int capacity = FluidCapacity.get(stack);
        stack.decrementUnlessCreative(1, player);

        entity.setContainer(container);
        entity.playSound(player, BlockSoundGroup.GLASS.getPlaceSound());
        return Optional.of(container.isOf(PSItems.BOTTLE) ? new LargeContents(entity, capacity, container) : new SmallContents(entity, capacity, container));
    }

    private boolean isValidContainer(ItemStack stack) {
        return stack.isIn(PSTags.Items.BUNSEN_BURNER_INSERTABLE);
    }

    public Contents getForStack(World world, BlockPos pos, BlockState state, ItemStack stack) {
        if (!isValidContainer(stack)) {
            return this;
        }
        world.playSoundAtBlockCenter(pos, BlockSoundGroup.GLASS.getPlaceSound(), SoundCategory.BLOCKS, 1, 1, true);
        int capacity = FluidCapacity.get(stack);
        return stack.isOf(PSItems.BOTTLE) ? new LargeContents(entity, capacity, stack) : new SmallContents(entity, capacity, stack);
    }

    @Override
    public void tick(ServerWorld world) {
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getFilled(ItemStack container, boolean dryRun, float drainPercentage) {
        return container;
    }
}
