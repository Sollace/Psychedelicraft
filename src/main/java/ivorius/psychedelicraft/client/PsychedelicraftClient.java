package ivorius.psychedelicraft.client;

import ivorius.psychedelicraft.client.item.PSModelPredicates;
import ivorius.psychedelicraft.client.rendering.DrugEffectInterpreter;
import ivorius.psychedelicraft.client.rendering.SmoothCameraHelper;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public class PsychedelicraftClient implements ClientModInitializer {
    public PsychedelicraftClient() {
        new ClientProxy();
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            PSRenderStates.update();

            if (!client.isPaused()) {
                DrugProperties.of(client.cameraEntity).ifPresent(drugProperties -> {
                    SmoothCameraHelper.INSTANCE.update(DrugEffectInterpreter.getSmoothVision(drugProperties));
                });
            }
        });

        PSModelPredicates.bootstrap();
    }
}
