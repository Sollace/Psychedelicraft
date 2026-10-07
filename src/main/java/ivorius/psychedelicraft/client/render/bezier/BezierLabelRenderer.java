package ivorius.psychedelicraft.client.render.bezier;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import net.minecraft.client.resource.language.ReorderingUtil;
import net.minecraft.text.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BezierLabelRenderer {
    public static final BezierLabelRenderer INSTANCE = new BezierLabelRenderer();

    private final float scale = -1/12F;

    public BezierLabelRenderState updateRenderState(BezierLabelRenderState state, Bezier bezier, Style style, Text text) {
        final float totalLength = text.getString().length();
        final Path path = bezier.getPath();
        state.length = 0;
        state.steps = new ArrayList<>();
        ReorderingUtil.reorder(text, !style.inwards).accept((charIndex, charStyle, character) -> {
            int i = state.steps.size();
            if (character != ' ') {
                double totalProgress = (style.spread ? (i / totalLength) : (i * 0.5)) + style.shift;
                double finalProgress = ((totalProgress % 1) + 1) % 1;

                if (finalProgress >= style.bottomCap && finalProgress <= style.capTop) {
                    Path.Intermediate step = path.getStep(finalProgress);
                    Vector3d position = step.position();
                    Vector3d rotation = path.getNaturalRotation(step, 0.01);

                    float textSize = scale * step.fontSize();

                    @Nullable TextColor color = charStyle.getColor();

                    state.steps.add(new BezierLabelRenderState.Step(new Vec3d(position.x, position.y, position.z), textSize, new Quaternionf()
                            .rotateY(((float)rotation.x + (style.inwards ? 0 : 180)) * MathHelper.RADIANS_PER_DEGREE)
                            .rotateX((float)rotation.y * MathHelper.RADIANS_PER_DEGREE), charIndex, charStyle, character,
                            color == null ? 0xFFFFFFFF : color.getRgb()));
                }
            }
            state.length++;
            return true;
        });
        return state;
    }

    public static class Style {
        private float capTop;
        private float bottomCap;
        private boolean inwards;
        private boolean spread;
        private float shift;

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
