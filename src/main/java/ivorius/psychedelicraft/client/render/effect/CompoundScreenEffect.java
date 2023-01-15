package ivorius.psychedelicraft.client.render.effect;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class CompoundScreenEffect implements ScreenEffect {

    private final List<ScreenEffect> effects = new ArrayList<>();

    private final MinecraftClient client = MinecraftClient.getInstance();

    public static ScreenEffect of(ScreenEffect... effects) {
        return new CompoundScreenEffect().add(effects);
    }

    private CompoundScreenEffect() { }

    public CompoundScreenEffect add(ScreenEffect... effects) {
        for (ScreenEffect effect : effects) {
            add(effect);
        }
        return this;
    }

    public CompoundScreenEffect add(ScreenEffect effect) {
        if (effect instanceof CompoundScreenEffect comp) {
            effects.addAll(comp.effects);
        } else {
            effects.add(effect);
        }
        return this;
    }

    @Override
    public void update(float tickDelta) {
        effects.forEach(effect -> effect.update(tickDelta));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        effects.forEach(effect -> {
            if (effect.shouldApply(client.getTickDelta())) {
                effect.render(matrices, vertices, screenWidth, screenHeight, ticks, pingPong);
            }
        });
    }

    @Override
    public void close() {
        effects.forEach(e -> {
            try {
                e.close();
            } catch (Exception ignored) {}
        });
        effects.clear();
    }
}
