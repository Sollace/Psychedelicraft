package ivorius.psychedelicraft.client;

import ivorius.psychedelicraft.mixin.client.sodium.JellySquidSpriteUtil;
import net.minecraft.client.texture.Sprite;

public final class SodiumCompat {
    public static boolean IS_JELLY_SODIUM_LOADED;

    public static void markSpriteActive(Sprite sprite) {
        // We only need it for legacy versions since the bug is fixed in newer
        if (!IS_JELLY_SODIUM_LOADED) {
            return;
        }
        Impl.markSpriteActive(sprite);
    }

    static class Impl {
        static void markSpriteActive(Sprite sprite) {
            JellySquidSpriteUtil.invokeMarkSpriteActive(sprite);
        }
    }
}
