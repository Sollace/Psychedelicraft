package ivorius.psychedelicraft.client;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.Sprite;

public final class SodiumCompat {
    static final boolean IS_SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium");

    public static void markSpriteActive(Sprite sprite) {
        if (!IS_SODIUM_LOADED) {
            return;
        }
        Impl.markSpriteActive(sprite);
    }


    static class Impl {
        static void markSpriteActive(Sprite sprite) {
            SpriteUtil.markSpriteActive(sprite);
        }
    }
}
