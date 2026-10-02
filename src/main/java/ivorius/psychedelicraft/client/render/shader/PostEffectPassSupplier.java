package ivorius.psychedelicraft.client.render.shader;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface PostEffectPassSupplier {
    List<Pass> getPasses();

    interface Pass {
        String getId();

        void setDisabled();

        void setUniformUpdater(Supplier<Map<String, float[]>> updater);
    }
}
