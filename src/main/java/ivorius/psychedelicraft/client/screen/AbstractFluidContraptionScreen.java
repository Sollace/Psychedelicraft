package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.tooltip.TooltipType;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        titleX = 83 - textRenderer.getWidth(title) / 2;
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    protected Resovoir getTank() {
        return handler.getTank();
    }

    public void drawTank(DrawContext context, Resovoir tank, int x, int y, int width, int height) {
        if (tank.getContents().isEmpty()) {
            return;
        }

        float fluidHeight = MathHelper.clamp((float) tank.getContents().amount() / (float) tank.getCapacity(), 0, 1);
        int fluidHeightPixels = MathHelper.ceil(fluidHeight * height);

        FluidBoxRenderer.FluidAppearance appearance = FluidBoxRenderer.FluidAppearance.of(tank.getContents());

        float[] color = appearance.rgba();

        RenderUtil.drawRepeatingSprite(context, appearance.sprite(), x, y - fluidHeightPixels, width, fluidHeightPixels, color[0], color[1], color[2], 1);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public void drawTankTooltip(DrawContext context, Resovoir tank, int x, int y, int width, int height, int mouseX, int mouseY, List<Text> details) {
        if (rectContains(mouseX, mouseY, x, y, width, height)) {
            SimpleFluid fluid = tank.getContents().fluid();
            int level = tank.getContents().amount();

            List<Text> tooltip = new ArrayList<>();
            tooltip.add(fluid.getName(tank.getContents()));
            if (!fluid.isEmpty()) {
                tooltip.add(Text.translatable("psychedelicraft.container.levels", level, tank.getCapacity()).formatted(Formatting.GRAY));
            }
            fluid.appendTooltip(tank.getContents(), tooltip, TooltipType.BASIC);
            tooltip.addAll(details);
            context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
        }
    }

    public static boolean rectContains(int x, int y, int rectX, int rectY, int width, int height) {
        return x >= rectX
            && y >= rectY
            && x < rectX + width
            && y < rectY + height;
    }
}
