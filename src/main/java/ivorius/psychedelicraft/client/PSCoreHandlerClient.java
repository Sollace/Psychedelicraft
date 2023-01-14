/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client;

import ivorius.psychedelicraft.client.render.*;
import ivorius.psychedelicraft.client.render.shader.program.PSRenderStates;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.entity.Entity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by lukas on 21.02.14.
 */

//TODO: (Sollace) A lot of these are hooks that need reimplementing
public class PSCoreHandlerClient
{
    // Taken from RenderHelper
   // private final Vec3 field_82884_b = Vec3.createVectorHelper(0.20000000298023224D, 1.0D, -0.699999988079071D).normalize();
   // private final Vec3 field_82885_c = Vec3.createVectorHelper(-0.20000000298023224D, 1.0D, 0.699999988079071D).normalize();

    //@SubscribeEvent
    /*public void renderHand(RenderHandEvent event)
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
    }*/

    public void setupCameraTransform(CallbackInfo event) {
        if (PSRenderStates.setupCameraTransform()) {
            event.cancel();
        }
    }

    /*
    //@SubscribeEvent
    public void psycheGLFogi(GLFogiEvent event)
    {
        if (event.pname == GL11.GL_FOG_MODE)
        {
            PSRenderStates.setFogMode(event.param);
        }
    }
*/
/*
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
*/
}
