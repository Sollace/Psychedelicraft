package ivorius.psychedelicraft.client.render.bezier;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.resource.language.ReorderingUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.math.RotationAxis;

public class BezierLabelRenderer {
    public static final BezierLabelRenderer INSTANCE = new BezierLabelRenderer();

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private float length;
    private int i;

    private final float scale = -1/12F;

    private int activeIndex;
    private int activeCodePoint;
    private net.minecraft.text.Style activeStyle;
    private final OrderedText singleCharOrderedText = visitor -> {
        return visitor.accept(activeIndex, activeStyle, activeCodePoint);
    };

    public void render(MatrixStack matrices, Bezier bezier, Style style, Text text) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        length = text.getString().length();
        i = 0;
        Path path = bezier.getPath();
        ReorderingUtil.reorder(text, !style.inwards).accept((charIndex, charStyle, character) -> {
            if (character != ' ') {
                double totalProgress = (style.spread ? (i / length) : (i * 0.5)) + style.shift;
                double finalProgress = ((totalProgress % 1) + 1) % 1;

                if (finalProgress >= style.bottomCap && finalProgress <= style.capTop) {
                    Path.Intermediate step = path.getStep(finalProgress);
                    Vector3d position = step.position();
                    Vector3d rotation = path.getNaturalRotation(step, 0.01);

                    float textSize = scale * step.fontSize();

                    matrices.push();
                    matrices.translate(position.x, position.y, position.z);
                    matrices.scale(textSize, textSize, textSize);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)rotation.x + (style.inwards ? 0 : 180)));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float)rotation.y));

                    activeIndex = charIndex;
                    activeStyle = charStyle;
                    activeCodePoint = character;

                    @Nullable TextColor color = charStyle.getColor();
                    textRenderer.draw(matrices, singleCharOrderedText, 0, 0, color == null ? 0xFFFFFFFF : color.getRgb());
                    matrices.pop();
                }
            }
            i++;
            return true;
        });

        RenderSystem.disableBlend();
    }

    public static class Style {
        float capTop;
        float bottomCap;
        boolean inwards;
        boolean spread;
        float shift;

        public Style spread(boolean spread) {
            this.spread = spread;
            return this;
        }

        public Style shift(float shift) {
            this.shift = shift;
            return this;
        }

        public Style inwards(boolean inwards) {
            this.inwards = inwards;
            return this;
        }

        public Style bottomCap(float capBottom) {
            this.bottomCap = capBottom;
            return this;
        }

        public Style topCap(float capTop) {
            this.capTop = capTop;
            return this;
        }
    }
}
