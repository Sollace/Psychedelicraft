package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

import org.lwjgl.system.MemoryStack;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.util.memory.ObjectAllocator;
import net.minecraft.util.Identifier;

class LoadedShader {
    private final Identifier id;
    private final MinecraftClient client;

    private final List<Pass> passes = new ArrayList<>();
    private final Map<PostEffectPassSupplier.Pass, Pass> passById = new HashMap<>();
    private int passCount = 0;

    public LoadedShader(MinecraftClient client, Identifier id) throws IOException, JsonSyntaxException {
        this.client = client;
        this.id = id;
    }

    @SuppressWarnings("deprecation")
    public void render(ObjectAllocator pool, float tickDelta) {
        try {
            PostEffectProcessor processor = client.getShaderLoader().loadPostEffect(id, DefaultFramebufferSet.MAIN_ONLY);
            if (processor == null) {
                return;
            }

            passCount = 0;
            passes.clear();
            passById.clear();
            update(client, processor, tickDelta, (id, callback) -> {
                passCount = Math.max(passCount, passById.computeIfAbsent(id, this::addPass).add(callback));
            });

            for (int i = 0; i < passCount; i++) {
                for (Pass pass : passes) {
                    pass.replay(i);
                }
                processor.render(client.getFramebuffer(), pool);
            }
        } catch (Throwable t) {
            Psychedelicraft.LOGGER.error("Exception applying shader pass: {}", t);
        }
    }

    private Pass addPass(PostEffectPassSupplier.Pass id) {
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

    public void update(MinecraftClient client, PostEffectProcessor processor, float tickDelta, BiConsumer<PostEffectPassSupplier.Pass, Runnable> passCollector) {
        final int width = client.getWindow().getFramebufferWidth();
        final int height = client.getWindow().getFramebufferHeight();

        for (var pass : ((PostEffectPassSupplier)processor).getPasses()) {
            Identifier fragmentShaderId = pass.getPipeline().getFragmentShader();

            var programBindings = UniformBindings.VALUES.get(fragmentShaderId);
            if (programBindings != null) {
                pass.setDisabled(true);
                programBindings.bindUniforms(tickDelta, width, height, values -> {
                    passCollector.accept(pass, () -> {
                        pass.setDisabled(false);
                        values.accept((name, structure) -> {
                            var buffer = pass.getUniforms().get(name);

                            if (buffer != null) {
                                long bufferSize = buffer.size();
                                if ((buffer.usage() & GpuBuffer.USAGE_COPY_DST) == 0) {
                                    buffer.close();

                                    // recreate buffer to allow writing to it
                                    buffer = RenderSystem.getDevice().createBuffer(() -> pass.getId() + " / " + name, GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, bufferSize);
                                    pass.getUniforms().put(name, buffer);
                                }

                                // write to the butter
                                try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                                    Std140Builder builder = Std140Builder.onStack(memoryStack, (int)bufferSize);
                                    structure.accept(builder);
                                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), builder.get());
                                }
                            }
                        });
                    });
                });
            }
        }
    }
}
