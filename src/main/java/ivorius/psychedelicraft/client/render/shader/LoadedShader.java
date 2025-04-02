package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
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

    private final UniformBinding.Set bindings;

    private final List<Pass> passes = new ArrayList<>();
    private int passCount = 0;

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

            passCount = 0;
            passes.clear();

            Map<String, Pass> passById = new HashMap<>();
            update(client, processor, tickDelta, (id, callback) -> {
                passCount = Math.max(passCount, passById.computeIfAbsent(id, this::addPass).add(callback));
            });

            for (int i = 0; i < passCount; i++) {
                for (Pass pass : passes) {
                    pass.replay(i);
                }
                processor.render(client.getFramebuffer(), pool, null);
            }
        } catch (Throwable t) {
            Psychedelicraft.LOGGER.error("Exception applying shader pass: {}", t);
        }
    }

    private Pass addPass(String id) {
        Pass pass = new Pass(new ArrayList<>());
        passes.add(pass);
        return pass;
    }

    record Pass(List<Runnable> callbacks) {
        int add(Runnable callback) {
            callbacks.add(callback);
            return callbacks.size();
        }

        void replay(int pass) {
            if (pass >= 0 && pass < callbacks.size()) {
                callbacks.get(pass).run();
            }
        }
    }

    public void update(MinecraftClient client, PostEffectProcessor processor, float tickDelta, BiConsumer<String, Runnable> passCollector) {
        final int width = client.getWindow().getFramebufferWidth();
        final int height = client.getWindow().getFramebufferHeight();

        PassState globalState = new PassState(new HashMap<>());

        bindings.global.bindUniforms(globalState, tickDelta, width, height, () -> {
            for (var pass : ((PostEffectPassSupplier)processor).getPasses()) {
                String passId = pass.getId();
                var programBindings = bindings.programBindings.getOrDefault(Identifier.of(passId).withPrefixedPath("post/"), UniformBinding.EMPTY);
                if (programBindings != UniformBinding.EMPTY) {
                    pass.setDisabled();
                    PassState state = new PassState(new HashMap<>(globalState.uniforms));

                    programBindings.bindUniforms(state, tickDelta, width, height, () -> {
                        passCollector.accept(passId, () -> pass.setUniformUpdater(pipeline -> state.bind()));
                    });
                }
            }
        });
    }

    record PassState(Map<String, PostEffectPipeline.Uniform> uniforms) implements UniformSetter {
        @Override
        public void set(String name, String type, Supplier<List<Float>> setter) {
            uniforms.put(name, new PostEffectPipeline.Uniform(name, type, Optional.of(setter.get())));
        }

        List<PostEffectPipeline.Uniform> bind() {
            List<PostEffectPipeline.Uniform> uniforms = new ArrayList<>();
            uniforms.addAll(this.uniforms().values());
            return uniforms;
        }
    }
}
