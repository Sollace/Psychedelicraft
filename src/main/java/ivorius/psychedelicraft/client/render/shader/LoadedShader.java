package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;

import org.joml.Vector3f;

import com.google.gson.JsonSyntaxException;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import ivorius.psychedelicraft.client.render.shader.UniformBinding.UniformSetter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.util.Identifier;

class LoadedShader extends PostEffectProcessor {
    private final UniformBinding.Set bindings;

    private int width;
    private int height;
    private float time;
    private float lastTickDelta;

    private List<Pass> passes;

    public LoadedShader(MinecraftClient client, Identifier id, UniformBinding.Set bindings) throws IOException, JsonSyntaxException {
        super(client.getTextureManager(), new ModdedResourceManager(client.getResourceManager(), id.getNamespace()), client.getFramebuffer(), id);
        this.bindings = bindings;
        if (passes == null) {
            passes = new ArrayList<>();
        }
        setupDimensions(
            client.getWindow().getFramebufferWidth(),
            client.getWindow().getFramebufferHeight()
        );
    }

    @Override
    public void render(float tickDelta) {
        if (tickDelta < lastTickDelta) {
            time += 1 - lastTickDelta;
            time += tickDelta;
        } else {
            time += tickDelta - lastTickDelta;
        }
        this.lastTickDelta = tickDelta;
        while (time > 20) {
            time -= 20;
        }

        final float passRenderFrame = time / 20F;
        for (Pass pass : passes) {
            pass.render(passRenderFrame, tickDelta);
        }
    }

    @Override
    public void setupDimensions(int targetsWidth, int targetsHeight) {
        this.width = targetsWidth;
        this.height = targetsHeight;
        super.setupDimensions(targetsWidth, targetsHeight);
    }

    @Override
    public PostEffectPass addPass(String programName, Framebuffer source, Framebuffer dest) throws IOException {
        PostEffectPass pass = super.addPass(programName, source, dest);
        if (passes == null) {
            passes = new ArrayList<>();
        }
        passes.add(new Pass(pass));
        return pass;
    }

    class Pass implements UniformSetter {
        private final JsonEffectShaderProgram program;
        private final PostEffectPass pass;

        private final List<FloatConsumer> replay = new ArrayList<>();
        private int updateCount;

        public Pass(PostEffectPass pass) {
            this.pass = pass;
            this.program = pass.getProgram();
        }

        public void render(float passRenderTime, float tickDelta) {
            if (updateCount == 0) {
                replay.clear();
                var programBindings = bindings.programBindings.getOrDefault(pass.getName(), UniformBinding.EMPTY);
                bindings.global.bindUniforms(this, tickDelta, width, height, () -> {
                    programBindings.bindUniforms(this, tickDelta, width, height, () -> {
                        replay.add(pass::render);
                    });
                });
            }

            updateCount = (updateCount + 1) % 2;
            for (FloatConsumer action : replay) {
                action.accept(passRenderTime);
            }
        }

        @Override
        public void set(String name, float value) {
            var uniform = program.getUniformByName(name);
            if (uniform != null) {
                replay.add(f -> uniform.set(value));
            }
        }

        @Override
        public void set(String name, float... values) {
            var uniform = program.getUniformByName(name);
            if (uniform != null) {
                var copy = Arrays.copyOf(values, values.length);
                replay.add(f -> uniform.set(copy));
            }
        }

        @Override
        public void set(String name, Vector3f values) {
            var uniform = program.getUniformByName(name);
            if (uniform != null) {
                var copy = new Vector3f(values);
                replay.add(f -> uniform.set(copy));
            }
        }
    }
}