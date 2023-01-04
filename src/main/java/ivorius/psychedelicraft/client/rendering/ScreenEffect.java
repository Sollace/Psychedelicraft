package ivorius.psychedelicraft.client.rendering;

public interface ScreenEffect {
    boolean shouldApply(float tickDelta);

    void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong);

    default void destruct() {

    }

    interface PingPong {
        void pingPong();
    }
}
