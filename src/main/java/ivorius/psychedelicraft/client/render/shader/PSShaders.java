package ivorius.psychedelicraft.client.render.shader;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;

public class PSShaders {
    @Nullable
    private static ShaderProgram renderTypeZeroMatterProgram;

    public static ShaderProgram getRenderTypeZeroMatterProgram() {
        return renderTypeZeroMatterProgram;
    }

    public static void bootstrap() {
        CoreShaderRegistrationCallback.EVENT.register((manager, shaderList) -> {
            shaderList.add(Pair.of(new ShaderProgram(new ModdedResourceManager((ResourceManager)manager, "psychedelicraft"), "rendertype_zero_matter", VertexFormats.POSITION_COLOR), program -> {
                renderTypeZeroMatterProgram = program;
            }));
        });
    }
}
