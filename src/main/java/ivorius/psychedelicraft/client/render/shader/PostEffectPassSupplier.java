package ivorius.psychedelicraft.client.render.shader;

import java.util.List;

import net.minecraft.client.gl.PostEffectPass;

public interface PostEffectPassSupplier {
    List<PostEffectPass> getPasses();

    void setPasses(List<PostEffectPass> passes);

    interface Pass {
        String getId();
    }
}
