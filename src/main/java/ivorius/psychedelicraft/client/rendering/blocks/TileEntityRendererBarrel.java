/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.fluids.FluidWithIconSymbol;
import ivorius.psychedelicraft.fluids.FluidWithIconSymbolRegistering;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class TileEntityRendererBarrel implements BlockEntityRenderer<BarrelBlockEntity>
{
    private ModelBarrel barrelModel;

    private ResourceLocation barrelTexture;
    private ResourceLocation barrelTextureSpruce;
    private ResourceLocation barrelTextureBirch;
    private ResourceLocation barrelTextureJungle;
    private ResourceLocation barrelTextureAcacia;
    private ResourceLocation barrelTextureDarkOak;

    public TileEntityRendererBarrel(BlockEntityRendererFactory.Context context)
    {
        this.barrelModel = new ModelBarrel();
        this.barrelTexture = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "barrelTexture.png");
        this.barrelTextureSpruce = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "barrelTextureSpruce.png");
        this.barrelTextureBirch = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "barrelTextureBirch.png");
        this.barrelTextureJungle = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "barrelTextureJungle.png");
        this.barrelTextureAcacia = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "barrelTextureAcacia.png");
        this.barrelTextureDarkOak = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "barrelTextureDarkOak.png");
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f)
    {
        renderTileEntityStatueAt((BarrelBlockEntity) tileentity, d, d1, d2, f);
    }

    public void renderTileEntityStatueAt(BarrelBlockEntity tileEntity, double d, double d1, double d2, float f)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 0.5f, (float) d2 + 0.5F);
        GL11.glRotatef(-90.0f * tileEntity.getBlockRotation() + 180.0f, 0.0f, 1.0f, 0.0f);

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, 1.0f, 0.0f);
        GL11.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);

        Entity emptyEntity = new EntityArrow(tileEntity.getWorldObj());
        emptyEntity.ticksExisted = (int) (tileEntity.getTapRotation() * 100.0f);

        this.bindTexture(getBarrelTexture(tileEntity));
        barrelModel.render(emptyEntity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        GL11.glPopMatrix();

        FluidStack containedFluid = tileEntity.containedFluid();
        if (containedFluid != null && containedFluid.getFluid() instanceof FluidWithIconSymbol)
        {
            IIcon drinkIcon = ((FluidWithIconSymbol) containedFluid.getFluid()).getIconSymbol(containedFluid, FluidWithIconSymbolRegistering.TEXTURE_TYPE_ITEM);
            if (drinkIcon != null)
            {
                this.bindTexture(TextureMap.locationItemsTexture);
                Tessellator tessellator = Tessellator.instance;

                double barrelZ = -0.45;
                double iconSize = 1.0;
                double centerX = 0.0;
                double centerY = 0.0;

                GL11.glColor3f(1.0f, 1.0f, 1.0f);
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0f, 0.0f, -1.0f);
                tessellator.addVertexWithUV(centerX - iconSize * 0.5, centerY - iconSize * 0.5, barrelZ, drinkIcon.getMaxU(), drinkIcon.getMaxV());
                tessellator.addVertexWithUV(centerX - iconSize * 0.5, centerY + iconSize * 0.5, barrelZ, drinkIcon.getMaxU(), drinkIcon.getMinV());
                tessellator.addVertexWithUV(centerX + iconSize * 0.5, centerY + iconSize * 0.5, barrelZ, drinkIcon.getMinU(), drinkIcon.getMinV());
                tessellator.addVertexWithUV(centerX + iconSize * 0.5, centerY - iconSize * 0.5, barrelZ, drinkIcon.getMinU(), drinkIcon.getMaxV());
                tessellator.draw();
            }
        }

        GL11.glPopMatrix();
    }

    public ResourceLocation getBarrelTexture(BarrelBlockEntity barrel)
    {
        switch (barrel.barrelWoodType)
        {
            case 1:
                return barrelTextureSpruce;
            case 2:
                return barrelTextureBirch;
            case 3:
                return barrelTextureJungle;
            case 4:
                return barrelTextureAcacia;
            case 5:
                return barrelTextureDarkOak;
            default:
                return barrelTexture;
        }
    }
}
