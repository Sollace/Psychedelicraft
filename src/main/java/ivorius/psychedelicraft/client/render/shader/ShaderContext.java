package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public interface ShaderContext {
    static HallucinationManager hallucinations() {
        return DrugProperties.of(MinecraftClient.getInstance().player).getHallucinations();
    }

    static DrugProperties properties() {
        return DrugProperties.of(MinecraftClient.getInstance().player);
    }

    static float drug(DrugType type) {
        return properties().getDrugValue(type);
    }

    static float modifier(Drug.AggregateModifier type) {
        return type.get(properties());
    }

    static float ticks() {
        return MinecraftClient.getInstance().player.age + tickDelta();
    }

    static float tickDelta() {
        return MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
    }

    static long time() {
        return MinecraftClient.getInstance().world.getTime();
    }

    static float viewDistace() {
        return MinecraftClient.getInstance().options.getViewDistance().getValue() * 16;
    }

    static Vec3d position() {
        return MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
    }
}
