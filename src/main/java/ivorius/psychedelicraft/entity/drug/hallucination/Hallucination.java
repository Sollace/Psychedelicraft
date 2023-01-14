/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.Optional;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

public abstract class Hallucination {

    public static final int UNLIMITED = -1;

    protected final PlayerEntity player;

    protected int age;

    protected Optional<ChatBot> chatBot = Optional.empty();

    public Hallucination(PlayerEntity player) {
        this.player = player;
    }

    public void update() {
        age++;
        chatBot.ifPresent(ChatBot::tick);
    }

    public Optional<ChatBot> getChatBot() {
        return chatBot;
    }

    public abstract void render(MatrixStack matrices, VertexConsumerProvider vertices, Camera camera, float tickDelta, float alpha);

    public abstract boolean isDead();

    public abstract int getMaxHallucinations();
}
