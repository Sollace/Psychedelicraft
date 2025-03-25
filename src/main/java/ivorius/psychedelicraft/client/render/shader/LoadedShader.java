package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.google.gson.JsonSyntaxException;

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

    public void render(Pool pool, float tickDelta) {
        PostEffectProcessor processor = client.getShaderLoader().loadPostEffect(id, DefaultFramebufferSet.MAIN_ONLY);
        if (processor == null) {
            return;
        }

        if (updater.update(processor, tickDelta)) {
            var original = ((PostEffectPassSupplier)processor).getPasses();
            try {
                ((PostEffectPassSupplier)processor).setPasses(updater.passes);
                processor.render(client.getFramebuffer(), pool);
            } finally {
                ((PostEffectPassSupplier)processor).setPasses(original);
            }
        }
    }

    class UpdateTracker {
        private int updateCount;

        private final List<PostEffectPass> passes = new ArrayList<>();
        private boolean enabled;

        public boolean update(PostEffectProcessor processor, float tickDelta) {
            if (updateCount == 0) {
                passes.clear();
                enabled = false;
                uniformValues.update(processor, tickDelta, passes, () -> enabled = true);
            }

            updateCount = (updateCount + 1) % 2;

            if (!enabled) {
                return false;
            }

            for (PostEffectPass pass : passes) {
                try {
                    for (var update : uniformValues.values) {
                        update.accept(pass.getProgram());
                    }
                } catch (Throwable t) {
                    throw new RuntimeException("Exception updating uniforms for shader " + id + " pass " + ((PostEffectPassSupplier.Pass)pass).getId(), t);
                }
            }
            return true;
        }
    }


    class UniformValues implements UniformSetter {
        private final List<Consumer<ShaderProgram>> values = new ArrayList<>();

        public void update(PostEffectProcessor postEffectProcessor, float tickDelta, List<PostEffectPass> retainedPasses, Runnable enableShader) {
            values.clear();
            final int width = client.getWindow().getFramebufferWidth();
            final int height = client.getWindow().getFramebufferHeight();

            bindings.global.bindUniforms(this, tickDelta, width, height, () -> {
                for (PostEffectPass pass : ((PostEffectPassSupplier)postEffectProcessor).getPasses()) {
                    var programBindings = bindings.programBindings.getOrDefault(((PostEffectPassSupplier.Pass)pass).getId(), UniformBinding.EMPTY);
                    if (programBindings != UniformBinding.EMPTY) {
                        programBindings.bindUniforms(this, tickDelta, width, height, () -> {
                            retainedPasses.add(pass);
                            enableShader.run();
                        });
                    } else {
                        retainedPasses.add(pass);
                    }
                }
            });
        }

        @Override
        public void set(String name, float value) {
            values.add(uniformSetter(name, uniform -> uniform.set(value)));
        }

        @Override
        public void set(String name, float... values) {
            var copy = Arrays.copyOf(values, values.length);
            this.values.add(uniformSetter(name, uniform -> uniform.set(copy)));
        }

        @Override
        public void set(String name, Vector3fc values) {
            var copy = new Vector3f(values);
            this.values.add(uniformSetter(name, uniform -> uniform.set(copy)));
        }

        @Override
        public void set(String name, Vector4fc values) {
            var copy = new Vector4f(values);
            this.values.add(uniformSetter(name, uniform -> uniform.set(copy)));
        }

        private static Consumer<ShaderProgram> uniformSetter(String name, Consumer<Uniform> consumer) {
            return program -> {
                try {
                    var uniform = program.getUniform(name);
                    if (uniform != null) {
                        consumer.accept(uniform);
                    }
                } catch (Throwable t) {
                    throw new RuntimeException("Exception setting uniform: " + name, t);
                }
            };
        }
    }

}