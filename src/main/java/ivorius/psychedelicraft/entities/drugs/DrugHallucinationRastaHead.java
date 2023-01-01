/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.ModelRastaHead;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class DrugHallucinationRastaHead extends DrugHallucination
{
    public int entityMaxTicks;

    public LivingEntity dummyEntity;
    public LookHelper lookHelper;

    public Model modelRastaHead;

    Identifier rastaHeadTexture;

    public DrugHallucinationRastaHead(PlayerEntity playerEntity)
    {
        super(playerEntity);

        this.entityMaxTicks = (playerEntity.getRandom().nextInt(59) + 120) * 20;

        this.dummyEntity = EntityType.PIG.create(playerEntity.world);
        this.dummyEntity.setPosition(playerEntity.getPos());

        this.lookHelper = new EntityLookHelper(dummyEntity);

        this.modelRastaHead = new ModelRastaHead();

//        this.chatBot = new ChatBotRastahead(playerEntity.getRNG(), playerEntity);

        rastaHeadTexture = Psychedelicraft.id(Psychedelicraft.filePathTextures + "rastaHeadTexture.png");
    }

    @Override
    public int getMaxHallucinations() {
        return UNLIMITED;
    }

    @Override
    public boolean isDead() {
        return this.entityTicksAlive >= this.entityMaxTicks;
    }

    @Override
    public void update()
    {
        super.update();

        this.dummyEntity.lastRenderX = this.dummyEntity.getX();
        this.dummyEntity.lastRenderY = this.dummyEntity.getY();
        this.dummyEntity.lastRenderZ = this.dummyEntity.getZ();

        this.dummyEntity.prevHeadYaw = this.dummyEntity.headYaw;
        this.dummyEntity.prevPitch = this.dummyEntity.getPitch();

        this.lookHelper.setLookPositionWithEntity(player, 3.0f, 3.0f);
        this.lookHelper.onUpdateLook();

        Vec3d wanted = player.getPos().add(
                MathHelper.sin(player.age / 50.0f) * 5.0f,
                0,
                MathHelper.cos(player.age / 50.0f) * 5.0f
        );

        double totalDist = wanted.distanceTo(dummyEntity.getPos());

        if (totalDist > 3) {
            dummyEntity.setVelocity(wanted.subtract(dummyEntity.getPos()).multiply(0.05D / totalDist));
        } else {
            dummyEntity.setVelocity(dummyEntity.getVelocity().multiply(0.9D));
        }

        this.dummyEntity.setPosition(this.dummyEntity.getPos().add(dummyEntity.getVelocity()));

        if (this.dummyEntity instanceof LivingEntity)
        {
            EntityLiving entityliving = this.dummyEntity;

            double var9 = this.dummyEntity.posX - this.dummyEntity.prevPosX;
            double var12 = this.dummyEntity.posZ - this.dummyEntity.prevPosZ;
            float var11 = MathHelper.sqrt_double(var9 * var9 + var12 * var12) * 4.0F;

            if (var11 > 1.0F)
            {
                var11 = 1.0F;
            }

            entityliving.limbSwingAmount += (var11 / 3f - entityliving.limbSwingAmount) * 0.4F;
            entityliving.limbSwing += entityliving.limbSwingAmount;
        }
    }

    @Override
    public void render(float par1, float dAlpha)
    {
        float alpha = MathHelper.sin((float) Math.min(this.entityTicksAlive, this.entityMaxTicks - 2) / (float) (this.entityMaxTicks - 2) * 3.1415f) * 18f;

        if (alpha > 1.0f)
        {
            alpha = 1.0f;
        }
        if (alpha > 0.0f)
        {
            double var3 = this.dummyEntity.lastTickPosX + (this.dummyEntity.posX - this.dummyEntity.lastTickPosX) * par1;
            double var5 = this.dummyEntity.lastTickPosY + (this.dummyEntity.posY - this.dummyEntity.lastTickPosY) * par1;
            double var7 = this.dummyEntity.lastTickPosZ + (this.dummyEntity.posZ - this.dummyEntity.lastTickPosZ) * par1;
            float var9 = -(this.dummyEntity.prevRotationYawHead + (this.dummyEntity.rotationYawHead - this.dummyEntity.prevRotationYawHead) * par1);
            float pitch = this.dummyEntity.prevRotationPitch + (this.dummyEntity.rotationPitch - this.dummyEntity.prevRotationPitch) * par1;

            GL11.glPushMatrix();

            OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            GL11.glEnable(GL11.GL_BLEND);
            PSRenderStates.setTexture2DEnabled(OpenGlHelper.lightmapTexUnit, false);
            GL11.glTranslated(var3 - RenderManager.renderPosX, var5 + 1.0 - RenderManager.renderPosY, var7 - RenderManager.renderPosZ);
            GL11.glRotatef(pitch, 0.0f, 0.0f, 1.0f);
            GL11.glRotatef(var9, 0.0f, 1.0f, 0.0f);
            GL11.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);

            Minecraft.getMinecraft().renderEngine.bindTexture(rastaHeadTexture);
            modelRastaHead.render(dummyEntity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

            PSRenderStates.setTexture2DEnabled(OpenGlHelper.lightmapTexUnit, true);
            PSRenderStates.setOverrideColor((float[]) null);

            GL11.glPopMatrix();
        }
    }
}
