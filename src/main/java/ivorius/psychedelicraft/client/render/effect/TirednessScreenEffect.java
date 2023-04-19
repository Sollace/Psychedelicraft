/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 19 April 2023
 */
public class TirednessScreenEffect implements ScreenEffect {
    private static final Identifier EYELID_OVERLAY = Psychedelicraft.id("textures/environment/eyelid_overlay.png");

    private float prevOverlayOpacity;
    private float overlayOpacity;

    private int ticksBlinking;

    @Override
    public void update(float tickDelta) {

        PlayerEntity entity = MinecraftClient.getInstance().player;
        DrugProperties properties = DrugProperties.of(entity);

        prevOverlayOpacity = overlayOpacity;

        float drowsyness = Math.max(0, properties.getModifier(Drug.DROWSYNESS) - 0.6F);

        overlayOpacity = MathUtils.approach(overlayOpacity, ticksBlinking > 0 ? drowsyness * 0.9F + MathHelper.sin(entity.age / 10F) : 0, 0.03F);
        if (drowsyness > 0.3F && overlayOpacity > 0.6F) {
            entity.world.playSound(entity.getX(), entity.getY(), entity.getZ(),
                PSSounds.ENTITY_PLAYER_HEARTBEAT,
                SoundCategory.AMBIENT, drowsyness, 0.3F, false);
        }

        if (drowsyness < 0.2F) {
            ticksBlinking = Math.max(ticksBlinking - 1, 0);
            return;
        }

        if (--ticksBlinking <= 0 && (ticksBlinking < -300 || entity.world.random.nextFloat() < properties.getModifier(Drug.DROWSYNESS))) {
            ticksBlinking = (int)entity.world.random.nextTriangular(300, 200);
            entity.sendMessage(Text.literal("...I should really sleep..."), true);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();

        float opacity = MathHelper.lerp(ticks, prevOverlayOpacity, overlayOpacity);

        if (opacity > 0) {
            RenderUtil.drawOverlay(matrices, opacity * 0.8F, screenWidth, screenHeight, EYELID_OVERLAY, 0, 0, 1, 1, (int)(opacity * 5.8F));
        }

        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    @Override
    public void close() {

    }
}
