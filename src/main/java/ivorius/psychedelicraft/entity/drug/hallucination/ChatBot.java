package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;

public class ChatBot {
    private final Personality personality;
    private final PlayerEntity player;

    private final List<Character> characters = new ArrayList<>();

    private final Queue<Runnable> incomingMessageQueue = new LinkedList<>();

    @Nullable
    private final MessageEmitter emitter;

    public ChatBot(Personality personality, PlayerEntity player, @Nullable MessageEmitter emitter) {
        this.personality = personality;
        this.player = player;
        this.emitter = emitter;
    }

    public void tick() {
        if (!player.getEntityWorld().isClient()) {
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

    public interface MessageEmitter {
        void onEmitMessage(String sender, Text message);
    }

    private boolean emitMessage(String sender, Text message) {
        HallucinationManager hallucinations = DrugProperties.of(player).getHallucinations();

        if (hallucinations.getEntityHallucinationAlphaTransparency(1) > 0) {
            player.sendMessage(message, false);
            incomingMessageQueue.add(() -> {
                getResponsiveCharacters(sender, message).forEach(character -> character.wakeUp(sender, message, false));
            });

            if (emitter != null) {
                emitter.onEmitMessage(sender, message);
            }

            return true;
        }

        return false;
    }

    public void onMessageReceived(String sender, Text message) {
        getResponsiveCharacters(sender, message).forEach(character -> character.wakeUp(sender, message, true));
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

        private final Queue<DelayedMessage> messageQueue = new LinkedList<>();

        private final Text name = personality.getName(player.getRandom());
        private final MessageType.Parameters parameters = MessageType.params(MessageType.CHAT, player.getEntityWorld().getRegistryManager(), name);

        public boolean tick() {
            if (sleepTicks-- > 0) {
                return false;
            }

            if (messageQueue.isEmpty()) {
                if (idleTicks++ > 300 && player.getRandom().nextInt(300) == 0) {
                    if (player.getRandom().nextInt(120) == 0) {
                        return true;
                    }

                    personality.supplyMessage(player.getRandom(), line -> {
                        messageQueue.add(new DelayedMessage(line));
                    });
                    sleepTicks = player.getRandom().nextBetween(5, 100);
                    idleTicks = 0;
                }
            }

            DelayedMessage message = messageQueue.peek();
            if (message != null) {
                if (message.tick()) {
                    messageQueue.poll();
                }
                sleepTicks = player.getRandom().nextBetween(2, 20);
            }

            return false;
        }

        public void wakeUp(String sender, Text message, boolean fromPlayer) {
            messageQueue.clear();
            personality.onMessageReceived(sender, message, player.getRandom(), fromPlayer, line -> {
                messageQueue.add(new DelayedMessage(line));
            });
            idleTicks = 0;
            sleepTicks = player.getRandom().nextBetween(1, 5);
        }

        class DelayedMessage {
            Text message;
            int delay;

            public DelayedMessage(Text message) {
                this.message = message;
                this.delay = player.getRandom().nextBetween(2, 20);
            }

            public boolean tick() {
                if (--delay <= 0) {
                    return emitMessage(name.getString(), parameters.applyChatDecoration(message));
                }
                return false;
            }
        }
    }
}
