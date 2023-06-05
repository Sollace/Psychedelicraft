package ivorius.psychedelicraft.command;

import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.*;

import ivorius.psychedelicraft.entity.drug.hallucination.EntityHallucinationType;
import ivorius.psychedelicraft.network.Channel;
import ivorius.psychedelicraft.network.MsgHallucinate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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
                        sendHallucination(ctx, ctx.getSource().getPlayerOrThrow(), Optional.empty());
                        return 0;
                    })
                    .then(CommandManager.argument("target", EntityArgumentType.players()).executes(ctx -> {
                        EntityArgumentType.getPlayers(ctx, "target").forEach(player -> {
                            sendHallucination(ctx, player, Optional.empty());
                        });
                        return 0;
                    }))
                    .then(CommandManager.argument("position", BlockPosArgumentType.blockPos()).executes(ctx -> {
                        sendHallucination(ctx, ctx.getSource().getPlayerOrThrow(), Optional.of(BlockPosArgumentType.getBlockPos(ctx, "position")));
                        return 0;
                    }))
            )
        );
    }

    private static void sendHallucination(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, Optional<BlockPos> position) {
        Identifier type = IdentifierArgumentType.getIdentifier(ctx, "type");
        Channel.HALLUCINATE.sendToPlayer(new MsgHallucinate(player.getId(), type, position), player);
        ctx.getSource().sendFeedback(() -> Text.translatable("commands.hallucinate.success"), true);
    }
}
