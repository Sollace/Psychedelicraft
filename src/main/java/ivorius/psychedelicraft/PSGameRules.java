package ivorius.psychedelicraft;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;

public interface PSGameRules {
    GameRule<Boolean> DO_SLEEP_DEPRIVATION = GameRuleBuilder.forBoolean(false).category(GameRuleCategory.SPAWNING).buildAndRegister(Psychedelicraft.id("sleep_deprivation"));

    static void bootstrap() { }
}
