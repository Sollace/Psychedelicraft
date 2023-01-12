package ivorius.psychedelicraft.client;

import java.util.function.Supplier;

import ivorius.psychedelicraft.client.item.PSModelPredicates;
import ivorius.psychedelicraft.client.rendering.*;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.client.screen.PSScreens;
import ivorius.psychedelicraft.config.JsonConfig;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public class PsychedelicraftClient implements ClientModInitializer {
    private static final Supplier<JsonConfig.Loader<PSClientConfig>> CONFIG_LOADER = JsonConfig.create("psychedelicraft_client.json", PSClientConfig::new);

    public static PSClientConfig getConfig() {
        return CONFIG_LOADER.get().getData();
    }

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

        PSRenderers.bootstrap();
        PSModelPredicates.bootstrap();
        PSScreens.bootstrap();
    }
}
