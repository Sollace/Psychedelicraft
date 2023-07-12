package ivorius.psychedelicraft.client.render.shader;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;

public class PSShaders {
    @Nullable
    private static Shader renderTypeZeroMatterProgram;

    public static Shader getRenderTypeZeroMatterProgram() {
        return renderTypeZeroMatterProgram;
    }

    public static void bootstrap() {
        CoreShaderRegistrationCallback.EVENT.register((manager, shaderList) -> {
            shaderList.add(Pair.of(new Shader(new ModdedResourceFactory(manager, "psychedelicraft"), "rendertype_zero_matter", VertexFormats.POSITION_COLOR), program -> {
                renderTypeZeroMatterProgram = program;
            }));
        });
    }
}
