package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.ivtoolkit.blocks.IvRotatableBlockRenderHelper;
import ivorius.ivtoolkit.raytracing.IvRaytracer;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TileEntityBottleRack;
import ivorius.psychedelicraft.items.ItemBottle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created by lukas on 16.11.14.
 */
public class TileEntityRendererBottleRack extends TileEntitySpecialRenderer
{
    public static IModelCustom modelBottleRack = AdvancedModelLoader.loadModel(new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathModels + "wineRack.obj"));

    public IModelCustom model;
    public ResourceLocation texture;

    public TileEntityRendererBottleRack()
    {
        model = modelBottleRack;
        texture = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "wineRack.png");
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks)
    {
        TileEntityBottleRack tileEntityBottleRack = (TileEntityBottleRack) tileEntity;

        GL11.glPushMatrix();
        IvRotatableBlockRenderHelper.transformFor(tileEntityBottleRack, x, y, z);
        GL11.glTranslated(0.0f, 0.002f, 0.0f);

        GL11.glPushMatrix();
        GL11.glTranslatef(0f, -.5f, 0f);
        GL11.glColor3f(1f, 1f, 1f);
        this.bindTexture(texture);
        model.renderOnly("RackFrame", "RackBoards");

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        Random r = new Random(4);
        for (int i = 0; i < 9; i++)
        {
            ItemStack bottle = tileEntityBottleRack.getStackInSlot(i);
            if (bottle != null && bottle.getItem() instanceof ItemBottle)
            {
                float[] color = ((ItemBottle) bottle.getItem()).getBottleColor(bottle);

                GL11.glColor3f(color[0], color[1], color[2]);
                model.renderOnly("Bottle_" + i);
            }
        }
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }
}
