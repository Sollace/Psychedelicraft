/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TileEntityPeyote;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TileEntityRendererPeyote extends TileEntitySpecialRenderer
{
    ModelBase[] peyoteModels = new ModelBase[4];
    ResourceLocation[] blockTextures = new ResourceLocation[4];

    public TileEntityRendererPeyote()
    {
        super();

        this.peyoteModels[0] = new ModelPeyote0();
        this.peyoteModels[1] = new ModelPeyote1();
        this.peyoteModels[2] = new ModelPeyote2();
        this.peyoteModels[3] = new ModelPeyote3();

        blockTextures[0] = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "peyoteTexture0.png");
        blockTextures[1] = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "peyoteTexture1.png");
        blockTextures[2] = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "peyoteTexture2.png");
        blockTextures[3] = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "peyoteTexture3.png");
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f)
    {
        renderTileEntityPeyoteAt((TileEntityPeyote) tileentity, d, d1, d2, f);
    }

    public void renderTileEntityPeyoteAt(TileEntityPeyote tileEntity, double d, double d1, double d2, float f)
    {
        Tessellator tessellator = Tessellator.instance;

        int m = tileEntity.getBlockMetadata();
        if (m > 3)
        {
            m = 3;
        }

        bindTexture(blockTextures[m]);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5f, (float) d2 + 0.5F);
        GL11.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
        float uniqueRotation = tileEntity.xCoord * 18249.849231f + tileEntity.yCoord * 892308.237542f + tileEntity.zCoord * 4387598.23842f;
        GL11.glRotatef(uniqueRotation % 360.0f, 0.0f, 1.0f, 0.0f);

        peyoteModels[m].render(null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        GL11.glPopMatrix();
    }
}
