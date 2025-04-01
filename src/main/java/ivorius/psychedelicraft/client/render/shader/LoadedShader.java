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
    private final UniformBinding.Set bindings;

    private final Identifier id;
    private final MinecraftClient client;

    private final UpdateTracker updater = new UpdateTracker();
    private final UniformValues uniformValues = new UniformValues();

    public LoadedShader(MinecraftClient client, Identifier id, UniformBinding.Set bindings) throws IOException, JsonSyntaxException {
        this.client = client;
        this.id = id;
        this.bindings = bindings;
    }

    @SuppressWarnings("deprecation")
    public void render(Pool pool, float tickDelta) {
        try {
            PostEffectProcessor processor = client.getShaderLoader().loadPostEffect(id, DefaultFramebufferSet.MAIN_ONLY);
            if (processor == null) {
                return;
            }

            if (updater.update(processor, tickDelta)) {
                processor.render(client.getFramebuffer(), pool);
            }
        } catch (Throwable t) {
            Psychedelicraft.LOGGER.error("Exception applying shader pass: {}", t);
        }
    }

    class UpdateTracker {
        private int updateCount;

        //private long processorHash;
        private boolean enabled;

        public boolean update(PostEffectProcessor processor, float tickDelta) {
            //if (updateCount == 0 || processor.hashCode() != processorHash) {
                //processorHash = processor.hashCode();
                enabled = false;
                uniformValues.update(processor, tickDelta, () -> enabled = true);
            //}

            updateCount = (updateCount + 1) % 2;

            return enabled;
        }
    }


    class UniformValues implements UniformSetter {
        private final Map<String, Supplier<List<Float>>> uniformValues = new HashMap<>();
        private final Map<String, Set<String>> passUniforms = new HashMap<>();
        private final Set<String> globalUniforms = new HashSet<>();

        private Set<String> currentPassUniforms = globalUniforms;

        public void update(PostEffectProcessor processor, float tickDelta, Runnable enableShader) {
            uniformValues.clear();
            globalUniforms.clear();
            passUniforms.clear();
            currentPassUniforms = globalUniforms;

            final int width = client.getWindow().getFramebufferWidth();
            final int height = client.getWindow().getFramebufferHeight();

            bindings.global.bindUniforms(this, tickDelta, width, height, () -> {
                for (var pass : ((PostEffectPassSupplier)processor).getPasses()) {
                    String passId = pass.getId();
                    var programBindings = bindings.programBindings.getOrDefault(passId, UniformBinding.EMPTY);
                    if (programBindings != UniformBinding.EMPTY) {
                        pass.setDisabled();
                        currentPassUniforms = passUniforms.computeIfAbsent(passId, i -> new HashSet<>(globalUniforms));
                        programBindings.bindUniforms(this, tickDelta, width, height, () -> {
                            enableShader.run();
                            pass.setUniformUpdater(program -> {
                                List<PostEffectPipeline.Uniform> uniforms = new ArrayList<>();
                                for (String uniformName : currentPassUniforms) {
                                    uniforms.add(new PostEffectPipeline.Uniform(uniformName, uniformValues.get(uniformName).get()));
                                }
                                return uniforms;
                            });
                        });
                    }
                }
            });
        }

        @Override
        public void set(String name, Supplier<List<Float>> setter) {
            currentPassUniforms.add(name);
            uniformValues.put(name, setter);
        }
    }
}