/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;
/*
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 31.10.14.
 */
@Deprecated(forRemoval = true)
public class ItemRendererModelCustom// implements IItemRenderer
{/*
    public ItemModelRenderer model;
    public ResourceLocation texture;
    public float modelSize;
    public float[] translation;
    public float[] rotation;

    public ItemRendererModelCustom(ItemModelRenderer model, ResourceLocation texture, float modelSize, float[] translation, float[] rotation)
    {
        this.model = model;
        this.texture = texture;
        this.modelSize = modelSize;
        this.translation = translation;
        this.rotation = rotation;
    }

    @Override
    public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type)
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data)
    {
        GL11.glPushMatrix();

        if (type == IItemRenderer.ItemRenderType.ENTITY)
        {
            GL11.glTranslated(0.0, 1.0, 0.0);
        }
        else if (type == IItemRenderer.ItemRenderType.INVENTORY)
        {
            GL11.glTranslated(0.0, 0.3, 0.0);
        }
        else
        {
            GL11.glTranslated(0.5, 1.0, 0.5);
        }

        GL11.glTranslatef(translation[0], translation[1] + 1.0f, translation[2]);

        if (type != IItemRenderer.ItemRenderType.ENTITY)
        {
            float modelScale = 1.0f / modelSize;
            GL11.glScalef(modelScale, modelScale, modelScale);
        }

        GL11.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        model.render(item);

        GL11.glPopMatrix();
    }

    public interface ItemModelRenderer
    {
        void render(ItemStack stack);
    }

    public static class ItemModelRendererSimple implements ItemModelRenderer
    {
        public IModelCustom model;

        public ItemModelRendererSimple(IModelCustom model)
        {
            this.model = model;
        }

        @Override
        public void render(ItemStack stack)
        {
            model.renderAll();
        }
    }*/
}
