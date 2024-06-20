package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.MinecraftClient;
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

    public static void drawTank(DrawContext context, Resovoir tank, int x, int y, int width, int height) {
        if (tank.getContents().isEmpty()) {
            return;
        }

        float fluidHeight = MathHelper.clamp((float) tank.getContents().amount() / (float) tank.getCapacity(), 0, 1);
        int fluidHeightPixels = MathHelper.ceil(fluidHeight * height);

        FluidBoxRenderer.FluidAppearance appearance = FluidBoxRenderer.FluidAppearance.of(tank.getContents());

        float[] color = appearance.rgba();

        RenderUtil.drawRepeatingSprite(context, appearance.sprite(), x, y - fluidHeightPixels, width, fluidHeightPixels, color[0], color[1], color[2], color[3]);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void drawTank(DrawContext context, ItemFluids fluids, int capacity, int x, int y, int width, int height) {
        if (fluids.isEmpty()) {
            return;
        }

        float fluidHeight = MathHelper.clamp((float) fluids.amount() / (float) capacity, 0, 1);
        int fluidHeightPixels = MathHelper.ceil(fluidHeight * height);

        FluidBoxRenderer.FluidAppearance appearance = FluidBoxRenderer.FluidAppearance.of(fluids);

        float[] color = appearance.rgba();

        RenderUtil.drawRepeatingSprite(context, appearance.sprite(), x, y + height - fluidHeightPixels, width, fluidHeightPixels, color[0], color[1], color[2], color[3]);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void drawTankTooltip(DrawContext context, Resovoir tank, int x, int y, int width, int height, int mouseX, int mouseY, List<Text> details) {
        drawTankTooltip(context, tank.getContents(), tank.getCapacity(), x, y, width, height, mouseX, mouseY, details);
    }

    public static void drawTankTooltip(DrawContext context, ItemFluids fluids, long capacity, int x, int y, int width, int height, int mouseX, int mouseY, List<Text> details) {
        if (rectContains(mouseX, mouseY, x, y, width, height)) {
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(fluids.getName());
            if (!fluids.isEmpty()) {
                if (capacity <= 0) {
                    tooltip.add(Text.translatable("psychedelicraft.container.amount", FluidVolumes.format(fluids.amount())).formatted(Formatting.GRAY));
                } else {
                    tooltip.add(Text.translatable("psychedelicraft.container.levels", FluidVolumes.format(fluids.amount()), FluidVolumes.format(capacity)).formatted(Formatting.GRAY));
                }
            }
            fluids.appendTooltip(tooltip, TooltipType.BASIC);
            tooltip.addAll(details);
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, mouseX, mouseY);
        }
    }

    public static boolean rectContains(int x, int y, int rectX, int rectY, int width, int height) {
        return x >= rectX
            && y >= rectY
            && x < rectX + width
            && y < rectY + height;
    }
}
