package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public interface PSShaders {
    ShaderProgramKey ZERO_MATTER = register("rendertype_zero_matter", VertexFormats.POSITION_COLOR);

    static void bootstrap() {}

    private static ShaderProgramKey register(String name, VertexFormat format) {
        ShaderProgramKey key = new ShaderProgramKey(Psychedelicraft.id("core/" + name), format, Defines.EMPTY);
        ShaderProgramKeys.getAll().add(key);
        return key;
    }
}
