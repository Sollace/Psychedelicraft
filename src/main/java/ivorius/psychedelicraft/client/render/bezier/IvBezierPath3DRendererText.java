package ivorius.psychedelicraft.client.render.bezier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IvBezierPath3DRendererText {
    @SuppressWarnings("unused")
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;

    public IvBezierPath3DRendererText setFont(Identifier font) {
        return this;
    }

    public IvBezierPath3DRendererText setText(String text) {
        return setText(Text.literal(text));
    }

    public IvBezierPath3DRendererText setText(Text text) {
        return this;
    }

    public IvBezierPath3DRendererText setSpreadToFill(boolean spreadToFill) {
        return this;
    }

    public IvBezierPath3DRendererText setShift(double d) {
        return this;
    }

    public IvBezierPath3DRendererText setInwards(boolean inwards) {
        return this;
    }

    public IvBezierPath3DRendererText setCapBottom(float capBottom) {
        return this;
    }

    public IvBezierPath3DRendererText setCapTop(float capTop) {
        return this;
    }

    public void render(IvBezierPath3D path) {
        // TODO: (Sollace) Implement this
    }
}
