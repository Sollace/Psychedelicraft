package ivorius.psychedelicraft.client.render.bezier;

import java.util.List;

import org.joml.Quaternionf;

import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.math.Vec3d;

public class BezierLabelRenderState {
    int length;
    List<Step> steps = List.of();

    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light) {
        steps.forEach(step -> {
            matrices.push();
            matrices.translate(step.offset());
            matrices.scale(step.scale(), step.scale(), step.scale());
            matrices.multiply(step.rotation());
            queue.submitText(matrices, 0, 0, step, false, TextLayerType.SEE_THROUGH, light, step.color(), 0, 0);
            matrices.pop();
        });
    }

    public record Step(Vec3d offset, float scale, Quaternionf rotation, int charIndex, Style charStyle, int codepoint, int color) implements OrderedText {
        @Override
        public boolean accept(CharacterVisitor visitor) {
            return visitor.accept(charIndex, charStyle, codepoint);
        }
    }
}
