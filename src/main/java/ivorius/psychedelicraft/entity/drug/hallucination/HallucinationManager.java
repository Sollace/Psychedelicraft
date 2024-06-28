package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.List;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 14.11.14.
 * Updated by Sollace on 14 Jan 2023
 */
public class HallucinationManager {
    private final DrugProperties properties;

    private final Visualisations visualisations = new Visualisations();
    private final EntityHallucinationList entities = new EntityHallucinationList(this);
    private final DriftingCamera camera = new DriftingCamera();

    private final MindColor surfaceColor = new MindColor(this);
    private final MindColor bloomColor = new MindColor(this);
    private final MindColor pulseColor = new MindColor(this);
    private final MindColor skyColor = new MindColor(this);

    private final List<Block> fractalTypes = List.of(
            Blocks.NETHER_PORTAL,
            Blocks.AMETHYST_BLOCK,
            Blocks.BRICKS,
            Blocks.CHISELED_RED_SANDSTONE
    );
    private int fractalType = 0;

    public HallucinationManager(DrugProperties properties) {
        this.properties = properties;
    }

    public void update() {
        entities.update();
        camera.update(properties);
        visualisations.update(properties);
        surfaceColor.update();
        bloomColor.update();
        pulseColor.update();

        if (get(Drug.FRACTALS) > 0) {
            Random random = properties.asEntity().getWorld().random;
            if (random.nextInt(20) == 0) {
                fractalType = random.nextInt(fractalTypes.size());
            }
        } else {
            fractalType = 0;
        }
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

    public BlockState getFractalAppearance() {
        return fractalTypes.get(fractalType).getDefaultState();
    }

    public Vector4f getPulseColor(float tickDelta, boolean sky) {
        float alpha = MathHelper.clamp(visualisations.getMultiplier(HallucinationTypes.PULSES), 0, 1);
        if (alpha == 0) {
            return MathUtils.ZERO;
        }
        return MathUtils.TEMP_VECTOR.set(pulseColor.getColor(tickDelta), alpha);
    }

    public Vector4fc getColorBloom(float tickDelta, boolean sky) {
        return MathUtils.mixColorsDynamic(
                (sky ? skyColor : bloomColor).getColor(tickDelta),
                Drug.BLOOM.apply(properties),
                MathHelper.clamp(1.5f * visualisations.getMultiplier(HallucinationTypes.COLOR_BLOOM), 0, 1)
        );
    }

    public Vector4fc getContrastColorization(float tickDelta) {
        return MathUtils.mixColorsDynamic(
                surfaceColor.getColor(tickDelta),
                Drug.CONTRAST_COLORIZATION.apply(properties),
                MathHelper.clamp(visualisations.getMultiplier(HallucinationTypes.COLOR_CONTRAST), 0, 0.8F)
        );
    }

    public float[] getBlur() {
        float menuBlur = Math.max(0, properties.getDrugValue(DrugType.SLEEP_DEPRIVATION) - 0.7F) * ShaderContext.tickDelta() * 15;
        float vBlur = ShaderContext.drug(DrugType.POWER) + menuBlur;
        float hBlur = menuBlur + (
              ShaderContext.drug(DrugType.BATH_SALTS) * 6F
            + ShaderContext.drug(DrugType.BATH_SALTS) * (ShaderContext.ticks() % 5)
        );
        return new float[] { hBlur, vBlur };
    }
}
