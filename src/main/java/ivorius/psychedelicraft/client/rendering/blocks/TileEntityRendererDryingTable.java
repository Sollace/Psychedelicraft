/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;

import java.util.Random;

public class TileEntityRendererDryingTable extends TileEntitySpecialRenderer
{
    public TileEntityRendererDryingTable()
    {

    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f)
    {
        renderTileEntityDryingTableAt((DryingTableBlockEntity) tileentity, d, d1, d2, f);
    }

    public void renderTileEntityDryingTableAt(DryingTableBlockEntity tileEntity, double d, double d1, double d2, float f)
    {
        Tessellator tessellator = Tessellator.instance;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) d, (float) d1 + 0.75f, (float) d2);

        Random random = new Random(tileEntity.xCoord * 100L + tileEntity.yCoord * 10L + tileEntity.zCoord);

        for (int i = 0; i < tileEntity.getSizeInventory(); i++)
        {
            boolean result = i == 0;
            ItemStack stack = tileEntity.getStackInSlot(i);

            if (stack != null)
            {
                float positionX = result ? 0.5f : (0.35f + random.nextFloat() * 0.3f);
                float positionZ = result ? 0.5f : (0.35f + random.nextFloat() * 0.3f);
                float rotation = random.nextFloat() * 360.0f;

                GL11.glPushMatrix();

                GL11.glTranslatef(positionX, i / 500.0f, positionZ);

                GL11.glRotatef(rotation, 0.0f, 1.0f, 0.0f);
                GL11.glScalef(0.25f, 0.25f, 0.25f);
                if (result)
                {
                    GL11.glScalef(1.5f, 1.5f, 1.5f);
                }
                GL11.glTranslatef(0.0f, 0.0f, -0.2f);
                GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);

//				Srsly Mojang
                GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                RenderManager.instance.itemRenderer.renderItem(Minecraft.getMinecraft().thePlayer, stack, 0);

//				EntityItem var3 = new EntityItem(PsychedelicraftMCPH.tileEntityGetWorldObj(tileEntity), 0.0D, 0.0D, 0.0D, stack);
//				var3.getEntityItem().stackSize = 1;
//				var3.hoverStart = 0.0F;
//
//				RenderItem.renderInFrame = true;
//				RenderManager.instance.renderEntityWithPosYaw(var3, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
//				RenderItem.renderInFrame = false;

                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();
    }
}
