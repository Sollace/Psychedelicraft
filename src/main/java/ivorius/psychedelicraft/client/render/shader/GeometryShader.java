package ivorius.psychedelicraft.client.render.shader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.SodiumCompat;
import ivorius.psychedelicraft.client.render.RenderPhase;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.util.MathUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.gl.ShaderStage.Type;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;

public class GeometryShader implements IdentifiableResourceReloadListener {
    private static final String GEO_DIRECTORY = "shaders/geometry/";
    private static final Pattern PS_VARIABLE_PATTERN = Pattern.compile("(^|\\n)ps_([a-z]+ +[a-zA-Z0-9]+) +([^;]+);");
    private static final Identifier BASIC = Psychedelicraft.id("basic");
    private static final Identifier ID = Psychedelicraft.id("geometry_shaders");

    public static final GeometryShader INSTANCE = new GeometryShader();

    private Identifier name;
    private Type type;

    private final MinecraftClient client = MinecraftClient.getInstance();
    @Nullable
    private ResourceManager manager = client.getResourceManager();

    private final Map<Identifier, Optional<String>> loadedPrograms = new HashMap<>();

    private final Map<String, Supplier<Integer>> samplers = Util.make(new HashMap<>(), map -> {
        map.put("PS_SurfaceFractalSampler", () -> {
            @SuppressWarnings("deprecation")
            Identifier id = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
            if (client.player != null) {
                id = client.getBlockRenderManager().getModels().getModelParticleSprite(ShaderContext.hallucinations().getFractalAppearance()).getAtlasId();
            }

            return client.getTextureManager().getTexture(id).getGlId();
        });
    });

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager,
            Profiler prepareProfiler, Profiler applyProfiler,
            Executor prepareExecutor, Executor applyExecutor) {
        loadedPrograms.clear();
        this.manager = manager;
        return CompletableFuture.completedFuture(null);
    }

    public void setup(Type type, String domain, String name) {
        this.name = new Identifier(name);
        this.type = type;
    }

    public void setup(Type type, Identifier name) {
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
        addUniforms(builder, builder::addUniform);
        return builder;
    }

    public void addUniforms(ShaderProgramSetupView program, Consumer<GlUniform> register) {
        addUniforms(new UniformCollection() {
            @Override
            public void vec1(String name, FloatSupplier value) {
                register.accept(new BoundUniform(name, GlUniform.getTypeIndex("float"), 1, program, uniform -> uniform.set(value.getAsFloat())));
            }

            @Override
            public void vec3(String name, Supplier<Vector3f> value) {
                register.accept(new BoundUniform(name, GlUniform.getTypeIndex("float") + 2, 3, program, uniform -> uniform.set(value.get())));
            }

            @Override
            public void vec4(String name, Supplier<Vector4f> value) {
                register.accept(new BoundUniform(name, GlUniform.getTypeIndex("float") + 3, 4, program, uniform -> uniform.set(value.get())));
            }
        });
    }

    public void addUniforms(UniformCollection uniformHolder) {
        uniformHolder.vec1("PS_SurfaceFractalStrength", () -> isEnabled() ? MathHelper.clamp(ShaderContext.hallucinations().get(Drug.FRACTALS), 0, 1) : 0);
        uniformHolder.vec4("PS_Pulses", () -> isEnabled() ? ShaderContext.hallucinations().getPulseColor(ShaderContext.tickDelta(), RenderPhase.current() == RenderPhase.SKY) : MathUtils.ZERO);
        uniformHolder.vec4("PS_SurfaceFractalCoords", () -> {
            if (isEnabled() && ShaderContext.hallucinations().get(Drug.FRACTALS) > 0) {
                Sprite sprite = client.getBlockRenderManager().getModels().getModelParticleSprite(ShaderContext.hallucinations().getFractalAppearance());
                SodiumCompat.markSpriteActive(sprite);
                return new Vector4f(sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
            }
            return MathUtils.ZERO;
        });
        uniformHolder.vec3("PS_PlayerPosition", () -> ShaderContext.position().toVector3f());
        uniformHolder.vec1("PS_WorldTicks", () -> ShaderContext.ticks());
        uniformHolder.vec4("PS_WavesMatrix", () -> {
            if (isWorld() && RenderPhase.current() != RenderPhase.CLOUDS) {
                return new Vector4f(
                    ShaderContext.hallucinations().get(Drug.SMALL_WAVES),
                    ShaderContext.hallucinations().get(Drug.BIG_WAVES),
                    ShaderContext.hallucinations().get(Drug.WIGGLE_WAVES),
                    ShaderContext.hallucinations().get(Drug.BUBBLING_WAVES)
                );
            }
            return MathUtils.ZERO;
        });
        uniformHolder.vec1("PS_DistantWorldDeformation", () -> isWorld() ? ShaderContext.hallucinations().get(Drug.DISTANT_WAVES) : 0F);
        uniformHolder.vec1("PS_FractalFractureStrength", () -> isWorld() ? ShaderContext.hallucinations().get(Drug.SHATTERING_WAVES) : 0F);
        uniformHolder.vec1("PS_lsdBlendRatio", () -> isWorld() && RenderPhase.current() != RenderPhase.CLOUDS
                ? ShaderContext.modifier(Drug.RAINBOW_WAVES)
                : RenderPhase.current() == RenderPhase.SKY
                    ? ShaderContext.modifier(Drug.RAINBOW_WAVES) * 1.1F
                    : 0F);
    }

    public Map<String, Supplier<Integer>> getSamplers() {
        return samplers;
    }

    public String injectShaderSources(@Nullable String source) {
        if (source == null) {
            return null;
        }

        if (source.indexOf("PSYCHEDELICRAFT") != -1) {
            Psychedelicraft.LOGGER.info("Skipping already-processed shader " + name);
            return source;
        }

        if (source.indexOf("void main()") == -1) {
            return source;
        }

        if (type == Type.VERTEX) {
            return loadProgram(name.withPath(p -> GEO_DIRECTORY + p + ".gvsh")).or(() -> {
                return loadProgram(BASIC.withPath(p -> GEO_DIRECTORY + p + ".gvsh"));
            }).map(geometryShaderSources -> {
                return combineSources(source, geometryShaderSources);
            }).orElse(source);
        }

        if (type == Type.FRAGMENT) {
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

        if (name.getNamespace().equalsIgnoreCase("sodium")) {
            if (vertexSources.indexOf("out vec4 v_Color") != -1
                || vertexSources.indexOf("in vec4 v_Color") != -1) {
                geometrySources = geometrySources.replaceAll("vertexColor", "v_Color");
            }

            geometrySources = geometrySources.replaceAll("/\\*replaceme\\*/Position", "_vert_position");

            if (vertexSources.indexOf("out float v_FragDistance") != -1
                || vertexSources.indexOf("in float v_FragDistance") != -1) {
                geometrySources = geometrySources.replaceAll("vertexDistance", "v_FragDistance");
            }
        }
        if (name.getPath().startsWith("iris/")) {
            geometrySources = geometrySources.replaceAll("/\\*replaceme\\*/Position", "cameraPosition");
            if (vertexSources.indexOf("uniform vec3 cameraPosition") == -1) {
                geometrySources = "uniform vec3 cameraPosition;" + geometrySources;
            }
        }

        geometrySources = PS_VARIABLE_PATTERN.matcher(geometrySources).replaceAll(match -> {
            String fieldSlug = Arrays.stream(match.group(3).split(","))
                    .map(String::trim)
                    .filter(field -> !vertexSources.contains(field))
                    .collect(Collectors.joining(", "));
            return fieldSlug.isEmpty() ? "/* " + match.group(0) + "*/" : match.group(2) + " " + fieldSlug + ";";
        });
        String newline = System.lineSeparator();
        return writeSources(vertexSources.replace("void main()", "void i_parent_shaders_main()" + newline) + newline + "/*PSYCHEDELICRAFT START*/" + newline + geometrySources + newline + "/*PSYCHEDELICRAFT END*/", "merged");
    }

    private String writeSources(String sources, String suffex) {
        if (!PsychedelicraftClient.getConfig().exportShaderSources.get()) {
            return sources;
        }
        Path output = FabricLoader.getInstance().getGameDir().resolve("logs/shader_compilation/" + type.name().toLowerCase(Locale.ROOT) + "/" + name.getNamespace() + "/" + name.getPath() + "_" + suffex);
        try {
            Files.createDirectories(output.getParent());
            Files.deleteIfExists(output);
        } catch (IOException e) {
            Psychedelicraft.LOGGER.error("Could not remove stale shader sources file {} {}", output, e);
        }
        try (var writer = Files.newBufferedWriter(output, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            writer.append(sources);
            writer.flush();
        } catch (IOException e) {
            Psychedelicraft.LOGGER.error("Could not write shader sources to file {} {}", output, e);
        }
        return sources;
    }

    private Optional<String> loadProgram(Identifier id) {
        synchronized (this) {
            if (PsychedelicraftClient.getConfig().forceShaderRecompiles.get()) {
                return manager.getResource(id).map(res -> {
                    try (var stream = res.getInputStream()) {
                        return IOUtils.toString(stream, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        return null;
                    }
                });
            }
            Optional<String> source = loadedPrograms.getOrDefault(id, Optional.empty());
            if (source.isEmpty()) {
                source = manager.getResource(id).map(res -> {
                    try (var stream = res.getInputStream()) {
                        return IOUtils.toString(stream, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        return null;
                    }
                });
                loadedPrograms.put(id, source);
            }

            return source;
        }
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
