/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.blocks.TileEntityDryingTable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class GuiDryingTable extends GuiContainer
{
    public static Identifier dryingTableBG = Psychedelicraft.id(Psychedelicraft.filePathTextures + "guiDryingTable.png");

    public GuiDryingTable(InventoryPlayer par1InventoryPlayer, World par2World, TileEntityDryingTable tileEntityDryingTable)
    {
        super(new ContainerDryingTable(par1InventoryPlayer, par2World, tileEntityDryingTable));
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString("Drying Table", 28, 6, 4210752);
        this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(dryingTableBG);

        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);

        if (((ContainerDryingTable) this.inventorySlots).tileEntityDryingTable.dryingProgress > 0.0f)
        {
            this.drawTexturedModalRect(var5 + 88, var6 + 34, 176, 59, 25, 16);
        }

        int var7 = (int) (((ContainerDryingTable) this.inventorySlots).tileEntityDryingTable.dryingProgress * 24f); //Max 24, progress
        this.drawTexturedModalRect(var5 + 88, var6 + 34, 176, 42, var7 + 1, 16);

        int var8 = (int) (((ContainerDryingTable) this.inventorySlots).tileEntityDryingTable.heatRatio * 20f); //Max 20, sun
        this.drawTexturedModalRect(var5 + 148, var6 + 6 + (20 - var8), 176, 21 + (20 - var8), 20, var8);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

}
