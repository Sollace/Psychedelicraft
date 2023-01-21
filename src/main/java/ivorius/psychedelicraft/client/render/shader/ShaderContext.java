package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import net.minecraft.client.MinecraftClient;

interface ShaderContext {
    static HallucinationManager hallucinations() {
        return DrugProperties.of(MinecraftClient.getInstance().player).getHallucinations();
    }

    static DrugProperties properties() {
        return DrugProperties.of(MinecraftClient.getInstance().player);
    }

    static float drug(DrugType type) {
        return properties().getDrugValue(type);
    }
}
