package ivorius.psychedelicraft.client.render.shader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.IOUtils;

import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.textures.GpuTexture;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.SodiumCompat;
import ivorius.psychedelicraft.client.render.RenderPhase;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.util.MathUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class GeometryShader {
    // Deprecation: SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE = Identifier.ofVanilla("textures/atlas/blocks.png");
    @SuppressWarnings("deprecation")
    private static final Identifier BLOCK_ATLAS_TEXTURE = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    private static final String GEO_DIRECTORY = "shaders/geometry/";
    private static final Pattern PS_VARIABLE_PATTERN = Pattern.compile("(?:^|\\n)ps_([a-z]+ +[a-zA-Z0-9]+) +([^;]+);");
    private static final Identifier BASIC = Psychedelicraft.id("basic");

    public static final GeometryShader INSTANCE = new GeometryShader();

    private Identifier name;
    private ShaderType type;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ResourceManager manager = client.getResourceManager();

    private final Map<Identifier, Optional<String>> loadedPrograms = new HashMap<>();

    private final Map<String, Supplier<GpuTexture>> samplers = Util.make(new HashMap<>(), map -> {
        map.put("PS_SurfaceFractalSampler", () -> {
            @SuppressWarnings("deprecation")
            Identifier id = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
            if (client.player != null) {
                id = client.getBlockRenderManager().getModels().getModelParticleSprite(ShaderContext.hallucinations().getFractalAppearance()).getAtlasId();
            }

            return client.getTextureManager().getTexture(id).getGlTexture();
        });
    });

    public void setup(ShaderType type, Identifier name) {
        this.name = name;
        this.type = type;
    }

    public boolean isEnabled() {
        return RenderPhase.current() != RenderPhase.NORMAL && client.world != null && client.player != null;
    }

    public boolean isWorld() {
        return (RenderPhase.current() == RenderPhase.WORLD || RenderPhase.current() == RenderPhase.CLOUDS) && client.world != null && client.player != null;
    }

    public BuiltGemoetryShader.Builder createShaderBuilder(int program, int lastUniformId, int lastSamplerId) {
        var builder = new BuiltGemoetryShader.Builder(program, lastUniformId, lastSamplerId);
        samplers.forEach(builder::addSampler);
        addUniforms(builder::addUniform);
        return builder;
    }

    public void addUniforms(Consumer<BoundUniform> register) {
        register.accept(new BoundUniform("PS_SurfaceFractalStrength", UniformType.FLOAT, uniform -> {
            uniform.set(isEnabled() ? MathHelper.clamp(ShaderContext.hallucinations().get(Drug.FRACTALS), 0, 1) : 0);
        }));
        register.accept(new BoundUniform("PS_Pulses", UniformType.VEC4, uniform -> {
            uniform.set(isEnabled() ? ShaderContext.hallucinations().getPulseColor(ShaderContext.tickDelta(), RenderPhase.current() == RenderPhase.SKY) : MathUtils.ZERO);
        }));
        register.accept(new BoundUniform("PS_SurfaceFractalCoords", UniformType.VEC4, uniform -> {
            if (isEnabled() && ShaderContext.hallucinations().get(Drug.FRACTALS) > 0) {
                Sprite sprite = client.getBlockRenderManager().getModels().getModelParticleSprite(ShaderContext.hallucinations().getFractalAppearance());
                SodiumCompat.markSpriteActive(sprite);
                uniform.set(new float[] {sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV()});
            } else {
                uniform.set(MathUtils.ZERO);
            }
        }));
        register.accept(new BoundUniform("PS_PlayerPosition", UniformType.VEC3, uniform -> uniform.set(ShaderContext.position().toVector3f())));
        register.accept(new BoundUniform("PS_WorldTicks", UniformType.FLOAT, uniform -> uniform.set(ShaderContext.ticks())));
        register.accept(new BoundUniform("PS_WavesMatrix", UniformType.VEC4, uniform -> {
            if (isWorld() && RenderPhase.current() != RenderPhase.CLOUDS) {
                uniform.set(new float[] {
                    ShaderContext.hallucinations().get(Drug.SMALL_WAVES),
                    ShaderContext.hallucinations().get(Drug.BIG_WAVES),
                    ShaderContext.hallucinations().get(Drug.WIGGLE_WAVES),
                    ShaderContext.hallucinations().get(Drug.BUBBLING_WAVES)
                });
            } else {
                uniform.set(MathUtils.ZERO);
            }
        }));
        register.accept(new BoundUniform("PS_DistantWorldDeformation", UniformType.FLOAT, uniform -> {
            uniform.set(isWorld() ? ShaderContext.hallucinations().get(Drug.DISTANT_WAVES) : 0F);
        }));
        register.accept(new BoundUniform("PS_FractalFractureStrength", UniformType.FLOAT, uniform -> {
            uniform.set(isWorld() ? ShaderContext.hallucinations().get(Drug.SHATTERING_WAVES) : 0F);
        }));
        register.accept(new BoundUniform("PS_lsdBlendRatio", UniformType.FLOAT, uniform -> {
            uniform.set(isWorld() && RenderPhase.current() != RenderPhase.CLOUDS
                    ? ShaderContext.modifier(Drug.RAINBOW_WAVES)
                    : RenderPhase.current() == RenderPhase.SKY ? ShaderContext.modifier(Drug.RAINBOW_WAVES) * 1.1F
                            : 0F);
        }));
    }

    public Map<String, Supplier<GpuTexture>> getSamplers() {
        return samplers;
    }

    public List<String> injectShaderSources(List<String> source) {
        String joined = String.join("%PS_DELIM%", source);
        String converted = injectShaderSources(joined);
        if (converted.equals(joined)) {
            return source;
        }
        return List.of(converted.split("%PS_DELIM%"));
    }

    public String injectShaderSources(String source) {
        if (source.indexOf("PSYCHEDELICRAFT") != -1) {
            Psychedelicraft.LOGGER.info("Skipping already-processed shader " + name);
            return source;
        }

        if (source.indexOf("void main()") == -1) {
            return source;
        }

        if (type == ShaderType.VERTEX) {
            return loadProgram(name.withPath(p -> GEO_DIRECTORY + p + ".gvsh")).or(() -> {
                return loadProgram(BASIC.withPath(p -> GEO_DIRECTORY + p + ".gvsh"));
            }).map(geometryShaderSources -> {
                return combineSources(source, geometryShaderSources);
            }).orElse(source);
        }

        if (type == ShaderType.FRAGMENT) {
            return loadProgram(name.withPath(p -> p + ".gfsh")).or(() -> {
                return loadProgram(BASIC.withPath(p -> GEO_DIRECTORY + p + ".gfsh"));
            }).map(geometryShaderSources -> {
                return combineSources(source, geometryShaderSources);
            }).orElse(source);
        }

        Psychedelicraft.LOGGER.info("Skipping unknown shader " + name);
        return source;
    }

    private String combineSources(String vertexSources, String geometrySources) {
        writeSources(vertexSources, "before");

        if (vertexSources.indexOf("out vec4 v_Color") != -1
            || vertexSources.indexOf("in vec4 v_Color") != -1) {
            geometrySources = geometrySources.replaceAll("vertexColor", "v_Color");
            geometrySources = geometrySources.replaceAll("/\\*replaceme\\*/Position", "_vert_position");
        }
        if (vertexSources.indexOf("out float v_FragDistance") != -1
            || vertexSources.indexOf("in float v_FragDistance") != -1) {
            geometrySources = geometrySources.replaceAll("vertexDistance", "v_FragDistance");
        }

        geometrySources = PS_VARIABLE_PATTERN.matcher(geometrySources).replaceAll(match -> {
            String fieldSlug = Arrays.stream(match.group(2).split(","))
                    .map(String::trim)
                    .filter(field -> !vertexSources.contains(match.group(1) + " " + field))
                    .collect(Collectors.joining(", "));
            return fieldSlug.isEmpty() ? "/* " + match.group(0) + "*/" : match.group(1) + " " + fieldSlug + ";";
        });
        String newline = System.lineSeparator();
        return writeSources(vertexSources.replace("void main()", "void i_parent_shaders_main()" + newline) + newline + "/*PSYCHEDELICRAFT START*/" + newline + geometrySources + newline + "/*PSYCHEDELICRAFT END*/", "merged");
    }

    private String writeSources(String sources, String suffex) {
        Path output = FabricLoader.getInstance().getGameDir().resolve("logs/shader_compilation/" + type.name().toLowerCase(Locale.ROOT) + "/" + name.getNamespace() + "/" + name.getPath() + "_" + suffex);
        try {
            Files.createDirectories(output.getParent());
            Files.deleteIfExists(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (var writer = Files.newBufferedWriter(output, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            writer.append(sources);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sources;
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

    public static class BoundUniform extends GlUniform {
        private final Consumer<GlUniform> valueGetter;

        public BoundUniform(String name, UniformType type, Consumer<GlUniform> valueGetter) {
            super(name, type);
            this.valueGetter = valueGetter;
        }

        @Override
        public void upload() {
            valueGetter.accept(this);
            super.upload();
        }
    }
}
