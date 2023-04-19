package ivorius.psychedelicraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.*;

import ivorius.psychedelicraft.entity.drug.hallucination.EntityHallucinationType;
import ivorius.psychedelicraft.network.Channel;
import ivorius.psychedelicraft.network.MsgHallucinate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
class HallucinateCommand {
    private static final SuggestionProvider<ServerCommandSource> SUGGESTIONS = (context, builder) -> CommandSource.suggestIdentifiers(EntityHallucinationType.REGISTRY.keySet(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registries) {
        dispatcher.register(CommandManager.literal("hallucinate")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("type", IdentifierArgumentType.identifier()).suggests(SUGGESTIONS).executes(ctx -> {
                Identifier type = IdentifierArgumentType.getIdentifier(ctx, "type");
                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                Channel.HALLUCINATE.sendToPlayer(new MsgHallucinate(player.getId(), type), player);
                ctx.getSource().sendFeedback(Text.translatable("commands.hallucinate.success"), true);
                return 0;
            })
            .then(CommandManager.argument("target", EntityArgumentType.players()).executes(ctx -> {
                Identifier type = IdentifierArgumentType.getIdentifier(ctx, "type");
                ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
                Channel.HALLUCINATE.sendToPlayer(new MsgHallucinate(player.getId(), type), player);
                ctx.getSource().sendFeedback(Text.translatable("commands.hallucinate.success"), true);
                return 0;
            }))));
    }
}
