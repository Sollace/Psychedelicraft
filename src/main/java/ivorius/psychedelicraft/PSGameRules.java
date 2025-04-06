package ivorius.psychedelicraft;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;

public interface PSGameRules {
    GameRules.Key<BooleanRule> DO_SLEEP_DEPRIVATION = GameRuleRegistry.register("doSleepDeprivation", GameRules.Category.SPAWNING, GameRuleFactory.createBooleanRule(false));

    static void bootstrap() { }
}
