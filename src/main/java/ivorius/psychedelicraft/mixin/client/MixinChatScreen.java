package ivorius.psychedelicraft.mixin.client;

import java.time.Instant;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.MessageDistorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

@Mixin(ChatScreen.class)
abstract class MixinChatScreen extends Screen {
    MixinChatScreen() { super(null); }

    @ModifyReturnValue(method = "normalize(Ljava/lang/String;)Ljava/lang/String;", at = @At("RETURN"))
    private String onNormalize(String message) {
        return MessageDistorter.INSTANCE.distortOutgoingMessage(client.player, message);
    }
}

@Mixin(ChatMessages.class)
abstract class MixinChatMessages {
    @ModifyReturnValue(method = "getRenderedChatMessage(Ljava/lang/String;)Ljava/lang/String;", at = @At("RETURN"))
    private static String onGetRenderedChatMessage(String message) {
        return MessageDistorter.INSTANCE.distortIncomingMessage(MinecraftClient.getInstance().player, message);
    }
}

@Mixin(MessageHandler.class)
abstract class MixinMessageHandler {
    @ModifyReturnValue(method = "processChatMessageInternal(Lnet/minecraft/network/message/MessageType$Parameters;Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/text/Text;Lcom/mojang/authlib/GameProfile;ZLjava/time/Instant;)Z", at = @At("RETURN"))
    private boolean onProcessChatMessageInternal(boolean success, MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp) {
        if (success) {
            DrugProperties.of((Entity)MinecraftClient.getInstance().player).ifPresent(properties -> {
                properties.getHallucinations().getEntities().getChatBots().forEach(chatbot -> {
                    chatbot.onMessageReceived(sender.getName(), decorated);
                });
            });
        }
        return success;
    }
}