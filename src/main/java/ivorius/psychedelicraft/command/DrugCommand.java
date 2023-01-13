package ivorius.psychedelicraft.command;

import java.util.stream.Stream;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.*;

import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public class DrugCommand {
    public static final SimpleCommandExceptionType INVALID_DRUG_NAME = new SimpleCommandExceptionType(Text.translatable("commands.drug.nodrug"));
    private static final SuggestionProvider<ServerCommandSource> DRUG_NAME_SUGGESTIONS = (context, builder) -> CommandSource.suggestIdentifiers(DrugType.REGISTRY.getIds(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registries) {
        dispatcher.register(CommandManager.literal("drug")
            .then(CommandManager.argument("target", EntityArgumentType.players())
                .then(CommandManager.argument("drug", IdentifierArgumentType.identifier())
                        .suggests(DRUG_NAME_SUGGESTIONS)
                    .then(CommandManager.literal("lock")
                        .then(CommandManager.argument("locked", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean locked = BoolArgumentType.getBool(context, "locked");
                                    getDrugs(context).forEach(drug -> drug.setLocked(locked));
                                    return 0;
                                })
                        )
                    )
                    .then(CommandManager.literal("set")
                        .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, 1)).executes(context -> {
                            double value = DoubleArgumentType.getDouble(context, "value");
                            getDrugs(context).forEach(drug -> drug.setDesiredValue(value));
                            return 0;
                        }))
                    )
                )
            )
        );
    }

    static Stream<Drug> getDrugs(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = EntityArgumentType.getPlayer(context, "target");
        Identifier drugName = IdentifierArgumentType.getIdentifier(context, "drug");
        DrugProperties properties = DrugProperties.of(player);

        if ("all".equals(drugName.getPath())) {
            return properties.getAllDrugs().stream();
        }

        return DrugType.REGISTRY.getOrEmpty(drugName).map(properties::getDrug).stream();
    }
}
