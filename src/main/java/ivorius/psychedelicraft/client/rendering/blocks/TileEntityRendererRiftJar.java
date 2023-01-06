/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TileEntityRiftJar;
import ivorius.psychedelicraft.client.rendering.bezier.*;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Random;
import java.util.stream.Stream;

public class TileEntityRendererRiftJar implements BlockEntityRenderer<TileEntityRiftJar> {
    private static final IvBezierPath3DRendererText bezierPath3DRendererText = new IvBezierPath3DRendererText().setFont(new Identifier("alt"));
    public static final Identifier texture = Psychedelicraft.id(Psychedelicraft.filePathTextures + "riftJar.png");
    public static final Identifier crackedTexture = Psychedelicraft.id(Psychedelicraft.filePathTextures + "riftJarCracked.png");
    public static final Identifier[] zeroScreenTexture = Stream.iterate(0, i -> i + 1)
            .map(i -> Psychedelicraft.id(Psychedelicraft.filePathTextures + "zeroScreen" + i + ".png"))
            .limit(8)
            .toArray(Identifier[]::new);

    public static final IvBezierPath3D sphereBezierPath = IvBezierPath3DCreator.createSpiraledSphere(3.0, 8.0, 0.2);
    public static final IvBezierPath3D outgoingBezierPath = IvBezierPath3DCreator.createSpiraledBezierPath(0.06, 6.0, 6.0, 1.0, 0.2, 0.0, false);

    private final ModelMystJar model = new ModelMystJar(ModelMystJar.getTexturedModelData().createModel());

    public TileEntityRendererRiftJar(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(TileEntityRiftJar entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        float ticks = entity.ticksAliveVisual + tickDelta;

        matrices.push();
        matrices.translate(0.5F, 0.5f, 0.5F);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getCachedState().get(HorizontalFacingBlock.FACING).asRotation() + 180));

        if (entity.currentRiftFraction > 0) {
            renderZeroInsides(matrices, vertices, tickDelta, ticks, Math.min(entity.currentRiftFraction * 2.0f, 1.0f));
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

                bezierPath3DRendererText.setText(cheeseString("This is a small circle.", 1.0f - connection.fractionUp, new Random(42)));
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



    public void renderZeroInsides(MatrixStack matrices, VertexConsumerProvider vertices, float partialTicks, float ticks, float alpha) {
        int textureChosen = MathHelper.floor(ticks * 0.5f);
        Random thisTextureMov = new Random(textureChosen);

        float pixelsX = 70F;//140F / 2F;
        float pixelsY = 112F;//224F / 2F;

        GL11.glTexCoord2f(
                thisTextureMov.nextInt(10) * 0.1F * pixelsX,
                thisTextureMov.nextInt(8) * 0.125f * pixelsY
        );
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, alpha);

        PSRenderStates.setUseScreenTexCoords(true);
        PSRenderStates.setPixelSize(1F / pixelsX, -1F / pixelsY);
        model.renderInterior(matrices, vertices.getBuffer(model.getLayer(zeroScreenTexture[textureChosen % 8])), 0, 0, 1, 1, 1, alpha);
        RenderSystem.disableBlend();

        PSRenderStates.setScreenSizeDefault();
        PSRenderStates.setUseScreenTexCoords(false);
    }

    public static String cheeseString(String string, float effect, Random rand) {
        if (effect <= 0) {
            return string;
        }

        StringBuilder builder = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            if (rand.nextFloat() <= effect) {
                builder.append(' ');
            } else {
                builder.append(string.charAt(i));
            }
        }

        return builder.toString();
    }
}
