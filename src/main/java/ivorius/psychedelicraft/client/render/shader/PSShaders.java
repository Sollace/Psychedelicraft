package ivorius.psychedelicraft.client.render.shader;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public interface PSShaders {
    @Nullable
    Supplier<ShaderProgram> ZERO_MATTER = register("rendertype_zero_matter", VertexFormats.POSITION_COLOR);

    static void bootstrap() {}

    private static Supplier<ShaderProgram> register(String name, VertexFormat format) {
        AtomicReference<ShaderProgram> program = new AtomicReference<>(null);
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(Psychedelicraft.id(name), format, program::set);
        });
        return program::get;
    }
}
