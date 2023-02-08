package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;

public class ChatBot {
    private final Personality personality;
    private final PlayerEntity player;

    private final List<Character> characters = new ArrayList<>();

    private final Queue<Runnable> incomingMessageQueue = new LinkedList<>();

    public ChatBot(Personality personality, PlayerEntity player) {
        this.personality = personality;
        this.player = player;
    }

    public void tick() {
        if (!player.world.isClient) {
            return;
        }

        if (characters.size() < 3 && player.getRandom().nextInt(200) == 0) {
            characters.add(new Character());
        }

        Runnable action;
        while ((action = incomingMessageQueue.poll()) != null) {
            action.run();
        }

        characters.removeIf(Character::tick);
    }

    private void emitMessage(String sender, Text message) {
        player.sendMessage(message);
        incomingMessageQueue.add(() -> onMessageReceived(sender, message));
    }

    public void onMessageReceived(String sender, Text message) {
        getResponsiveCharacters(sender, message).forEach(character -> character.wakeUp(message));
    }

    private List<Character> getResponsiveCharacters(String sender, Text message) {
        String txt = message.getString();
        var allCharacters = characters.stream().filter(character -> !sender.contentEquals(character.name.getString())).toList();
        if (allCharacters.isEmpty()) {
            return allCharacters;
        }
        var mentionedCharacters = characters.stream().filter(character -> txt.contains(character.name.getString())).toList();
        if (mentionedCharacters.isEmpty()) {
            return List.of(allCharacters.get(player.getRandom().nextInt(allCharacters.size())));
        }
        return mentionedCharacters;
    }

    final class Character {
        private int idleTicks;
        private int sleepTicks;

        private final Queue<Text> messageQueue = new LinkedList<>();

        private final Text name = personality.getName(player.getRandom());
        private final MessageType.Parameters parameters = MessageType.params(MessageType.CHAT, player.world.getRegistryManager(), name);

        public boolean tick() {
            if (sleepTicks-- > 0) {
                return false;
            }

            if (messageQueue.isEmpty()) {
                if (idleTicks++ > 300 && player.getRandom().nextInt(300) == 0) {
                    if (player.getRandom().nextInt(120) == 0) {
                        return true;
                    }

                    personality.supplyMessage(player.getRandom(), messageQueue::add);
                    sleepTicks = player.getRandom().nextBetween(2, 20);
                    idleTicks = 0;
                }
            }

            Text message = messageQueue.poll();
            if (message != null) {
                emitMessage(name.getString(), parameters.applyChatDecoration(message));
                sleepTicks = player.getRandom().nextBetween(2, 20);
            }

            return false;
        }

        public void wakeUp(Text message) {
            messageQueue.clear();
            personality.supplyMessage(player.getRandom(), messageQueue::add);
            idleTicks = 0;
            sleepTicks = player.getRandom().nextBetween(1, 5);
        }
    }
}
