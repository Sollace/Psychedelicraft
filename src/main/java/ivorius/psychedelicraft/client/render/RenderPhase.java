package ivorius.psychedelicraft.client.render;

import java.util.Deque;
import java.util.LinkedList;

public enum RenderPhase {
    NORMAL,
    WORLD,
    SKY,
    CLOUDS,
    SCREEN;

    private static final Deque<RenderPhase> STACK = new LinkedList<>();

    public static RenderPhase current() {
        RenderPhase phase = STACK.peekLast();
        return phase == null ? NORMAL : phase;
    }

    public void push() {
        STACK.add(this);
    }

    public static void pop() {
        STACK.pollLast();
    }
}
