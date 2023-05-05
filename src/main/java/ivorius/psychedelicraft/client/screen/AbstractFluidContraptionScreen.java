package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer.TextureBounds;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

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
        titleY = 10;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        titleX = 83 - textRenderer.getWidth(title) / 2;
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    protected Resovoir getTank() {
        return handler.getTank();
    }

    public void drawTank(Resovoir tank, int x, int y, int width, int height, float repeatTextureX, float repeatTextureY) {
        if (tank.isEmpty()) {
            return;
        }

        SimpleFluid fluid = tank.getFluidType();
        int level = tank.getLevel();

        float fluidHeight = MathHelper.clamp((float) level / (float) tank.getCapacity(), 0, 1);
        int fluidHeightPixels = MathHelper.ceil(fluidHeight * height);

        FluidBoxRenderer.FluidAppearance appearance = FluidBoxRenderer.FluidAppearance.of(fluid, tank.getStack());

        RenderUtil.setColor(appearance.color(), false);
        RenderSystem.setShaderTexture(0, appearance.texture());

        TextureBounds frame = appearance.frame();

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        RenderUtil.drawRepeatingTexture(new MatrixStack(), bufferBuilder,
                x, x + width, y - fluidHeightPixels, y,
                frame.x0(), frame.x1(), frame.y0(), frame.y1(), 2048, 0, 0);
        Tessellator.getInstance().draw();

        RenderSystem.setShaderColor(1, 1, 1, 1);

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public void drawTankTooltip(MatrixStack matrices, Resovoir tank, int x, int y, int width, int height, int mouseX, int mouseY, List<Text> details) {
        if (rectContains(mouseX, mouseY, x, y, width, height)) {
            SimpleFluid fluid = tank.getFluidType();
            int level = tank.getLevel();

            List<Text> tooltip = new ArrayList<>();
            tooltip.add(fluid.getName(tank.getStack()));
            if (!fluid.isEmpty()) {
                tooltip.add(Text.literal("Amount: " + level).formatted(Formatting.GRAY));
            }
            fluid.appendTooltip(tank.getStack(), null, tooltip, TooltipContext.BASIC);
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
