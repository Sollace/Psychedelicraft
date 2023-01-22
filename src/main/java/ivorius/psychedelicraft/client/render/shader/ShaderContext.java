package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import net.minecraft.client.MinecraftClient;

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
        return properties().getModifier(type);
    }

    static float ticks() {
        return MinecraftClient.getInstance().player.age + MinecraftClient.getInstance().getTickDelta();
    }

    static long time() {
        return MinecraftClient.getInstance().world.getTime();
    }

    static float viewDistace() {
        return MinecraftClient.getInstance().options.getViewDistance().getValue() * 16;
    }
}
