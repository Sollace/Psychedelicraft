package ivorius.psychedelicraft.client;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.item.PSModelPredicates;
import net.fabricmc.api.ClientModInitializer;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public class PsychedelicraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Psychedelicraft.proxy = new ClientProxy();
        PSModelPredicates.bootstrap();
    }
}
