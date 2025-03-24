package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderPass;

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

    private long lastUpdateTime;

    private boolean shouldRender;
    private final List<PostEffectPass> passes = new ArrayList<>();

    public LoadedShader(MinecraftClient client, Identifier id, UniformBinding.Set bindings) throws IOException, JsonSyntaxException {
        this.client = client;
        this.id = id;
        this.uniformValues = new UniformValues(bindings);
    }

    @SuppressWarnings("deprecation")
    public void render(Pool pool, float tickDelta) {
        PostEffectProcessor processor = client.getShaderLoader().loadPostEffect(id, DefaultFramebufferSet.MAIN_ONLY);
        if (processor == null) {
            return;
        }

        if (updateUniforms(processor, tickDelta)) {
            var original = ((PostEffectPassSupplier)processor).getPasses();
            try {
                ((PostEffectPassSupplier)processor).setPasses(passes);
                // Deprecated
                processor.render(client.getFramebuffer(), pool, pass -> {
                    try {
                        for (var update : uniformValues.values) {
                            update.accept(pass);
                        }
                    } catch (Throwable t) {
                        throw new RuntimeException("Exception updating uniforms for shader " + id + " pass " + ((PostEffectPassSupplier.Pass)pass).getId(), t);
                    }
                });
            } finally {
                ((PostEffectPassSupplier)processor).setPasses(original);
            }
        }
    }

    private boolean updateUniforms(PostEffectProcessor processor, float tickDelta) {
        long now = System.currentTimeMillis();

        if (now > lastUpdateTime - 100) {
            lastUpdateTime = now;
            passes.clear();
            shouldRender = false;
            uniformValues.update(client, processor, tickDelta, passes, () -> shouldRender = true);
        }

        return shouldRender;
    }


    private static class UniformValues implements UniformSetter {
        private final UniformBinding.Set bindings;
        private final List<Consumer<RenderPass>> values = new ArrayList<>();

        UniformValues(UniformBinding.Set bindings) {
            this.bindings = bindings;
        }

        public void update(MinecraftClient client, PostEffectProcessor postEffectProcessor, float tickDelta, List<PostEffectPass> retainedPasses, Runnable markRenderable) {
            values.clear();
            final int width = client.getWindow().getFramebufferWidth();
            final int height = client.getWindow().getFramebufferHeight();

            bindings.global.bindUniforms(this, tickDelta, width, height, () -> {
                for (PostEffectPass pass : ((PostEffectPassSupplier)postEffectProcessor).getPasses()) {
                    Identifier id = ((PostEffectPassSupplier.Pass)pass).getPipeline().getFragmentShader();
                    var programBindings = bindings.programBindings.getOrDefault(id, UniformBinding.EMPTY);
                    programBindings.bindUniforms(this, tickDelta, width, height, () -> {
                        retainedPasses.add(pass);
                        markRenderable.run();
                    });
                    if (!Psychedelicraft.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
                        retainedPasses.add(pass);
                    }
                }
            });
        }

        @Override
        public void set(String name, float value) {
            this.values.add(pass -> pass.setUniform(name, value));
        }

        @Override
        public void set(String name, float... values) {
            var copy = Arrays.copyOf(values, values.length);
            this.values.add(pass -> pass.setUniform(name, copy));
        }
    }

}