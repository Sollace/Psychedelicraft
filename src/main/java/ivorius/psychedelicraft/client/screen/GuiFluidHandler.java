/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Created by lukas on 26.10.14.
 */
public class GuiFluidHandler extends GuiFluid
{
    public static final ResourceLocation bgTexture = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "container_fluid.png");

    public GuiButton changeTransferButton;

    public GuiFluidHandler(InventoryPlayer inventoryPlayer, TileEntity tileEntity, IFluidHandler fluidHandler, ForgeDirection side)
    {
        super(new ContainerFluidHandler(inventoryPlayer, tileEntity, fluidHandler, side));
        containerFluidHandler = (ContainerFluidHandler) inventorySlots;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        initTransferButton();
    }

    protected void initTransferButton()
    {
        int baseX = (this.width - this.xSize) / 2;
        int baseY = (this.height - this.ySize) / 2;

        buttonList.add(changeTransferButton = new GuiButton(0, baseX + 7, baseY + 60, 50, 20, ""));
        updateTransferButtonTitle();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(getBackgroundTexture());
        int baseX = (this.width - this.xSize) / 2;
        int baseY = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(baseX, baseY, 0, 0, this.xSize, this.ySize);

        drawAdditionalInfo(baseX, baseY);
        drawTanks(baseX, baseY);
    }

    protected void drawAdditionalInfo(int baseX, int baseY)
    {

    }

    protected void drawTanks(int baseX, int baseY)
    {
        FluidTankInfo tankInfo = getTankInfo(0);
        if (tankInfo != null)
            drawTank(tankInfo, baseX + 60, baseY + 14 + 57, 108, 57, 4.0f, 2.1111f);
    }

    protected ResourceLocation getBackgroundTexture()
    {
        return bgTexture;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int baseX = (this.width - this.xSize) / 2;
        int baseY = (this.height - this.ySize) / 2;

        drawTankTooltips(mouseX, mouseY, baseX, baseY);
    }

    protected void drawTankTooltips(int mouseX, int mouseY, int baseX, int baseY)
    {
        FluidTankInfo tankInfo = getTankInfo(0);
        if (tankInfo != null)
            drawTankTooltip(tankInfo, baseX + 60, baseY + 14, 108, 57, mouseX, mouseY, getAdditionalTankText());
    }

    protected List<String> getAdditionalTankText()
    {
        return null;
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button == changeTransferButton)
        {
            containerFluidHandler.currentlyDrainingItem = !containerFluidHandler.currentlyDrainingItem;
            updateTransferButtonTitle();

            this.mc.playerController.sendEnchantPacket(this.containerFluidHandler.windowId, containerFluidHandler.currentlyDrainingItem ? 1 : 0);
        }
    }

    public void updateTransferButtonTitle()
    {
        changeTransferButton.displayString = containerFluidHandler.currentlyDrainingItem ? "Drain" : "Fill";
    }
}
