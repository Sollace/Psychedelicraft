/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.ivtoolkit.blocks.IvMultiBlockRenderHelper;
import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TileEntityMashTub;
import ivorius.psychedelicraft.client.rendering.FluidBoxRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class TileEntityRendererMashTub extends TileEntitySpecialRenderer
{
    public static IModelCustom modelWoodenVat = AdvancedModelLoader.loadModel(new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathModels + "woodenVat.obj"));
    public static ResourceLocation textureMashTub = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "woodenVat.png");

    public static final float MODEL_SIZE = 15.0f / 16.0f;
    public static final float MODEL_BORDER_WIDTH = 1.0f / 16.0f;
    public static final float MODEL_HEIGHT = 12.0f / 16.0f;

    private IModelCustom model;
    private ResourceLocation texture;

    public TileEntityRendererMashTub()
    {
        model = modelWoodenVat;
        texture = textureMashTub;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f)
    {
        renderTileEntityMashTub((TileEntityMashTub) tileentity, d, d1, d2, f);
    }

    public void renderTileEntityMashTub(TileEntityMashTub tileEntity, double x, double y, double z, float partialTicks)
    {
        if (tileEntity.isParent())
        {
            GL11.glPushMatrix();
            IvMultiBlockRenderHelper.transformFor(tileEntity, x, y, z);
            GL11.glTranslated(0.0f, 0.002f, 0.0f);

            GL11.glPushMatrix();
            GL11.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
            GL11.glTranslatef(0f, -.5f, 0f);
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
            this.bindTexture(texture);
            model.renderAll();
            GL11.glPopMatrix();

            FluidStack fluidStack = tileEntity.tank.getFluid();
            if (fluidStack != null)
            {
                float fluidHeight = (MODEL_HEIGHT - MODEL_BORDER_WIDTH - 1.0f / 16.0f) * IvMathHelper.clamp(0.0f, (float) fluidStack.amount / (float) tileEntity.tank.getCapacity(), 1.0f);

                FluidBoxRenderer fluidBoxRenderer = new FluidBoxRenderer(1.0f);
                fluidBoxRenderer.prepare(fluidStack);

                fluidBoxRenderer.renderFluid(-MODEL_SIZE, -.5f + MODEL_BORDER_WIDTH, -MODEL_SIZE, MODEL_SIZE * 2, fluidHeight, MODEL_SIZE * 2, ForgeDirection.UP);

                fluidBoxRenderer.cleanUp();
            }
            else if (tileEntity.solidContents != null && tileEntity.solidContents.getItem() instanceof ItemBlock)
            {
                float fluidHeight = (MODEL_HEIGHT - MODEL_BORDER_WIDTH - 1.0f / 16.0f);

                FluidBoxRenderer fluidBoxRenderer = new FluidBoxRenderer(1.0f);
                fluidBoxRenderer.prepare(tileEntity.solidContents);

                fluidBoxRenderer.renderFluid(-MODEL_SIZE, -.5f + MODEL_BORDER_WIDTH, -MODEL_SIZE, MODEL_SIZE * 2, fluidHeight, MODEL_SIZE * 2, ForgeDirection.UP);

                fluidBoxRenderer.cleanUp();
            }

            GL11.glPopMatrix();
        }
    }
}
