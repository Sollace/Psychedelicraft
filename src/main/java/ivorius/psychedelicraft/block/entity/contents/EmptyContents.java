/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity.contents;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity.Contents;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;

public class EmptyContents implements BurnerBlockEntity.Contents {
    public static final Identifier ID = Psychedelicraft.id("empty");

    private final BurnerBlockEntity entity;

    public EmptyContents(BurnerBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public TypedActionResult<Contents> interact(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {
        if (stack.isOf(Items.GLASS_BOTTLE)
                || stack.isOf(PSItems.FILLED_GLASS_BOTTLE)
                || stack.isOf(Items.POTION)
                || stack.isOf(PSItems.BOTTLE)
        ) {
            entity.setContainer(ItemFluids.set(stack.splitUnlessCreative(1, player), ItemFluids.EMPTY));
            entity.playSound(player, BlockSoundGroup.GLASS.getPlaceSound());
            int capacity = FluidCapacity.get(stack);
            return TypedActionResult.success(stack.isOf(PSItems.BOTTLE) ? new LargeContents(entity, capacity, stack) : new SmallContents(entity, capacity, stack));
        }

        return TypedActionResult.fail(this);
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

}
