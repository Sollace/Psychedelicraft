package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.fluid.Resovoir;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 13.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public abstract class AbstractFluidContraptionScreen<T extends FluidContraptionScreenHandler<?>> extends HandledScreen<T> {

    protected AbstractFluidContraptionScreen(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    protected Resovoir getTank() {
        return handler.getTank();
    }

    public void drawTank(Resovoir tank, int x, int y, int width, int height, float repeatTextureX, float repeatTextureY) {
        SimpleFluid fluid = tank.getFluidType();

        float fluidHeight = MathHelper.clamp((float) tank.getLevel() / (float) tank.getCapacity(), 0, 1);
        int fluidHeightPixels = MathHelper.floor(fluidHeight * height + 0.5f);

        RenderSystem.enableBlend();
        // GL11.glAlphaFunc(GL11.GL_GREATER, 0.001f);
        RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ZERO);

        int color = fluid.getTranslucentColor(tank.getStack());

        RenderSystem.setShaderColor(NativeImage.getRed(color), NativeImage.getGreen(color), NativeImage.getBlue(color), NativeImage.getAlpha(color));
        RenderSystem.setShaderTexture(0, fluid.getSymbol());

        drawRepeatingTexture(x, y, width, fluidHeightPixels, 0, 0, 16, 16, repeatTextureX, repeatTextureY * fluidHeight, true);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public void drawRepeatingTexture(int x, int y, int width, int height, float u0, float u1, float v0, float v1, float repeatX, float repeatY, boolean fromBelow) {
        drawRepeatingTexture(x, y, getZOffset(), width, height, u0, u1, v0, v1, repeatX, repeatY, fromBelow);
    }

    public static void drawRepeatingTexture(int x, int y, int z, int width, int height, float u0, float u1, float v0, float v1, float repeatX, float repeatY, boolean fromBelow) {
        Tessellator tessellator = Tessellator.getInstance();

        if (fromBelow) {
            height = -height;
            // Flip for correct vertex order
            x = x + width;
            width = -width;
        }

        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        for (int curX = 0; curX < MathHelper.ceil(repeatX); curX++) {
            for (int curY = 0; curY < MathHelper.ceil(repeatY); curY++) {
                float curWidthPartial = MathHelper.clamp(repeatX - curX, 0, 1);
                float curHeightPartial = MathHelper.clamp(repeatY - curY, 0, 1);

                float curWidth = curWidthPartial * width / repeatX;
                float curHeight = curHeightPartial * height / repeatY;

                float origX = curX * width / repeatX + x;
                float origY = curY * height / repeatY + y;

                float curTexX1 = u0 + (u1 - u0) * curWidthPartial;
                float curTexY1 = v0 + (v1 - v0) * curHeightPartial;

                buffer.vertex(origX, origY, z).texture(u0, v0).next();
                buffer.vertex(origX, origY + curHeight, z).texture(u0, curTexY1).next();
                buffer.vertex(origX + curWidth, origY + curHeight, z).texture(curTexX1, curTexY1).next();
                buffer.vertex(origX + curWidth, origY, z).texture(curTexX1, v0).next();
            }
        }

        tessellator.draw();
    }

    public void drawTankTooltip(MatrixStack matrices, Resovoir tank, int x, int y, int width, int height, int mouseX, int mouseY, List<Text> details) {
        if (rectContains(mouseX, mouseY, x, y, width, height)) {
            SimpleFluid fluid = tank.getFluidType();
            int level = tank.getLevel();

            List<Text> tooltip = new ArrayList<>();
            tooltip.add(fluid.getName(tank.getStack()));
            tooltip.add(Text.literal("Amount: " + level).formatted(Formatting.GRAY));
            tooltip.addAll(details);
            renderTooltip(matrices, tooltip, mouseX, mouseY);
        }
    }

    public static boolean rectContains(int x, int y, int rectX, int rectY, int width, int height) {
        return x >= rectX
            && y >= rectY
            && x < rectX + width
            && y < rectY + height;
    }
}
