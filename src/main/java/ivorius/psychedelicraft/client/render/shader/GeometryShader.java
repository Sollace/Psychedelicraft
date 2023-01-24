package ivorius.psychedelicraft.client.render.shader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.gl.ShaderStage.Type;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class GeometryShader {
    private static final String GEO_DIRECTORY = "shaders/geometry/";
    private static final Identifier BASIC = Psychedelicraft.id("basic");

    public static final GeometryShader INSTANCE = new GeometryShader();

    private String name;
    private Type type;

    private final ResourceManager manager = MinecraftClient.getInstance().getResourceManager();

    private final Map<Identifier, Optional<String>> loadedPrograms = new HashMap<>();

    public void setup(Type type, String name, InputStream stream, String domain, GLImportProcessor loader) {
        this.name = name;
        this.type = type;
    }

    public void addUniforms(ShaderProgramSetupView program, Consumer<GlUniform> register) {
        register.accept(new BoundUniform("PS_PlayerPosition", GlUniform.getTypeIndex("float") + 2, 3, program, uniform -> {
            Vec3d pos = MinecraftClient.getInstance().player == null ? Vec3d.ZERO : MinecraftClient.getInstance().player.getPos();
            uniform.set((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
        }));
        register.accept(new BoundUniform("PS_WorldTicks", GlUniform.getTypeIndex("float"), 1, program, uniform -> {
            uniform.set(MinecraftClient.getInstance().world == null ? 0 : MinecraftClient.getInstance().player.age + MinecraftClient.getInstance().getTickDelta());
        }));
        register.accept(new BoundUniform("PS_WavesMatrix", GlUniform.getTypeIndex("float") + 2, 3, program, uniform -> {
            if (MinecraftClient.getInstance().world == null) {
                uniform.set(0F, 0F, 0F);
            } else {
                float tickDelta = MinecraftClient.getInstance().getTickDelta();
                uniform.set(
                    ShaderContext.hallucinations().getSmallWaveStrength(tickDelta),
                    ShaderContext.hallucinations().getBigWaveStrength(tickDelta),
                    ShaderContext.hallucinations().getWiggleWaveStrength(tickDelta)
                );
            }
        }));
        register.accept(new BoundUniform("PS_DistantWorldDeformation", GlUniform.getTypeIndex("float"), 1, program, uniform -> {
            uniform.set(MinecraftClient.getInstance().world == null ? 0 : ShaderContext.hallucinations().getDistantWorldDeformationStrength(MinecraftClient.getInstance().getTickDelta()));
        }));
    }

    public String injectShaderSources(String source) {
        if (type == Type.VERTEX) {
            return loadProgram(new Identifier(GEO_DIRECTORY + name + ".gsh")).or(() -> {
                return loadProgram(BASIC.withPath(p -> GEO_DIRECTORY + p + ".gsh"));
            }).map(geometryShaderSources -> {
                return combineSources(source, geometryShaderSources);
            }).orElse(source);
        }
        return source;
    }

    private String combineSources(String vertexSources, String geometrySources) {
        String newline = System.lineSeparator();
        String result = vertexSources.replace("void main()", "void __vertex_shaders__main()" + newline) + newline + geometrySources;
        System.out.println(result);
        return result;
    }

    private Optional<String> loadProgram(Identifier id) {
        loadedPrograms.clear();
        return loadedPrograms.computeIfAbsent(id, i -> {
            return manager.getResource(i).map(res -> {
                try (var stream = res.getInputStream()) {
                    return IOUtils.toString(stream, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    return null;
                }
            });
        });
    }

    static class BoundUniform extends GlUniform {
        private final Consumer<GlUniform> valueGetter;

        public BoundUniform(String name, int dataType, int count, ShaderProgramSetupView program, Consumer<GlUniform> valueGetter) {
            super(name, dataType, count, program);
            this.valueGetter = valueGetter;
        }

        @Override
        public void upload() {
            valueGetter.accept(this);
            super.upload();
        }
    }
}
