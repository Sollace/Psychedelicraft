package ivorius.psychedelicraft.commands;

import java.util.*;
import java.util.stream.Stream;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.*;

import ivorius.psychedelicraft.entities.drugs.*;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public class DrugCommand {
    public static final SimpleCommandExceptionType INVALID_DRUG_NAME = new SimpleCommandExceptionType(Text.translatable("commands.drug.nodrug"));
    private static final SuggestionProvider<ServerCommandSource> DRUG_NAME_SUGGESTIONS = (context, builder) -> CommandSource.suggestMatching(DrugRegistry.getAllDrugNames(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registries) {
        dispatcher.register(CommandManager.literal("drug")
            .then(CommandManager.argument("target", EntityArgumentType.players())
                .then(CommandManager.argument("drug name", StringArgumentType.word())
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
        String drugName = StringArgumentType.getString(context, "drug name");

        DrugProperties properties = DrugProperties.of(player);

        if ("all".equalsIgnoreCase(drugName)) {
            return properties.getAllDrugs().stream();
        }

        Drug drug = properties.getDrug(drugName);
        if (drug == null) {
            throw new CommandSyntaxException(INVALID_DRUG_NAME, Text.translatable("commands.drug.nodrug", drugName));
        }

        return Optional.of(properties.getDrug(drugName)).stream();
    }
}
