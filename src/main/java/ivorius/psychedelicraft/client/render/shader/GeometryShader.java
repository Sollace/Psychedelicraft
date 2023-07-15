package ivorius.psychedelicraft.client.render.shader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.RenderPhase;
import ivorius.psychedelicraft.util.Compat119;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.gl.Program.Type;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.*;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GeometryShader {
    private static final String GEO_DIRECTORY = "shaders/geometry/";
    private static final Pattern PS_VARIABLE_PATTERN = Pattern.compile("(^|\\n)ps_([a-z]+ +[a-zA-Z0-9]+) +([^;]+);");
    private static final Identifier BASIC = Psychedelicraft.id("basic");

    public static final GeometryShader INSTANCE = new GeometryShader();

    private Identifier name;
    private Type type;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ResourceManager manager = client.getResourceManager();

    private final Map<Identifier, Optional<String>> loadedPrograms = new HashMap<>();


    private final Map<String, Supplier<Object>> samplers = Util.make(new HashMap<>(), map -> {
        map.put("PS_DepthSampler", () -> MinecraftClient.getInstance().getFramebuffer().getDepthAttachment());
        map.put("PS_SurfaceFractalSampler", () -> MinecraftClient.getInstance().getTextureManager().getTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE));
    });

    public void setup(Type type, String name, InputStream stream, String domain, GLImportProcessor loader) {
        this.name = new Identifier(name);
        this.type = type;
    }

    public boolean isEnabled() {
        return RenderPhase.current() != RenderPhase.NORMAL && client.world != null && client.player != null;
    }

    public boolean isWorld() {
        return (RenderPhase.current() == RenderPhase.WORLD || RenderPhase.current() == RenderPhase.CLOUDS) && client.world != null && client.player != null;
    }

    public void addUniforms(GlShader program, Consumer<GlUniform> register) {
        register.accept(new BoundUniform("PS_SurfaceFractalStrength", GlUniform.getTypeIndex("float"), 1, program, uniform -> {
            uniform.set(isEnabled() ? MathHelper.clamp(ShaderContext.hallucinations().getSurfaceFractalStrength(ShaderContext.tickDelta()), 0, 1) : 0);
        }));
        register.accept(new BoundUniform("PS_Pulses", GlUniform.getTypeIndex("float") + 3, 4, program, uniform -> {
            uniform.set(isEnabled() ? ShaderContext.hallucinations().getPulseColor(ShaderContext.tickDelta()) : new float[4]);
        }));
        register.accept(new BoundUniform("PS_SurfaceFractalCoords", GlUniform.getTypeIndex("float") + 3, 4, program, uniform -> {
            if (isEnabled()) {
                Sprite sprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
                uniform.set(sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
            }
        }));
        register.accept(new BoundUniform("PS_PlayerPosition", GlUniform.getTypeIndex("float") + 2, 3, program, uniform -> {
            Vec3d pos = isEnabled() ? MinecraftClient.getInstance().player.getPos() : Vec3d.ZERO;
            uniform.set((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
        }));
        register.accept(new BoundUniform("PS_WorldTicks", GlUniform.getTypeIndex("float"), 1, program, uniform -> {
            uniform.set(isEnabled() ? ShaderContext.ticks() : 0);
        }));
        register.accept(new BoundUniform("PS_WavesMatrix", GlUniform.getTypeIndex("float") + 2, 3, program, uniform -> {
            if (isWorld()) {
                float tickDelta = ShaderContext.tickDelta();
                uniform.set(
                    ShaderContext.hallucinations().getSmallWaveStrength(tickDelta),
                    ShaderContext.hallucinations().getBigWaveStrength(tickDelta),
                    ShaderContext.hallucinations().getWiggleWaveStrength(tickDelta)
                );
            } else {
                uniform.set(0F, 0F, 0F);
            }
        }));
        register.accept(new BoundUniform("PS_DistantWorldDeformation", GlUniform.getTypeIndex("float"), 1, program, uniform -> {
            uniform.set(isWorld() ? ShaderContext.hallucinations().getDistantWorldDeformationStrength(MinecraftClient.getInstance().getTickDelta()) : 0);
        }));
        register.accept(new BoundUniform("PS_FractalFractureStrength", GlUniform.getTypeIndex("float"), 1, program, uniform -> {
            uniform.set(isWorld() ? ShaderContext.hallucinations().getSurfaceShatteringStrength(MinecraftClient.getInstance().getTickDelta()) : 0F);
        }));
    }

    public Map<String, Supplier<Object>> getSamplers() {
        return samplers;
    }

    public String injectShaderSources(String source) {
        if (type == Type.VERTEX) {
            return loadProgram(Compat119.withPath(name, p -> GEO_DIRECTORY + p + ".gvsh")).or(() -> {
                return loadProgram(Compat119.withPath(BASIC, p -> GEO_DIRECTORY + p + ".gvsh"));
            }).map(geometryShaderSources -> {
                return combineSources(source, geometryShaderSources);
            }).orElse(source);
        }

        if (type == Type.FRAGMENT) {
            return loadProgram(Compat119.withPath(name, p -> name + ".gfsh")).or(() -> {
                return loadProgram(Compat119.withPath(BASIC, p -> GEO_DIRECTORY + p + ".gfsh"));
            }).map(geometryShaderSources -> {
                return combineSources(source, geometryShaderSources);
            }).orElse(source);
        }

        return source;
    }

    private String combineSources(String vertexSources, String geometrySources) {
        geometrySources = PS_VARIABLE_PATTERN.matcher(geometrySources).replaceAll(match -> {
            String fieldSlug = Arrays.stream(match.group(3).split(","))
                    .map(String::trim)
                    .filter(field -> !vertexSources.contains(field))
                    .collect(Collectors.joining(", "));
            return fieldSlug.isEmpty() ? "/* " + match.group(0) + "*/" : match.group(2) + " " + fieldSlug + ";";
        });

        String newline = System.lineSeparator();
        return vertexSources.replace("void main()", "void i_parent_shaders_main()" + newline) + newline + geometrySources;
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

        public BoundUniform(String name, int dataType, int count, GlShader program, Consumer<GlUniform> valueGetter) {
            super(name, dataType, count, program);
            this.valueGetter = valueGetter;
        }

        @Override
        public void upload() {
            if (!MinecraftClient.getInstance().isPaused()) {
                valueGetter.accept(this);
            }
            super.upload();
        }
    }
}
