package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
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
        int fluidHeightPixels = MathHelper.floor(fluidHeight * height + 0.5f);

        Identifier texture = fluid.getStationaryTexture();

        if (fluid.getFluidState(0).isIn(FluidTags.WATER)) {
            RenderUtil.setColor(BiomeColors.getWaterColor(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getBlockPos()), false);
        }

        if (MinecraftClient.getInstance().getResourceManager().getResource(texture).isEmpty()) {
            RenderSystem.enableBlend();
            RenderUtil.setColor(fluid.getColor(tank.getStack()), false);
            texture = new Identifier("textures/block/water_still.png");
        }

        RenderSystem.setShaderTexture(0, texture);

        int frameSize = 32;
        int frameCount = 32;
        int ticks = (MinecraftClient.getInstance().player.age % frameCount);
        DrawableHelper.drawTexture(new MatrixStack(), x, y - fluidHeightPixels, 0, 0, ticks * frameSize, width, fluidHeightPixels, frameSize, frameSize * frameCount);

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
