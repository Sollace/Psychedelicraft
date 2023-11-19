/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import ivorius.psychedelicraft.Psychedelicraft;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by lukas on 22.05.14.
 */
public class MessageDistorter {
    public static final MessageDistorter INSTANCE = new MessageDistorter();

    public String distortIncomingMessage(PlayerEntity player, String message) {
        if (player == null || !Psychedelicraft.getConfig().balancing.messageDistortion.incoming) {
            return message;
        }
        return distortMessage(DrugProperties.of(player), message);
    }

    public String distortOutgoingMessage(PlayerEntity player, String message) {
        if (player == null || !Psychedelicraft.getConfig().balancing.messageDistortion.outgoing) {
            return message;
        }

        if (message.indexOf("/") == 0) {
            return message;
        }

        return distortMessage(DrugProperties.of(player), message);
    }

    private String distortMessage(DrugProperties properties, String message) {

        float alcohol = Math.min(properties.getDrugValue(DrugType.ALCOHOL) + properties.getDrugValue(DrugType.KAVA), 1);
        float zero = properties.getDrugValue(DrugType.ZERO);
        float cannabis = properties.getDrugValue(DrugType.CANNABIS);
        if (alcohol > 0 || zero > 0 || cannabis > 0) {
            return distortMessage(message, properties.asEntity().getRandom(), alcohol, zero, cannabis);
        }
        return message;
    }

    private String getRandomTranslation(String keyBeggining, Random random) {
        int i = 0;
        while (true) {
            if (!I18n.hasTranslation(keyBeggining + i)) {
                return i < 1 ? keyBeggining + i : I18n.translate(keyBeggining + random.nextInt(i));
            }
            i++;
        }
    }

    public String distortMessage(String message, Random random, float alcohol, float zero, float cannabis) {
        StringBuilder builder = new StringBuilder();

        float randomCaseChance = MathHelper.lerp(alcohol, 0.3f, 1) * 0.06f + MathHelper.lerp(zero, 0, 0.3f);
        float randomLetterChance = MathHelper.lerp(alcohol, 0.5f, 1) * 0.015f;
        float sToShChance = MathHelper.lerp(alcohol, 0.2f, 0.6f);
        float longShChance = alcohol * 0.8f;
        float hicChance = MathHelper.lerp(alcohol, 0.5f, 1) * 0.04f;
        float rewindChance = MathHelper.lerp(alcohol, 0.4f, 0.9f) * 0.03f;
        float longCharChance = MathHelper.lerp(alcohol, 0.3f, 1) * 0.025f;

        float oneZeroChance = MathHelper.lerp(zero, 0.6f, 0.95f);
        float randomCharChance = MathHelper.lerp(zero, 0.2f, 0.95f);

        float fillerWordChance = MathHelper.lerp(cannabis, 0.2f, 0.95f) * 0.1f;
        float startFillerWordChance = MathHelper.lerp(cannabis, 0.2f, 0.95f) * 0.7f;

        boolean wasPoint = true;
        for (int i = 0; i < message.length(); i++) {
            char origChar = message.charAt(i);
            char curChar = origChar;

            if (random.nextFloat() < oneZeroChance) {
                curChar = random.nextBoolean() ? '0' : '1';
            } else if (random.nextFloat() < randomCharChance) {
                curChar = (char) (' ' + random.nextInt(('~' - ' ' + 1)));
            } else if (random.nextFloat() < randomLetterChance) {
                curChar = (char) ((random.nextBoolean() ? 'a' : 'A') + random.nextInt(26));
            } else if (random.nextFloat() < randomCaseChance) {
                if (random.nextBoolean()) {
                    if (Character.isUpperCase(curChar)) {
                        curChar = Character.toLowerCase(curChar);
                    } else {
                        curChar = Character.toUpperCase(curChar);
                    }
                }
            }

            if ((curChar == 's' || curChar == 'S') && random.nextFloat() < sToShChance) {
                builder.append(curChar).append(random.nextFloat() < longShChance ? "hh" : "h");
            } else if (curChar == ' ' && random.nextFloat() < fillerWordChance) {
                builder.append(getRandomTranslation("distortion.psychedelicraft.filler_words.", random));
            } else if (wasPoint && random.nextFloat() < startFillerWordChance) {
                builder.append(getRandomTranslation("distortion.psychedelicraft.start_filler_words.", random)).append(curChar);
            } else {
                builder.append(curChar);
            }

            wasPoint = false; // Grammar and stuff... I'd rather be safe

            if (random.nextFloat() < longCharChance) {
                float moreChance = 0.6f * 2;
                do {
                    moreChance *= 0.5f;
                    builder.append(curChar);
                } while (random.nextFloat() < moreChance);
            }

            if (random.nextFloat() < hicChance) {
                builder.append(getRandomTranslation("distortion.psychedelicraft.hics.", random));
            }

            if (random.nextFloat() < rewindChance) {
                builder.append("... ");
                int wordsRewind = random.nextInt(5) + 1;
                for (int j = 0; j < wordsRewind; j++) {
                    i = message.lastIndexOf(" ", i - 1);
                }

                if (i < 0) {
                    i = 0;
                }
            }
        }

        return builder.toString();
    }
}
