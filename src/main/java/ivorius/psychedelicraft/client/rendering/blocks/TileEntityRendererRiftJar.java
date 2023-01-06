/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TileEntityRiftJar;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Random;

public class TileEntityRendererRiftJar implements BlockEntityRenderer<TileEntityRiftJar> {
    public ModelMystJar model = new ModelMystJar();
    public static final Identifier texture = Psychedelicraft.id(Psychedelicraft.filePathTextures + "riftJar.png");
    public static final Identifier crackedTexture = Psychedelicraft.id(Psychedelicraft.filePathTextures + "riftJarCracked.png");

    public static final Identifier[] zeroScreenTexture;

    private static final Identifier GALACTIC_FONT_ID = new Identifier("minecraft", "alt");
    private static final Style GALACTIC_STYLE = Style.EMPTY.withFont(GALACTIC_FONT_ID);

    static {
        zeroScreenTexture = new Identifier[8];
        for (int i = 0; i < zeroScreenTexture.length; i++) {
            zeroScreenTexture[i] = Psychedelicraft.id(Psychedelicraft.filePathTextures + "zeroScreen" + i + ".png");
        }
    }

    public IvBezierPath3D sphereBezierPath = IvBezierPath3DCreator.createSpiraledSphere(3.0, 8.0, 0.2);
    public IvBezierPath3D outgoingBezierPath = IvBezierPath3DCreator.createSpiraledBezierPath(0.06, 6.0, 6.0, 1.0, 0.2, 0.0, false);

    public IvBezierPath3DRendererText bezierPath3DRendererText = new IvBezierPath3DRendererText();

    public TileEntityRendererRiftJar(BlockEntityRendererFactory.Context context) {
        bezierPath3DRendererText.setFontRenderer(MinecraftClient.getInstance().textRenderer);
    }

    @Override
    public void render(TileEntityRiftJar entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        float ticks = entity.ticksAliveVisual + tickDelta;

        matrices.push();
        matrices.translate(0.5F, 0.5f, 0.5F);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getCachedState().get(HorizontalFacingBlock.FACING).asRotation() + 180));

        if (entity.currentRiftFraction > 0) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            renderZeroInsides(tickDelta, ticks, Math.min(entity.currentRiftFraction * 2.0f, 1.0f));
            RenderSystem.disableBlend();
        }

        matrices.translate(0, 1, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        model.setAngles(entity, tickDelta);
        model.render(matrices, vertices.getBuffer(model.getLayer(texture)), light, overlay, 1, 1, 1, 1);

        float crackedVisibility = Math.min((entity.currentRiftFraction - 0.5f) * 2, 1);

        if (crackedVisibility > 0) {
            RenderSystem.setShaderColor(1, 1, 1, crackedVisibility);
            model.render(matrices, vertices.getBuffer(model.getLayer(crackedTexture)), light, overlay, 1, 1, 1, crackedVisibility);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        matrices.pop();
        matrices.pop();

        matrices.push();
        matrices.translate(0.5F, 0.5f, 0.5F);
        RenderSystem.disableCull();
        GL11.glDisable(GL11.GL_CULL_FACE);

        Vec3d jarPosition = entity.getPos().toCenterPos();

        for (TileEntityRiftJar.JarRiftConnection connection : entity.getConnections()) {
            if (connection.bezierPath3D == null) {
                connection.bezierPath3D = IvBezierPath3DCreator.createSpiraledBezierPath(0.1, 0.5, 8.0, new double[] {
                        connection.position.x - jarPosition.x,
                        connection.position.y - (jarPosition.y + 0.1F),
                        connection.position.z - jarPosition.z
                }, 0.2, 0.0, false);
            }

            bezierPath3DRendererText.setText("This is a small spiral.");
            bezierPath3DRendererText.setSpreadToFill(true);
            bezierPath3DRendererText.setShift(ticks * -0.002);
            bezierPath3DRendererText.setInwards(false);
            bezierPath3DRendererText.setCapBottom(0);
            bezierPath3DRendererText.setCapTop(connection.fractionUp);

            bezierPath3DRendererText.render(connection.bezierPath3D);

            if (connection.fractionUp > 0) {
                matrices.push();

                matrices.translate(connection.position.x - jarPosition.x, connection.position.y - (jarPosition.y + 0.1), connection.position.z - jarPosition.z);

                bezierPath3DRendererText.setText(IvStringHelper.cheeseString("This is a small circle.", 1.0f - connection.fractionUp, 42));
                bezierPath3DRendererText.setSpreadToFill(true);
                bezierPath3DRendererText.setShift(ticks * -0.002F);
                bezierPath3DRendererText.setInwards(false);
                bezierPath3DRendererText.setCapBottom(0);
                bezierPath3DRendererText.setCapTop(1);

                bezierPath3DRendererText.render(sphereBezierPath);

                matrices.pop();
            }
        }

        float outgoingStrength = entity.fractionHandleUp * entity.fractionOpen;
        if (outgoingStrength > 0) {
            bezierPath3DRendererText.setText("This is a small spiral.");
            bezierPath3DRendererText.setSpreadToFill(true);
            bezierPath3DRendererText.setShift(ticks * 0.002);
            bezierPath3DRendererText.setInwards(false);
            bezierPath3DRendererText.setCapBottom(0);
            bezierPath3DRendererText.setCapTop(outgoingStrength);

            bezierPath3DRendererText.render(outgoingBezierPath);
        }

        RenderSystem.enableCull();
        matrices.pop();
    }

    public void renderZeroInsides(float partialTicks, float ticks, float alpha) {
        int textureChosen = MathHelper.floor_double(ticks * 0.5f);
        Random thisTextureMov = new Random(textureChosen);
        bindTexture(zeroScreenTexture[textureChosen % 8]);
        PSRenderStates.setUseScreenTexCoords(true);
        float pixelsX = 140.0f / 2.0f;
        float pixelsY = 224.0f / 2.0f;
        PSRenderStates.setPixelSize(1.0f / pixelsX, -1.0f / pixelsY);
        GL11.glTexCoord2f(thisTextureMov.nextInt(10) * 0.1f * pixelsX, thisTextureMov.nextInt(8) * 0.125f * pixelsY);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        renderJarInside(partialTicks, alpha);
        PSRenderStates.setScreenSizeDefault();
        PSRenderStates.setUseScreenTexCoords(false);
    }

    public void renderJarInside(float partialTicks, float alpha) {
        Tessellator tessellator = Tessellator.instance;

        float in = 0.001f;
        float in2 = in * 2.0f;

        tessellator.startDrawingQuads();
        IvRenderHelper.drawModelCuboid(tessellator, -4.0f + in, 0.0f + in, -4.0f + in, 8.0f - in2, 5.0f - in2, 8.0f - in2);
        IvRenderHelper.drawModelCuboid(tessellator, -3.0f + in, 5.0f - in, -3.0f + in, 6.0f - in2, 2.0f + in2, 6.0f - in2);
        IvRenderHelper.drawModelCuboid(tessellator, -4.0f + in, 7.0f + in, -4.0f + in, 8.0f - in2, 5.0f - in2, 8.0f - in2);
        tessellator.draw();
    }
}
