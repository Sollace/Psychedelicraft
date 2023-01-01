/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */
/*
TODO: (Sollace) Need to reimplement IvChatBot
package ivorius.psychedelicraft.entities.drugs;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Random;

public class ChatBotRastahead extends IvChatBot
{
    public static String[][] randomStatements = new String[][]
            {
                    new String[]{"<Reggie> Did you ever notice, like...", "<Reggie> Sorry, I forgot what I was trying to say"},
                    new String[]{"<Reggie> Want another joint? I got so many, like, 2 or 3 more..."}
            };

    public static String[][] responseMessagesPlayer = new String[][]
            {
                    new String[]{"<Reggie> Haha, you're so right."},
                    new String[]{"<Reggie> Haha, he's so right."},
                    new String[]{"<Reggie> Yes."}
            };

    public static String[][] responseMessagesOther = new String[][]
            {
                    new String[]{"<Reggie> Haha, he's so right."}
            };

    public PlayerEntity playerEntity;

    public ChatBotRastahead(Random random, PlayerEntity player)
    {
        super(random);

        this.playerEntity = player;
    }

    @Override
    public void updateIdle()
    {
        if (random.nextInt(300) == 0)
        {
            addMessagesToSendQueue(randomStatements[random.nextInt(randomStatements.length)]);
        }
    }

    @Override
    public void receiveChatMessage(String message)
    {
        int tagEndIndex = message.indexOf(">");
        boolean hasSender = message.indexOf("<") == 0 && tagEndIndex > 0;

        if (hasSender)
        {
            String sender = message.substring(1, tagEndIndex);
            String fullMessage = message.substring(tagEndIndex + 1);

            boolean responded = false;

            if (!responded && random.nextFloat() < 0.4f)
            {
                if (sender.equals(playerEntity.getCommandSenderName()))
                {
                    addMessagesToSendQueue(responseMessagesPlayer[playerEntity.getRNG().nextInt(responseMessagesPlayer.length)]);
                }
                else if (sender.equals("Reggie"))
                {

                }
                else
                {
                    addMessagesToSendQueue(responseMessagesOther[playerEntity.getRNG().nextInt(responseMessagesOther.length)]);
                }

                responded = true;
            }
        }
    }
}
*/