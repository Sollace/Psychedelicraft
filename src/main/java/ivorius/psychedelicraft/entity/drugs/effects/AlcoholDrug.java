/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drugs.effects;

import ivorius.psychedelicraft.PSDamageSources;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 01.11.14.
 */
public class AlcoholDrug extends SimpleDrug {
    public static void rotateEntityPitch(Entity entity, double amount) {
        entity.setPitch((float)MathHelper.clamp(entity.getPitch() + amount, -90F, 90F));
    }

    public static void rotateEntityYaw(Entity entity, double amount) {
        entity.setYaw(entity.getYaw() + (float)amount);
    }

    public AlcoholDrug(double decSpeed, double decSpeedPlus) {
        super(decSpeed, decSpeedPlus);
    }

    @Override
    public float viewWobblyness() {
        return (float)getActiveValue() * 0.5f;
    }

    @Override
    public float doubleVision() {
        return MathHelper.lerp((float)getActiveValue(), 0.25f, 1.0f);
    }

    @Override
    public float motionBlur() {
        return MathHelper.lerp((float)getActiveValue(), 0.5f, 1.0f) * 0.3f;
    }

    @Override
    public void update(LivingEntity entity, DrugProperties drugProperties) {
        super.update(entity, drugProperties);

        if (getActiveValue() > 0) {
            int ticksExisted = drugProperties.ticksExisted;
            Random random = entity.getRandom();

            double activeValue = getActiveValue();

            if ((ticksExisted % 20) == 0) {
                double damageChance = (activeValue - 0.9f) * 2.0f;

                if (ticksExisted % 20 == 0 && random.nextFloat() < damageChance) {
                    entity.damage(PSDamageSources.ALCOHOL_POISONING, (int) ((activeValue - 0.9f) * 50.0f + 4.0f));
                }
            }

            double motionEffect = Math.min(activeValue, 0.8);

//            player.motionX += MathHelper.sin(ticksExisted / 10.0F * (float) Math.PI) / 40.0F * motionEffect * (random.nextFloat() + 0.5F);
//            player.motionZ += MathHelper.cos(ticksExisted / 10.0F * (float) Math.PI) / 40.0F * motionEffect * (random.nextFloat() + 0.5F);
//
//            player.motionX *= (random.nextFloat() - 0.5F) * 2 * motionEffect + 1.0F;
//            player.motionZ *= (random.nextFloat() - 0.5F) * 2 * motionEffect + 1.0F;

            rotateEntityPitch(entity, MathHelper.sin(ticksExisted / 600.0F * (float) Math.PI) / 2.0F * motionEffect * (random.nextFloat() + 0.5F));
            rotateEntityYaw(entity, MathHelper.cos(ticksExisted / 500.0F * (float) Math.PI) / 1.3F * motionEffect * (random.nextFloat() + 0.5F));

            rotateEntityPitch(entity, MathHelper.sin(ticksExisted / 180.0F * (float) Math.PI) / 3.0F * motionEffect * (random.nextFloat() + 0.5F));
            rotateEntityYaw(entity, MathHelper.cos(ticksExisted / 150.0F * (float) Math.PI) / 2.0F * motionEffect * (random.nextFloat() + 0.5F));
        }
    }

    @Override
    public void drawOverlays(MatrixStack matrices, float partialTicks, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties) {
        float alcohol = (float)getActiveValue();
        if (alcohol <= 0) {
            return;
        }

        float overlayAlpha = Math.min(0.8F, (MathHelper.sin(updateCounter / 80F) * alcohol * 0.5F + alcohol));
        Sprite sprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
        DrugRenderer.renderOverlay(overlayAlpha * 0.25f, width, height, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                sprite.getMinU(),
                sprite.getMinV(),
                sprite.getMaxU(),
                sprite.getMaxV(), 0);
    }
}
