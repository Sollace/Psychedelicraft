package ivorius.psychedelicraft.command;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.*;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public class DrugCommand {
    private static final Identifier ALL = Psychedelicraft.id("all");
    public static final SimpleCommandExceptionType INVALID_DRUG_NAME = new SimpleCommandExceptionType(Text.translatable("commands.drug.nodrug"));
    private static final SuggestionProvider<ServerCommandSource> DRUG_NAME_SUGGESTIONS = (context, builder) -> CommandSource.suggestIdentifiers(DrugType.REGISTRY.getIds(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registries) {
        dispatcher.register(CommandManager.literal("drug")
            .then(CommandManager.argument("target", EntityArgumentType.players())
                .then(CommandManager.literal("lock")
                    .then(CommandManager.literal("all")
                        .then(CommandManager.argument("locked", BoolArgumentType.bool()).executes(ctx -> lockDrugs(ctx, ALL))))
                    .then(CommandManager.argument("drug", IdentifierArgumentType.identifier()).suggests(DRUG_NAME_SUGGESTIONS)
                        .then(CommandManager.argument("locked", BoolArgumentType.bool()).executes(ctx -> lockDrugs(ctx, IdentifierArgumentType.getIdentifier(ctx, "drug")))
                    )
                ))
                .then(CommandManager.literal("get")
                    .then(CommandManager.argument("drug", IdentifierArgumentType.identifier()).suggests(DRUG_NAME_SUGGESTIONS).executes(DrugCommand::getDrugs)
                ))
                .then(CommandManager.literal("set")
                    .then(CommandManager.literal("all")
                        .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, 1)).executes(ctx -> setDrugs(ctx, ALL))))
                    .then(CommandManager.argument("drug", IdentifierArgumentType.identifier()).suggests(DRUG_NAME_SUGGESTIONS)
                        .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, 1)).executes(ctx -> setDrugs(ctx, IdentifierArgumentType.getIdentifier(ctx, "drug")))))
                )
                .then(CommandManager.literal("add")
                    .then(CommandManager.literal("all")
                        .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, 1)).executes(ctx -> addToDrugs(ctx, ALL))))
                    .then(CommandManager.argument("drug", IdentifierArgumentType.identifier()).suggests(DRUG_NAME_SUGGESTIONS)
                        .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, 1)).executes(ctx -> addToDrugs(ctx, IdentifierArgumentType.getIdentifier(ctx, "drug")))))
                )
            )
        );
    }

    private static int getDrugs(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
        Identifier drugName = IdentifierArgumentType.getIdentifier(context, "drug");
        DrugProperties properties = DrugProperties.of(player);

        DrugType.REGISTRY.getOrEmpty(drugName).ifPresentOrElse(drugType -> {
            float value = properties.isDrugActive(drugType) ? properties.getDrugValue(drugType) : 0;
            source.sendFeedback(Text.translatable("commands.drug.success.get." + (player == source.getEntity() ? "self" : "other"), drugName.getPath(), value), true);
        }, () -> {
            source.sendFeedback(Text.translatable("commands.drug.fail.get", drugName), true);
        });

        return 0;
    }

    private static int lockDrugs(CommandContext<ServerCommandSource> context, Identifier drugName) throws CommandSyntaxException {
        boolean locked = BoolArgumentType.getBool(context, "locked");
        applyDrugChange(context, drugName, drug -> drug.setLocked(locked), (player, type) -> {
            if (type == UpdateType.NONE) {
                sendFeedback(context.getSource(), player, false, "none", drugName);
            } else if (type == UpdateType.ALL) {
                sendFeedback(context.getSource(), player, true, (locked ? "lock" : "unlock") + ".all");
            } else {
                sendFeedback(context.getSource(), player, true, (locked ? "lock" : "unlock"), drugName);
            }
        });
        return 0;
    }

    private static int setDrugs(CommandContext<ServerCommandSource> context, Identifier drugName) throws CommandSyntaxException {
        double value = DoubleArgumentType.getDouble(context, "value");
        applyDrugChange(context, drugName, drug -> drug.setDesiredValue(value), (player, type) -> {
            if (type == UpdateType.NONE) {
                sendFeedback(context.getSource(), player, false, "set", drugName);
            } else if (type == UpdateType.ALL) {
                sendFeedback(context.getSource(), player, true, "set.all", value);
            } else {
                sendFeedback(context.getSource(), player, true, "set", drugName.getPath(), value);
            }
        });

        return 0;
    }

    private static int addToDrugs(CommandContext<ServerCommandSource> context, Identifier drugName) throws CommandSyntaxException {
        double value = DoubleArgumentType.getDouble(context, "value");
        applyDrugChange(context, drugName, drug -> drug.addToDesiredValue(value), (player, type) -> {
            if (type == UpdateType.NONE) {
                sendFeedback(context.getSource(), player, false, "add", drugName);
            } else if (type == UpdateType.ALL) {
                sendFeedback(context.getSource(), player, true, "add.all", value);
            } else {
                sendFeedback(context.getSource(), player, true, "add", drugName.getPath(), value);
            }
        });

        return 0;
    }

    static void applyDrugChange(CommandContext<ServerCommandSource> context, Identifier drugName, Consumer<Drug> change, FeedbackConsumer feedback) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
        DrugProperties properties = DrugProperties.of(player);

        if ("all".equals(drugName.getPath())) {
            DrugType.REGISTRY.forEach(type -> {
                change.accept(properties.getDrug(type));
            });
            feedback.accept(player, UpdateType.ALL);
        } else {
            DrugType.REGISTRY.getOrEmpty(drugName).map(properties::getDrug).ifPresentOrElse(drug -> {
                change.accept(drug);
                feedback.accept(player, UpdateType.ONE);
            }, () -> feedback.accept(player, UpdateType.NONE));
        }

        properties.markDirty();
    }

    interface FeedbackConsumer {
        void accept(ServerPlayerEntity player, UpdateType updateType);
    }

    enum UpdateType {
        ALL,
        ONE,
        NONE
    }

    private static void sendFeedback(ServerCommandSource source, ServerPlayerEntity player, boolean succeeded, String key, Object... arguments) {
        if (source.getEntity() == player) {
            source.sendFeedback(Text.translatable("commands.drug." + (succeeded ? "success" : "fail") + "." + key + ".self", arguments), true);
        } else {
            if (succeeded && source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendMessage(Text.translatable("commands.drug." + key + ".changed", arguments));
            }

            source.sendFeedback(Text.translatable("commands.drug." + (succeeded ? "success" : "fail") + "." + key + ".other", Streams.concat(
                    Stream.of(player.getDisplayName()),
                    Arrays.stream(arguments)).toArray()
            ), true);
        }
    }
}
