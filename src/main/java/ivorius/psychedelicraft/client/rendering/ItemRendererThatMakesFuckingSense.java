/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static net.minecraft.client.renderer.ItemRenderer.renderItemIn2D;
import static net.minecraftforge.client.IItemRenderer.ItemRenderType.*;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.EQUIPPED_BLOCK;

/**
 * Created by lukas on 23.10.14.
 */
@Deprecated
public class ItemRendererThatMakesFuckingSense implements IItemRenderer
{
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    private boolean renderWithColor = true;
    private boolean renderEffect = true;
    private int zLevel = 0;
    private RenderItem renderItem = RenderItem.getInstance();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return type != FIRST_PERSON_MAP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION || helper == EQUIPPED_BLOCK;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        if (type == INVENTORY)
        {
            renderItemInInventory(item, 0, 0);
        }
        else
        {
            if (type == EQUIPPED_FIRST_PERSON || type == EQUIPPED)
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);

            Entity entity = data.length >= 1 && data[1] instanceof Entity ? (Entity) data[1] : null;
            int neededPasses = item.getItem() instanceof RenderPassesCustom ? ((RenderPassesCustom) item.getItem()).getRenderPassesCustom(item) : 1;
            boolean isAlpha = false;

            RenderPassesCustom passesCustom = item.getItem() instanceof RenderPassesCustom ? (RenderPassesCustom) item.getItem() : null;

            for (int pass = 0; pass < neededPasses; pass++)
            {
                boolean needsAlpha = passesCustom != null && passesCustom.hasAlphaCustom(item, pass);

                if (needsAlpha)
                {
                    isAlpha = true;
                    GL11.glEnable(GL11.GL_BLEND);
                    OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                }
                else if (isAlpha)
                {
                    isAlpha = false;
                    GL11.glDisable(GL11.GL_BLEND);
                }

                setColor(item, isAlpha, pass);

                GL11.glPushMatrix();
                this.renderItem(entity, item, pass, type);
                GL11.glPopMatrix();
            }

            if (isAlpha)
                GL11.glDisable(GL11.GL_BLEND);
        }
    }

    public static void setColor(ItemStack stack, boolean isAlpha, int pass)
    {
        MCColorHelper.setColor(stack.getItem().getColorFromItemStack(stack, pass), isAlpha);
    }

    private void renderItem(Entity entity, ItemStack item, int pass, ItemRenderType renderType)
    {
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();

        IIcon iicon = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).getItemIcon(item, pass) : item.getItem().getIcon(item, pass);

        if (iicon == null)
            return;

        texturemanager.bindTexture(texturemanager.getResourceLocation(item.getItemSpriteNumber()));
        TextureUtil.func_152777_a(false, false, 1.0F);
        Tessellator tessellator = Tessellator.instance;
        float f = iicon.getMinU();
        float f1 = iicon.getMaxU();
        float f2 = iicon.getMinV();
        float f3 = iicon.getMaxV();
        float f4 = 0.0F;
        float f5 = 0.3F;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef(-f4, -f5, 0.0F);
        float f6 = 1.5F;
        GL11.glScalef(f6, f6, f6);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
        renderItemIn2D(tessellator, f1, f2, f, f3, iicon.getIconWidth(), iicon.getIconHeight(), 0.0625F);

        if (item.hasEffect(pass))
        {
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glDisable(GL11.GL_LIGHTING);
            texturemanager.bindTexture(RES_ITEM_GLINT);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(768, 1, 1, 0);
            float f7 = 0.76F;
            GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPushMatrix();
            float f8 = 0.125F;
            GL11.glScalef(f8, f8, f8);
            float f9 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
            GL11.glTranslatef(f9, 0.0F, 0.0F);
            GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
            renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(f8, f8, f8);
            f9 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
            GL11.glTranslatef(-f9, 0.0F, 0.0F);
            GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
            renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        texturemanager.bindTexture(texturemanager.getResourceLocation(item.getItemSpriteNumber()));
        TextureUtil.func_147945_b();
    }

    private void renderItemInInventory(ItemStack itemStack, int p_77015_4_, int p_77015_5_)
    {
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        int k = itemStack.getItemDamage();
        int pass;
        float f;
        Object object = itemStack.getIconIndex();

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        texturemanager.bindTexture(TextureMap.locationItemsTexture);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(0, 0, 0, 0);
        GL11.glColorMask(false, false, false, true);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(-1);
        tessellator.addVertex((double) (p_77015_4_ - 2), (double) (p_77015_5_ + 18), (double) this.zLevel);
        tessellator.addVertex((double) (p_77015_4_ + 18), (double) (p_77015_5_ + 18), (double) this.zLevel);
        tessellator.addVertex((double) (p_77015_4_ + 18), (double) (p_77015_5_ - 2), (double) this.zLevel);
        tessellator.addVertex((double) (p_77015_4_ - 2), (double) (p_77015_5_ - 2), (double) this.zLevel);
        tessellator.draw();
        GL11.glColorMask(true, true, true, true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        Item item = itemStack.getItem();

        RenderPassesCustom passesCustom = item instanceof RenderPassesCustom ? (RenderPassesCustom) item : null;
        int renderPasses = passesCustom != null ? passesCustom.getRenderPassesCustom(itemStack) : item.getRenderPasses(k);
        for (pass = 0; pass < renderPasses; ++pass)
        {
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            texturemanager.bindTexture(item.getSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);
            IIcon iicon = item.getIcon(itemStack, pass);

            if (renderWithColor)
                setColor(itemStack, true, pass);

            GL11.glDisable(GL11.GL_LIGHTING); //Forge: Make sure that render states are reset, ad renderEffect can derp them up.
            GL11.glEnable(GL11.GL_ALPHA_TEST);

            renderItem.renderIcon(p_77015_4_, p_77015_5_, iicon, 16, 16);

            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_LIGHTING);

            if (renderEffect && itemStack.hasEffect(pass))
            {
                renderItem.renderEffect(texturemanager, p_77015_4_, p_77015_5_);
            }
        }

        GL11.glEnable(GL11.GL_LIGHTING);
    }
}
