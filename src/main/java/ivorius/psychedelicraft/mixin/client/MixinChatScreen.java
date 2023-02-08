package ivorius.psychedelicraft.mixin.client;

import java.time.Instant;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "normalize(Ljava/lang/String;)Ljava/lang/String;",
            at = @At("RETURN"),
            cancellable = true)
    public void onNormalize(String chatText, CallbackInfoReturnable<String> info) {
        info.setReturnValue(MessageDistorter.INSTANCE.distortOutgoingMessage(client.player, info.getReturnValue()));
    }
}

@Mixin(ChatMessages.class)
abstract class MixinChatMessages {
    @Inject(method = "getRenderedChatMessage(Ljava/lang/String;)Ljava/lang/String;",
            at = @At("RETURN"),
            cancellable = true)
    private static void onGetRenderedChatMessage(String message, CallbackInfoReturnable<String> info) {
        info.setReturnValue(MessageDistorter.INSTANCE.distortIncomingMessage(MinecraftClient.getInstance().player, info.getReturnValue()));
    }
}

@Mixin(MessageHandler.class)
abstract class MixinMessageHandler {
    @Inject(method = "processChatMessageInternal", at = @At("RETURN"))
    private void onProcessChatMessageInternal(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValueZ()) {
            DrugProperties.of((Entity)MinecraftClient.getInstance().player).ifPresent(properties -> {
                properties.getHallucinations().getEntities().getChatBots().forEach(chatbot -> {
                    chatbot.onMessageReceived(sender.getName(), decorated);
                });
            });
        }
    }
}