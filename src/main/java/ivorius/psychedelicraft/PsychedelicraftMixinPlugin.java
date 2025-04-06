package ivorius.psychedelicraft;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import ivorius.psychedelicraft.client.SodiumCompat;
import net.fabricmc.loader.api.FabricLoader;

public class PsychedelicraftMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("Psychedelicraft");
    private static final String MIXIN_PACKAGE = "ivorius.psychedelicraft.mixin";

    private String sodiumPackage = "";
    private boolean hasSodium;
    private boolean hasIris;

    @Override
    public void onLoad(String mixinPackage) {
        hasSodium = FabricLoader.getInstance().isModLoaded("sodium");
        hasIris = FabricLoader.getInstance().isModLoaded("iris");
        if (hasSodium) {
            sodiumPackage = isTargetAvailable("caffeinemc") ? "caffeinemc" : "jellysquid";
            SodiumCompat.IS_JELLY_SODIUM_LOADED = "jellysquid".equals(sodiumPackage);
            LOGGER.info("Detected sodium package: " + sodiumPackage);
        }
        if (hasIris) {
            LOGGER.info("Detected iris");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(MIXIN_PACKAGE) && mixinClassName.indexOf("sodium") != -1) {
            return hasSodium && targetClassName.indexOf(sodiumPackage) != -1;
        }
        if (mixinClassName.startsWith(MIXIN_PACKAGE) && mixinClassName.indexOf(".iris.") != -1) {
            return hasIris;
        }
        return true;
    }

    private boolean isTargetAvailable(String target) {
        try {
            return Class.forName("net." + target + ".mods.sodium.client.SodiumClientMod") != null;
        } catch (ClassNotFoundException e) {
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
