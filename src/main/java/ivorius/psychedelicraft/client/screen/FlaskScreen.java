/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.List;

/**
 * Created by lukas on 26.10.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class FlaskScreen<T extends FlaskBlockEntity> extends AbstractFluidContraptionScreen<FluidContraptionScreenHandler<T>> {
    private final Identifier background;

    public FlaskScreen(FluidContraptionScreenHandler<T> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.background = Psychedelicraft.id("textures/gui/" + BlockEntityType.getId(handler.getBlockEntity().getType()).getPath() + ".png");
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
        int baseX = (width - backgroundWidth) / 2;
        int baseY = (height - backgroundHeight) / 2;
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, background);
        drawTexture(matrices, baseX + 30, baseY + 20, 0, backgroundHeight, 110, 50);

        drawTanks(matrices, baseX, baseY);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, background);

        drawTexture(matrices, baseX, baseY, 0, 0, backgroundWidth, backgroundHeight);
        drawAdditionalInfo(matrices, baseX, baseY);

        RenderSystem.setShaderColor(1, 1, 1, 1);

        float inputProgress = handler.getBlockEntity().inputSlot.getProgress();
        if (inputProgress > 0 && inputProgress < 1) {
            int width = (int)(45 * inputProgress);
            drawTexture(matrices, baseX + 20 + width, baseY + 40, 176 + width, 0, 45 - width, 16);
        }
        float outputProgress = handler.getBlockEntity().outputSlot.getProgress();
        if (outputProgress > 0 && outputProgress < 1) {
            drawTexture(matrices, baseX + 68, baseY + 60, 176, 17, (int)(53 * outputProgress), 20);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    protected void drawAdditionalInfo(MatrixStack matrices, int baseX, int baseY) {

    }

    protected void drawTanks(MatrixStack matrices, int baseX, int baseY) {
        drawTank(getTank(), baseX + 48, baseY + 59, 64, 27, 4.0f, 2.1111f);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        super.render(matrices, mouseX, mouseY, tickDelta);
        int baseX = (width - backgroundWidth) / 2;
        int baseY = (height - backgroundHeight) / 2;
        drawTankTooltips(matrices, mouseX, mouseY, baseX, baseY);
    }

    protected void drawTankTooltips(MatrixStack matrices, int mouseX, int mouseY, int baseX, int baseY) {
        drawTankTooltip(matrices, getTank(), baseX + 65, baseY + 33, 40, 30, mouseX, mouseY, getAdditionalTankText());
    }

    protected List<Text> getAdditionalTankText() {
        return List.of();
    }
}
