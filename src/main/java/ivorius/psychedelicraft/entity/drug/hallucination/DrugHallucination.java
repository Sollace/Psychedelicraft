/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public abstract class DrugHallucination {

    public static final int UNLIMITED = -1;

    public PlayerEntity player;

    public int entityTicksAlive;

    public IvChatBot chatBot;

    public DrugHallucination(PlayerEntity player) {
        this.player = player;
    }

    public void update() {
        entityTicksAlive++;

        if (chatBot != null) {
            String sendString = chatBot.update();

            if (sendString != null) {
                player.sendMessage(Text.literal(sendString));
            }
        }
    }

    public void receiveChatMessage(String message, LivingEntity entity) {
        if (this.chatBot != null) {
            this.chatBot.receiveChatMessage(message);
        }
    }

    public abstract void render(float par1, float dAlpha);

    public abstract boolean isDead();

    public abstract int getMaxHallucinations();

    public interface IvChatBot {
        String update();

        void receiveChatMessage(String message);
    }
}
