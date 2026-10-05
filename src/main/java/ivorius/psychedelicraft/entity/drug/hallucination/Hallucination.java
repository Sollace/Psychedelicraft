/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public abstract class Hallucination {

    public static final int UNLIMITED = -1;

    protected final PlayerEntity player;

    public int age;

    protected final Random random;

    public Hallucination(PlayerEntity player) {
        this.player = player;
        this.random = player.getRandom();
    }

    public void update(float alpha) {
        age++;
    }

    public abstract boolean isDead();

    public void setDead() {
        age = Integer.MAX_VALUE;
    }

    public float getAlpha(float tickDelta) {
        return 1;
    }

    public abstract int getMaxHallucinations();

    public abstract Identifier getType();

    @Nullable
    public ChatBot getChatBot() {
        return null;
    }
}
