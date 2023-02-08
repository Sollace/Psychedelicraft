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
            {"Want another joint? I got so many, like, 2 or 3 more..."},
            {"Duuuude...", "DuuuuuuuuuuuuuuuuuuDe!", "DuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuDe!", "Yo dudeliodude!"},
            {"Woah, like, woah", "That's, like, so, rad, dude"},
            {"You know what would go excellently with this?", "DIRT"},
            {"Like, hear me out","Just this once","Come on, come oon"},
            {"I am SO high right now","Look at me flyyyyy..."},
            {"OoOoOOowOoOoOo", "SpooOoOoOoOky floating Head"},
            {"Creeper!"},
            {"Behind you"},
            {"Get him"},
            {"Ahahahahaaaa"},
            {"My minds is, like, so opened right now", "I'm like a can of beans","If you... yknow..Used a can opener on me","I'm like. My mind is like an opened can of beans","Like a can of beans you opened"},
            {"You not gonna eat my beans, are you?"},
            {"Don't look now, but I think this guy's a little bit sus"},
            {"It's aaaaalll natural, babey"}
    };

    private static final String[][] RESPONSES_TO_PLAYER = {
            {"Haha, you're so right."},
            {"Haha, he's so right."},
            {"Yes."},
            {"Great idea"},
            {"Wooooooooah", "That's, like, dued", "Are you, like, a genie or something?"},
            {"Wooooooah"},
            {"My minds is, like, so opened right now"}
    };

    private static final String[][] RESPONSES_OTHER = {
            {"Haha, he's so right."},
            {"This guy, I like this guy"},
            {"Don't look now, but I think this guy's a little sus"}
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

    @Override
    public void onMessageReceived(String sender, Text message, Random random, boolean fromPlayer, Consumer<Text> responseSender) {
        if (random.nextFloat() < 0.4f) {
            if (fromPlayer) {
                for (String response : RESPONSES_TO_PLAYER[random.nextInt(RESPONSES_TO_PLAYER.length)]) {
                    responseSender.accept(Text.literal(response));
                }

            } else {
                for (String response : RESPONSES_OTHER[random.nextInt(RESPONSES_OTHER.length)]) {
                    responseSender.accept(Text.literal(response));
                }
            }
        }
    }
}
