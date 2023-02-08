package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.function.Consumer;

import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

public interface Personality {
    Text getName(Random random);

    void supplyMessage(Random random, Consumer<Text> responseSender);

    void onMessageReceived(String sender, Text message, Random random, boolean fromPlayer, Consumer<Text> responseSender);
}