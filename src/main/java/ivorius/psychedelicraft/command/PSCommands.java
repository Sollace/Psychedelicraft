package ivorius.psychedelicraft.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSCommands {
    static void bootstrap() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            DrugCommand.register(dispatcher, access);
            HallucinateCommand.register(dispatcher, access);
            VomitCommand.register(dispatcher, access);
        });
    }
}
