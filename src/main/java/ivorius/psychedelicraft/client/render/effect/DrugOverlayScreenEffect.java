package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public abstract class DrugOverlayScreenEffect<D extends Drug> implements ScreenEffect {

    private final DrugType type;

    public DrugOverlayScreenEffect(DrugType type) {
        this.type = type;
    }

    @Override
    public boolean shouldApply(float tickDelta) {
        return DrugProperties.of((Entity)MinecraftClient.getInstance().player).filter(properties -> properties.isDrugActive(type)).isPresent();
    }

    @Override
    public void update(float tickDelta) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        DrugProperties properties = DrugProperties.of(MinecraftClient.getInstance().player);
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        render(matrices, vertices, screenWidth, screenHeight, ticks, properties, (D)properties.getDrug(type));
        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    protected abstract void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenheight, float ticks, DrugProperties properties, D drug);

    @Override
    public void close() throws Exception {
    }

}
