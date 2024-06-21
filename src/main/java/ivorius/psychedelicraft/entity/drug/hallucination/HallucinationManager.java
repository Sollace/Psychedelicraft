package ivorius.psychedelicraft.entity.drug.hallucination;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import ivorius.psychedelicraft.client.render.RenderPhase;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 14.11.14.
 * Updated by Sollace on 14 Jan 2023
 */
public class HallucinationManager {
    private final Vector3f currentMindColor = new Vector3f(1, 1, 1);
    private final Vector4f pulseColor = new Vector4f();

    private final DrugProperties properties;

    private final Visualisations visualisations = new Visualisations();
    private final EntityHallucinationList entities = new EntityHallucinationList(this);
    private final DriftingCamera camera = new DriftingCamera();

    public HallucinationManager(DrugProperties properties) {
        this.properties = properties;
    }

    public void update() {
        entities.update();
        camera.update(properties);
        visualisations.update(properties);
        Random random = properties.asEntity().getRandom();
        MathUtils.apply(currentMindColor, component -> MathUtils.nearValue(component, MathUtils.randomColor(random, properties.getAge(), 0.5f, 0.5f, 0.0012371f, 0.0017412f), 0.002f, 0.002f));
    }

    public DrugProperties getProperties() {
        return properties;
    }

    public EntityHallucinationList getEntities() {
        return entities;
    }

    public Visualisations getVisualisations() {
        return visualisations;
    }

    public DriftingCamera getCamera() {
        return camera;
    }

    public float get(Attribute attribute) {
        return AttributeFunction.FUNCTIONS.get(attribute, this);
    }

    public float getEntityHallucinationStrength() {
        return visualisations.getMultiplier(HallucinationTypes.ENTITIES) * 0.4F;
    }

    public Vector4f getPulseColor() {
        return pulseColor.set(
                currentMindColor,
                RenderPhase.current() == RenderPhase.SKY ? 0F : MathHelper.clamp(visualisations.getMultiplier(HallucinationTypes.PULSES), 0, 1)
        );
    }

    public Vector4fc getColorBloom() {
        return MathUtils.mixColorsDynamic(
                currentMindColor,
                Drug.BLOOM.apply(properties),
                MathHelper.clamp(1.5f * visualisations.getMultiplier(HallucinationTypes.COLOR_BLOOM), 0, 1), false);
    }

    public Vector4fc getContrastColorization() {
        return MathUtils.mixColorsDynamic(
                currentMindColor,
                Drug.CONTRAST_COLORIZATION.apply(properties),
                MathHelper.clamp(visualisations.getMultiplier(HallucinationTypes.COLOR_CONTRAST), 0, 1),
                true
        );
    }

    public float[] getBlur() {
        float menuBlur = Math.max(0, ShaderContext.drug(DrugType.SLEEP_DEPRIVATION) - 0.7F) * ShaderContext.tickDelta() * 15;
        float vBlur = ShaderContext.drug(DrugType.POWER) + menuBlur;
        float hBlur = menuBlur + (
              ShaderContext.drug(DrugType.BATH_SALTS) * 6F
            + ShaderContext.drug(DrugType.BATH_SALTS) * (ShaderContext.ticks() % 5)
        );
        return new float[] { hBlur, vBlur };
    }
}
