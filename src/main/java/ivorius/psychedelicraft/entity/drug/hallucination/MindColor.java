package ivorius.psychedelicraft.entity.drug.hallucination;

import org.joml.Vector3f;

import ivorius.psychedelicraft.util.MathUtils;

public class MindColor {
    private final Vector3f prevValue = new Vector3f(1, 1, 1);
    private final Vector3f value = new Vector3f(1, 1, 1);
    private final Vector3f lerpedValue = new Vector3f(1, 1, 1);

    private final HallucinationManager manager;

    public MindColor(HallucinationManager manager) {
        this.manager = manager;
    }

    public void update() {
        prevValue.set(value);
        MathUtils.apply(value, component -> MathUtils.nearValue(component, MathUtils.randomColor(manager.getProperties().asEntity().getWorld().random, manager.getProperties().getAge(), 0.5f, 0.5f, 0.0012371f, 0.0017412f), 0.002f, 0.002f));
    }

    public Vector3f getColor(float tickDelta) {
        return MathUtils.lerp(tickDelta, prevValue, value, lerpedValue);
    }
}
