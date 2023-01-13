/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import java.util.List;

import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Arm;

/**
 * Created by lukas on 25.03.14.
 */
public class FakeSunEntity extends LivingEntity {
    public Entity prevViewEntity;

    public FakeSunEntity(Entity prevViewEntity) {
        super(EntityType.PLAYER, prevViewEntity.world);
        this.prevViewEntity = prevViewEntity;

        NbtCompound cmp = new NbtCompound();
        prevViewEntity.writeNbt(cmp);
        readNbt(cmp);
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return List.of();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot var1) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot var1, ItemStack var2) {
    }

    @Override
    public Arm getMainArm() {
        return Arm.LEFT;
    }
}
