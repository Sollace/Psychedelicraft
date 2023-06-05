/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.screen.DryingTableScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.*;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Updated by Sollace on 3 Jan 2023
 */
public class DryingTableScreen extends HandledScreen<DryingTableScreenHandler> {
    public static final Identifier TEXTURE = Psychedelicraft.id("textures/gui/drying_table.png");

    public DryingTableScreen(DryingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        titleX = 26;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        int centerX = (width - backgroundWidth) / 2;
        int centerY = (height - backgroundHeight) / 2;

        context.drawTexture(TEXTURE, centerX, centerY, 0, 0, backgroundWidth, backgroundHeight);

        if (handler.getProgress() > 0) {
            context.drawTexture(TEXTURE, centerX + 88, centerY + 34, 176, 59, 25, 16);
        }

        int progress = (int)(handler.getProgress() * 24); //Max 24, progress
        context.drawTexture(TEXTURE, centerX + 88, centerY + 34, 176, 42, progress + 1, 16);

        int heat = (int) (handler.getHeatRatio() * 20); //Max 20, sun
        context.drawTexture(TEXTURE, centerX + 148, centerY + 6 + (20 - heat), 176, 21 + (20 - heat), 20, heat);
    }
}
