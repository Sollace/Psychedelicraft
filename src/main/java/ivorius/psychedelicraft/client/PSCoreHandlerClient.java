/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */
/*
package ivorius.psychedelicraft.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ivorius.psychedelicraft.*;
import ivorius.psychedelicraft.client.sound.MovingSoundDrug;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraft.fluids.FluidWithIconSymbolRegistering;
import net.minecraft.entity.Entity;
import ivorius.psychedelicraft.client.rendering.DrugEffectInterpreter;
import ivorius.psychedelicraft.client.rendering.GLStateProxy;
import ivorius.psychedelicraft.client.rendering.SmoothCameraHelper;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraftcore.PsycheCoreBusCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 21.02.14.
 */
/*
TODO: (Sollace) A lot of these are hooks that need reimplementing
public class PSCoreHandlerClient
{
    // Taken from RenderHelper
    private final Vec3 field_82884_b = Vec3.createVectorHelper(0.20000000298023224D, 1.0D, -0.699999988079071D).normalize();
    private final Vec3 field_82885_c = Vec3.createVectorHelper(-0.20000000298023224D, 1.0D, 0.699999988079071D).normalize();

    public void register()
    {
        PsycheCoreBusCommon.EVENT_BUS.register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event)
    {
        IIconRegister iconRegister = event.map;
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values())
        {
            if (fluid instanceof FluidWithIconSymbolRegistering)
                ((FluidWithIconSymbolRegistering) fluid).registerIcons(iconRegister, event.map.getTextureType());
        }
    }

    @SideOnly(Side.CLIENT)
    public void initializeMovingSoundDrug(Entity entity, DrugProperties drugProperties)
    {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        for (String drugName : drugProperties.getAllDrugNames())
        {
            if (PSConfig.hasBGM(drugName))
                soundHandler.playSound(new MovingSoundDrug(new ResourceLocation(Psychedelicraft.MODID, "drug." + drugName.toLowerCase()), entity, drugProperties, drugName));
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event)
    {
        if (event.type == RenderGameOverlayEvent.ElementType.PORTAL)
        {
            Minecraft mc = Minecraft.getMinecraft();
            EntityLivingBase renderEntity = mc.renderViewEntity;
            DrugProperties drugProperties = DrugProperties.getDrugProperties(renderEntity);

            if (drugProperties != null && drugProperties.renderer != null)
            {
                drugProperties.renderer.renderOverlaysAfterShaders(event.partialTicks, renderEntity, renderEntity.ticksExisted, event.resolution.getScaledWidth(), event.resolution.getScaledHeight(), drugProperties);
            }
        }
    }

    @SubscribeEvent
    public void renderWorld(RenderWorldEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();

        float partialTicks = event.partialTicks;
        int rendererUpdateCount = mc.ingameGUI.getUpdateCounter();
        float ticks = partialTicks + rendererUpdateCount;

        if (event instanceof RenderWorldEvent.Pre)
        {
//            setPlayerAngles(partialTicks);

            PSRenderStates.preRender(ticks);

            for (String pass : PSRenderStates.getRenderPasses(partialTicks))
            {
                if (!pass.equals("Default"))
                {
                    if (PSRenderStates.startRenderPass(pass, partialTicks, ticks))
                    {
                        mc.entityRenderer.renderWorld(partialTicks, 0L);
                        PSRenderStates.endRenderPass();
                    }
                }
            }

            PSRenderStates.startRenderPass("Default", partialTicks, ticks);
            PSRenderStates.preRender3D(ticks);
        }
        else if (event instanceof RenderWorldEvent.Post)
        {
            PSRenderStates.endRenderPass();

            DrugProperties drugProperties = DrugProperties.getDrugProperties(mc.renderViewEntity);

            if (drugProperties != null && drugProperties.renderer != null)
                drugProperties.renderer.renderOverlaysBeforeShaders(event.partialTicks, mc.renderViewEntity, rendererUpdateCount, mc.displayWidth, mc.displayHeight, drugProperties);

            PSRenderStates.postRender(ticks, partialTicks);
        }
    }

    @SubscribeEvent
    public void orientCamera(OrientCameraEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase renderEntity = mc.renderViewEntity;
        DrugProperties drugProperties = DrugProperties.getDrugProperties(renderEntity);
        int rendererUpdateCount = renderEntity.ticksExisted;

        if (drugProperties != null && drugProperties.renderer != null)
            drugProperties.renderer.distortScreen(event.partialTicks, renderEntity, rendererUpdateCount, drugProperties);
    }

    @SubscribeEvent
    public void psycheGLEnable(GLSwitchEvent event)
    {
        PSRenderStates.setEnabled(event.cap, event.enable);
    }

    @SubscribeEvent
    public void psycheGLBlendFunc(GLBlendFuncEvent event)
    {
        PSRenderStates.setBlendFunc(event.sFactor, event.dFactor, event.dfactorAlpha, event.dfactorAlpha);
    }

    @SubscribeEvent
    public void psycheGLActiveTexture(GLActiveTextureEvent event)
    {
        GLStateProxy.setActiveTextureUnit(event.texture);
    }

    @SubscribeEvent
    public void standardItemLighting(ItemLightingEvent event)
    {
        if (event.enable)
        {
            float var0 = 0.4F;
            float var1 = 0.6F;
            float var2 = 0.0F;

            PSRenderStates.setGLLightEnabled(true);
            PSRenderStates.setGLLight(0, (float) field_82884_b.xCoord, (float) field_82884_b.yCoord, (float) field_82884_b.zCoord, var1, var2);
            PSRenderStates.setGLLight(1, (float) field_82885_c.xCoord, (float) field_82885_c.yCoord, (float) field_82885_c.zCoord, var1, var2);
            PSRenderStates.setGLLightAmbient(var0);
        }
        else
        {
            PSRenderStates.setGLLightEnabled(false);
        }
    }

    @SubscribeEvent
    public void renderHeldItem(RenderHeldItemEvent event)
    {
        float partialTicks = event.partialTicks;

        Minecraft mc = Minecraft.getMinecraft();
        int rendererUpdateCount = mc.ingameGUI.getUpdateCounter();

        DrugProperties drugProperties = DrugProperties.getDrugProperties(mc.renderViewEntity);

        if (drugProperties != null)
        {
            float shiftX = DrugEffectInterpreter.getHandShiftX(drugProperties, (float) rendererUpdateCount + partialTicks);
            float shiftY = DrugEffectInterpreter.getHandShiftY(drugProperties, (float) rendererUpdateCount + partialTicks);
            GL11.glTranslatef(shiftX, shiftY, 0.0f);
        }
    }

    @SubscribeEvent
    public void renderEntities(RenderEntitiesEvent event)
    {
        int pass = MinecraftForgeClient.getRenderPass();

        if (pass == 1)
        {
            Minecraft mc = Minecraft.getMinecraft();
            EntityLivingBase renderEntity = mc.renderViewEntity;
            DrugProperties drugProperties = DrugProperties.getDrugProperties(renderEntity);

            if (drugProperties != null && drugProperties.renderer != null)
            {
                drugProperties.renderer.renderAllHallucinations(event.partialTicks, drugProperties);
            }
        }
    }

    @SubscribeEvent
    public void renderHand(RenderHandEvent event)
    {
        if (event instanceof RenderHandEvent.Pre)
        {
            if (!"Default".equals(PSRenderStates.currentRenderPass))
            {
                PSRenderStates.setDepthMultiplier(0.0f);
                event.setCanceled(true);
            }
        }
        else if (event instanceof RenderHandEvent.Post)
        {
            if (!"Default".equals(PSRenderStates.currentRenderPass))
            {
                PSRenderStates.setDepthMultiplier(1.0f);
            }
        }
    }

    @SubscribeEvent
    public void setPlayerAngles(SetPlayerAnglesEvent event)
    {
        float partialTicks = event.partialTicks;

        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase renderEntity = mc.renderViewEntity;
        DrugProperties drugProperties = DrugProperties.getDrugProperties(renderEntity);

        if (drugProperties != null)
        {
            float smoothness = DrugEffectInterpreter.getSmoothVision(drugProperties);
            if (smoothness < 1.0f && mc.inGameHasFocus)
            {
                float deltaX = mc.mouseHelper.deltaX;
                float deltaY = mc.mouseHelper.deltaY;

                float[] angles = SmoothCameraHelper.instance.getAngles(mc.gameSettings.mouseSensitivity, partialTicks, deltaX, deltaY, mc.gameSettings.invertMouse);

                if (!mc.gameSettings.smoothCamera)
                {
                    float[] originalAngles = SmoothCameraHelper.instance.getOriginalAngles(mc.gameSettings.mouseSensitivity, partialTicks, deltaX, deltaY, mc.gameSettings.invertMouse);
                    renderEntity.setAngles(angles[0] - originalAngles[0], angles[1] - originalAngles[1]);
                }
                else
                {
                    renderEntity.setAngles(angles[0], angles[1]);
                }
            }
        }
    }

    @SubscribeEvent
    public void psycheGLFogi(GLFogiEvent event)
    {
        if (event.pname == GL11.GL_FOG_MODE)
        {
            PSRenderStates.setFogMode(event.param);
        }
    }

    @SubscribeEvent
    public void getSoundVolume(GetSoundVolumeEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        DrugProperties drugProperties = DrugProperties.getDrugProperties(mc.renderViewEntity);

        if (drugProperties != null)
        {
            event.volume = MathHelper.clamp_float(event.volume * drugProperties.getSoundMultiplier(), 0.0f, 1.0f);
        }
    }

    @SubscribeEvent
    public void setupCameraTransform(SetupCameraTransformEvent event)
    {
        if (PSRenderStates.setupCameraTransform())
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void renderBlockOverlay(RenderBlockOverlayEvent event)
    {
        if (!"Default".equals(PSRenderStates.currentRenderPass))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void fixGLState(GLStateFixEvent event)
    {
        PSRenderStates.setUseScreenTexCoords(false);
        PSRenderStates.setTexture2DEnabled(OpenGlHelper.defaultTexUnit, true);
    }

    @SubscribeEvent
    public void glClear(GLClearEvent event)
    {
        event.currentMask = event.currentMask & PSRenderStates.getCurrentAllowedGLDataMask();
    }

    @SubscribeEvent
    public void renderSkyPre(RenderSkyEvent.Pre event)
    {
        PSRenderStates.preRenderSky(event.partialTicks);
    }
}
*/
