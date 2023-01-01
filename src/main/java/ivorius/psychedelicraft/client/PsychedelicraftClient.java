package ivorius.psychedelicraft.client;

import ivorius.psychedelicraft.client.item.PSModelPredicates;
import ivorius.psychedelicraft.events.PSCoreHandlerClient;
import net.fabricmc.api.ClientModInitializer;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public class PsychedelicraftClient implements ClientModInitializer {


    public static PSCoreHandlerClient coreHandlerClient;


    @Override
    public void onInitializeClient() {
        new ClientProxy();
        coreHandlerClient = new PSCoreHandlerClient();
        coreHandlerClient.register();

        PSModelPredicates.bootstrap();
    }
}
