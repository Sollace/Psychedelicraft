package ivorius.psychedelicraft.client;

import java.util.function.Supplier;

import ivorius.psychedelicraft.client.item.PSModelPredicates;
import ivorius.psychedelicraft.client.render.*;
import ivorius.psychedelicraft.client.render.shader.ShaderLoader;
import ivorius.psychedelicraft.client.screen.PSScreens;
import ivorius.psychedelicraft.config.JsonConfig;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceType;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public class PsychedelicraftClient implements ClientModInitializer {
    private static final Supplier<JsonConfig.Loader<PSClientConfig>> CONFIG_LOADER = JsonConfig.create("psychedelicraft_client.json", PSClientConfig::new);

    public static PSClientConfig getConfig() {
        return CONFIG_LOADER.get().getData();
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!client.isPaused()) {
                DrugProperties.of((Entity)client.player).ifPresent(properties -> {
                    DrugRenderer.INSTANCE.update(properties, client.player);
                    SmoothCameraHelper.INSTANCE.tick(properties);
                });
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            DrugProperties.of((Entity)client.player).ifPresent(properties -> {
                DrugRenderer.INSTANCE.renderAllHallucinations(context.matrixStack(), context.consumers(), context.camera(), context.tickDelta(), properties);
            });
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(ShaderLoader.POST_EFFECTS);

        PSRenderers.bootstrap();
        PSModelPredicates.bootstrap();
        PSScreens.bootstrap();
    }
}
