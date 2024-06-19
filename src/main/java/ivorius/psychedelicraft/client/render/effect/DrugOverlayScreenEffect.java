package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
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
    public void render(DrawContext context, Window window, float tickDelta) {
        if (MinecraftClient.getInstance().player != null) {
            DrugProperties properties = DrugProperties.of(MinecraftClient.getInstance().player);
            context.getMatrices().push();
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            render(context, window, tickDelta, properties, (D)properties.getDrug(type));
            RenderSystem.enableDepthTest();
            context.getMatrices().pop();
        }
    }

    protected abstract void render(DrawContext context, Window window, float tickDelta, DrugProperties properties, D drug);

    @Override
    public void close() throws Exception {
    }

}
