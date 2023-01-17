/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.widget.ButtonWidget;
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
public class FlaskScreen<T extends BlockEntity & BlockWithFluid.DirectionalFluidResovoir> extends AbstractFluidContraptionScreen<FluidContraptionScreenHandler<T>> {
    public static final Identifier BACKGROUND = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "container_fluid.png");

    public ButtonWidget changeTransferButton;

    public FlaskScreen(FluidContraptionScreenHandler<T> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        initTransferButton();
    }

    protected void initTransferButton() {
        int baseX = (width - backgroundWidth) / 2;
        int baseY = (height - backgroundHeight) / 2;
        addDrawableChild(changeTransferButton = ButtonWidget.builder(Text.empty(), this::toggleTransfer).dimensions(baseX + 7, baseY + 60, 50, 20).build());
        updateTransferButtonTitle();
    }

    protected void toggleTransfer(ButtonWidget sender) {
        handler.currentlyDrainingItem = !handler.currentlyDrainingItem;
        updateTransferButtonTitle();

        handler.sendContentUpdates();

        //this.mc.playerController.sendEnchantPacket(handler.syncId, handler.currentlyDrainingItem ? 1 : 0);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, getBackgroundTexture());
        int baseX = (width - backgroundWidth) / 2;
        int baseY = (height - backgroundHeight) / 2;
        drawTexture(matrices, baseX, baseY, 0, 0, backgroundWidth, backgroundHeight);
        drawAdditionalInfo(matrices, baseX, baseY);
        drawTanks(matrices, baseX, baseY);
    }

    protected void drawAdditionalInfo(MatrixStack matrices, int baseX, int baseY) {

    }

    protected void drawTanks(MatrixStack matrices, int baseX, int baseY) {
        drawTank(getTank(), baseX + 60, baseY + 14 + 57, 108, 57, 4.0f, 2.1111f);
    }

    protected Identifier getBackgroundTexture() {
        return BACKGROUND;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        super.render(matrices, mouseX, mouseY, tickDelta);
        int baseX = (width - backgroundWidth) / 2;
        int baseY = (height - backgroundHeight) / 2;
        drawTankTooltips(matrices, mouseX, mouseY, baseX, baseY);
    }

    protected void drawTankTooltips(MatrixStack matrices, int mouseX, int mouseY, int baseX, int baseY) {
        drawTankTooltip(matrices, getTank(), baseX + 60, baseY + 14, 108, 57, mouseX, mouseY, getAdditionalTankText());
    }

    protected List<Text> getAdditionalTankText() {
        return List.of();
    }

    public void updateTransferButtonTitle() {
        changeTransferButton.setMessage(handler.currentlyDrainingItem ? Text.literal("Drain") : Text.literal("Fill"));
    }
}
