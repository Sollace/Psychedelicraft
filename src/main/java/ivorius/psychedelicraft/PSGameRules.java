package ivorius.psychedelicraft;

import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;

public interface PSGameRules {
    GameRules.Key<BooleanRule> DO_SLEEP_DEPRIVATION = GameRules.register("doSleepDeprivation", GameRules.Category.SPAWNING, BooleanRule.create(false));

    static void bootstrap() { }
}
