/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.screen.DryingTableScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.*;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Updated by Sollace on 3 Jan 2023
 */
public class DryingTableScreen extends HandledScreen<DryingTableScreenHandler> {
    public static final Identifier TEXTURE = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "gui_drying_table.png");

    public DryingTableScreen(DryingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        titleX = 26;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int var5 = (width - backgroundWidth) / 2;
        int var6 = (height - backgroundHeight) / 2;

        drawTexture(matrices, var5, var6, 0, 0, backgroundWidth, backgroundHeight);

        if (handler.entity.dryingProgress > 0) {
            drawTexture(matrices, var5 + 88, var6 + 34, 176, 59, 25, 16);
        }

        int var7 = (int)handler.entity.dryingProgress * 24; //Max 24, progress
        drawTexture(matrices, var5 + 88, var6 + 34, 176, 42, var7 + 1, 16);

        int var8 = (int) handler.entity.heatRatio * 20; //Max 20, sun
        drawTexture(matrices, var5 + 148, var6 + 6 + (20 - var8), 176, 21 + (20 - var8), 20, var8);
    }
}
