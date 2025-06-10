/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
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

    private long lastPlayTime;

    @Override
    public void update(float tickDelta) {

        PlayerEntity entity = MinecraftClient.getInstance().player;
        if (entity == null || MinecraftClient.getInstance().isPaused()) {
            return;
        }
        float baseDrowsyness = MathHelper.clamp(DrugProperties.of(entity).getModifier(Drug.DROWSYNESS)
                + DrugProperties.of(entity).getCardiacArrestProgress(tickDelta), 0, 1);

        prevOverlayOpacity = overlayOpacity;

        float drowsyness = Math.max(0, baseDrowsyness - 0.6F);

        overlayOpacity = MathUtils.approach(overlayOpacity, ticksBlinking > 0 ? drowsyness * 0.9F + MathHelper.sin(entity.age / 10F) : 0, 0.03F);
        if (drowsyness > 0.3F && overlayOpacity > 0.6F) {
            long now = System.currentTimeMillis();
            if (now > (lastPlayTime + 1000)) {
                lastPlayTime = now;
                entity.getWorld().playSoundClient(entity.getX(), entity.getY(), entity.getZ(),
                    PSSounds.ENTITY_PLAYER_HEARTBEAT,
                    SoundCategory.AMBIENT, drowsyness, 0.3F, false);
            }
        }

        if (drowsyness < 0.2F) {
            ticksBlinking = Math.max(ticksBlinking - 1, 0);
            return;
        }

        if (--ticksBlinking <= 0 && (ticksBlinking < -300 || entity.getWorld().random.nextFloat() < baseDrowsyness)) {
            ticksBlinking = (int)entity.getWorld().random.nextTriangular(300, 200);
            entity.sendMessage(Text.literal("I really should get to bed..."), true);
        }
    }

    @Override
    public void render(DrawContext context, Window window, float tickDelta) {
        float opacity = MathHelper.lerp(tickDelta, prevOverlayOpacity, overlayOpacity);
        RenderUtil.drawOverlay(context, EYELID_OVERLAY, opacity * 0.8F, window.getScaledWidth(), window.getScaledHeight(), 0, 0, 1, 1, (int)(opacity * 5.8F));
    }

    @Override
    public void close() {

    }
}
