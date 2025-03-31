package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import com.google.gson.JsonSyntaxException;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.UniformBinding.UniformSetter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.util.Pool;
import net.minecraft.util.Identifier;

class LoadedShader {
    private final Identifier id;
    private final MinecraftClient client;

    private final UniformValues uniformValues;

    //private long lastUpdateTime;

    private boolean shouldRender;

    public LoadedShader(MinecraftClient client, Identifier id, UniformBinding.Set bindings) throws IOException, JsonSyntaxException {
        this.client = client;
        this.id = id;
        this.uniformValues = new UniformValues(bindings);
    }

    @SuppressWarnings("deprecation")
    public void render(Pool pool, float tickDelta) {
        try {
            PostEffectProcessor processor = client.getShaderLoader().loadPostEffect(id, DefaultFramebufferSet.MAIN_ONLY);
            if (processor == null) {
                return;
            }

            if (updateUniforms(processor, tickDelta)) {
                processor.render(client.getFramebuffer(), pool, null);
            }
        } catch (Throwable t) {
            Psychedelicraft.LOGGER.error(t.getMessage());
        }
    }

    private boolean updateUniforms(PostEffectProcessor processor, float tickDelta) {
        //long now = System.currentTimeMillis();

        //if (now > lastUpdateTime - 100) {
        //    lastUpdateTime = now;
            shouldRender = false;
            uniformValues.update(client, processor, tickDelta, () -> shouldRender = true);
        //}

        return shouldRender;
    }

    private static class UniformValues implements UniformSetter {
        private final UniformBinding.Set bindings;

        private final Map<String, Supplier<List<Float>>> uniformValues = new HashMap<>();
        private final Map<String, String> uniformTypes = new HashMap<>();
        private final Map<String, Set<String>> passUniforms = new HashMap<>();
        private final Set<String> globalUniforms = new HashSet<>();

        private Set<String> currentPassUniforms = globalUniforms;

        public UniformValues(UniformBinding.Set bindings) {
            this.bindings = bindings;
        }

        public void update(MinecraftClient client, PostEffectProcessor processor, float tickDelta, Runnable enableShader) {
            uniformValues.clear();
            globalUniforms.clear();
            passUniforms.clear();
            currentPassUniforms = globalUniforms;

            final int width = client.getWindow().getFramebufferWidth();
            final int height = client.getWindow().getFramebufferHeight();

            bindings.global.bindUniforms(this, tickDelta, width, height, () -> {
                for (var pass : ((PostEffectPassSupplier)processor).getPasses()) {
                    String passId = pass.getId();
                    var programBindings = bindings.programBindings.getOrDefault(Identifier.of(passId).withPrefixedPath("post/"), UniformBinding.EMPTY);
                    if (programBindings != UniformBinding.EMPTY) {
                        pass.setDisabled();
                        currentPassUniforms = passUniforms.computeIfAbsent(passId, i -> new HashSet<>(globalUniforms));
                        programBindings.bindUniforms(this, tickDelta, width, height, () -> {
                            enableShader.run();
                            pass.setUniformUpdater(pipeline -> {
                                List<PostEffectPipeline.Uniform> uniforms = new ArrayList<>();
                                for (String uniformName : currentPassUniforms) {
                                    uniforms.add(new PostEffectPipeline.Uniform(uniformName, uniformTypes.get(uniformName), Optional.of(uniformValues.get(uniformName).get())));
                                }
                                return uniforms;
                            });
                        });
                    }
                }
            });
        }

        @Override
        public void set(String name, String type, Supplier<List<Float>> setter) {
            currentPassUniforms.add(name);
            uniformValues.put(name, setter);
            uniformTypes.put(name, type);
        }
    }
}
