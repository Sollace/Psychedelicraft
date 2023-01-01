package ivorius.psychedelicraft.client.screen;

import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.psychedelicraft.client.rendering.MCColorHelper;
import ivorius.psychedelicraft.fluids.TranslucentFluid;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 13.11.14.
 */
public class GuiFluid extends GuiContainer
{
    public ContainerFluidHandler containerFluidHandler;

    public GuiFluid(ContainerFluidHandler container)
    {
        super(container);
        containerFluidHandler = container;
    }

    public void drawTank(FluidTankInfo tankInfo, int x, int y, int width, int height, float repeatTextureX, float repeatTextureY)
    {
        FluidStack containedFluidStack = containerFluidHandler.fluidHandler.drain(containerFluidHandler.side, tankInfo.capacity, false);
        if (containedFluidStack != null)
        {
            Fluid containedFluid = containedFluidStack.getFluid();
            IIcon icon = containedFluid.getIcon(containedFluidStack);
            float fluidHeight = IvMathHelper.clamp(0.0f, (float) containedFluidStack.amount / (float) tankInfo.capacity, 1.0f);
            int fluidHeightPixels = MathHelper.floor_float(fluidHeight * height + 0.5f);

            float texX0, texX1, texY0, texY1;

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.001f);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            if (icon == null)
            {
                MCColorHelper.setColor(containedFluid.getColor(containedFluidStack), containedFluid instanceof TranslucentFluid);
                texX0 = texX1 = texY0 = texY1 = 0.0f;
                GL11.glDisable(GL11.GL_TEXTURE_2D);
            }
            else
            {
                GL11.glColor3f(1.0f, 1.0f, 1.0f);
                texX0 = icon.getMinU();
                texX1 = icon.getMaxU();
                texY0 = icon.getMinV();
                texY1 = icon.getMaxV();
                mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            }

            drawRepeatingTexture(x, y, width, fluidHeightPixels, texX0, texX1, texY0, texY1, repeatTextureX, repeatTextureY * fluidHeight, true);

            if (icon == null)
                GL11.glEnable(GL11.GL_TEXTURE_2D);

            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    public void drawRepeatingTexture(int x, int y, int width, int height, float texX0, float texX1, float texY0, float texY1, float repeatX, float repeatY, boolean fromBelow)
    {
        Tessellator tessellator = Tessellator.instance;

        if (fromBelow)
        {
            height = -height;
            // Flip for correct vertex order
            x = x + width;
            width = -width;
        }

        tessellator.startDrawingQuads();
        for (int curX = 0; curX < MathHelper.ceiling_float_int(repeatX); curX++)
            for (int curY = 0; curY < MathHelper.ceiling_float_int(repeatY); curY++)
            {
                float curWidthPartial = IvMathHelper.clamp(0.0f, repeatX - curX, 1.0f);
                float curHeightPartial = IvMathHelper.clamp(0.0f, repeatY - curY, 1.0f);

                float curWidth = curWidthPartial * width / repeatX;
                float curHeight = curHeightPartial * height / repeatY;

                float origX = curX * width / repeatX + x;
                float origY = curY * height / repeatY + y;

                float curTexX1 = texX0 + (texX1 - texX0) * curWidthPartial;
                float curTexY1 = texY0 + (texY1 - texY0) * curHeightPartial;

                tessellator.addVertexWithUV(origX, origY, zLevel, texX0, texY0);
                tessellator.addVertexWithUV(origX, origY + curHeight, zLevel, texX0, curTexY1);
                tessellator.addVertexWithUV(origX + curWidth, origY + curHeight, zLevel, curTexX1, curTexY1);
                tessellator.addVertexWithUV(origX + curWidth, origY, zLevel, curTexX1, texY0);
            }

        tessellator.draw();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {

    }

    public void drawTankTooltip(FluidTankInfo tankInfo, int x, int y, int width, int height, int mouseX, int mouseY, List<String> additionalText)
    {
        if (rectContains(mouseX, mouseY, x, y, width, height))
        {
            FluidStack containedFluidStack = containerFluidHandler.fluidHandler.drain(containerFluidHandler.side, tankInfo.capacity, false);

            if (containedFluidStack != null)
            {
                List<String> tooltipList = new ArrayList<>();
                tooltipList.add(containedFluidStack.getLocalizedName());
                tooltipList.add(EnumChatFormatting.GRAY + "Amount: " + containedFluidStack.amount);

                if (additionalText != null)
                    tooltipList.addAll(additionalText);

                drawHoveringText(tooltipList, mouseX, mouseY, fontRendererObj);
            }
        }
    }

    public FluidTankInfo getTankInfo(int index)
    {
        FluidTankInfo[] tankInfos = containerFluidHandler.fluidHandler.getTankInfo(containerFluidHandler.side);

        if (tankInfos != null && tankInfos.length >= index)
            return tankInfos[index];

        return null;
    }

    public boolean rectContains(int x, int y, int rectX, int rectY, int rectWidth, int rectHeight)
    {
        return x >= rectX && y >= rectY && x < rectX + rectWidth && y < rectY + rectHeight;
    }
}
