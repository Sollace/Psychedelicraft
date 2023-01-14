package ivorius.psychedelicraft.entity.drug.hallucination;
/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.util.function.Consumer;

public class RastaheadPersonality implements Personality {
    private static final String[][] RANDOM_STATEMENTS = {
            {"Did you ever notice, like...", "Sorry, I forgot what I was trying to say"},
            {"Want another joint? I got so many, like, 2 or 3 more..."}
    };

    private static final String[][] RESPONSES_TO_PLAYER = {
            {"Haha, you're so right."},
            {"Haha, he's so right."},
            {"Yes."}
    };

    private static final String[][] RESPONSES_OTHER = {
            {"Haha, he's so right."}
    };

    @Override
    public Text getName(Random random) {
        return Text.literal("Reggie");
    }

    @Override
    public void supplyMessage(Random random, Consumer<Text> responseSender) {
        for (String line : RANDOM_STATEMENTS[random.nextInt(RANDOM_STATEMENTS.length)]) {
            responseSender.accept(Text.literal(line));
        }
    }
/*
    @Override
    public void updateIdle() {
        if (random.nextInt(300) == 0) {
            addMessagesToSendQueue();
        }
    }
*/
    @Override
    public void onMessageReceived(Text text, Random random, boolean fromPlayer, Consumer<Text> responseSender) {
        String message = text.getString();
        int tagEndIndex = message.indexOf(">");
        boolean hasSender = message.indexOf("<") == 0 && tagEndIndex > 0;

        if (hasSender) {
            String sender = message.substring(1, tagEndIndex);

            boolean responded = false;

            if (!responded && random.nextFloat() < 0.4f) {
                if (fromPlayer) {
                    for (String response : RESPONSES_TO_PLAYER[random.nextInt(RESPONSES_TO_PLAYER.length)]) {
                        responseSender.accept(Text.literal(response));
                    }

                } else if (!sender.equals("Reggie")) {
                    for (String response : RESPONSES_OTHER[random.nextInt(RESPONSES_OTHER.length)]) {
                        responseSender.accept(Text.literal(response));
                    }
                }

                responded = true;
            }
        }
    }
}
