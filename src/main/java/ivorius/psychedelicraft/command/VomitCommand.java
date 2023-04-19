package ivorius.psychedelicraft.command;

import com.mojang.brigadier.CommandDispatcher;
import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Sollace
 * @since 18 April 2023
 */
class VomitCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registries) {
        dispatcher.register(CommandManager.literal("vomit").requires(source -> source.hasPermissionLevel(2)).executes(ctx -> {
            DrugProperties.of(ctx.getSource().getPlayerOrThrow()).getStomach().vomit();
            return 0;
        })
            .then(CommandManager.argument("target", EntityArgumentType.players()).executes(ctx -> {
                ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
                DrugProperties.of(player).getStomach().vomit();
                return 0;
            })
        ));
    }
}
