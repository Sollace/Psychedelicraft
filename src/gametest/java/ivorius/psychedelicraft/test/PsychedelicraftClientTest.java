package ivorius.psychedelicraft.test;

import ivorius.psychedelicraft.client.screen.SettingsScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class PsychedelicraftClientTest implements FabricClientGameTest {
    private static final String[] DRUGS = {
        "lsd", "cannabis", "alcohol", "brown_shrooms", "red_shrooms", "peyote", "power", "zero", "harmonium",
        "coccaine", "warmth", "atropine", "kava", "bath_salts", "tobacco"
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            sp.getClientWorld().waitForChunksRender();
            run(sp, "time set noon");
            run(sp, "weather clear");
            run(sp, "gamerule advance_time false");
            run(sp, "effect give @a minecraft:regeneration infinite 10 true");
            context.waitTicks(20);
            context.takeScreenshot("ps_00_baseline");

            for (String drug : DRUGS) {
                run(sp, "drug @a set all 0");
                run(sp, "drug @a set psychedelicraft:" + drug + " 1");
                context.waitTicks(200);
                context.takeScreenshot("ps_drug_" + drug);
            }
            run(sp, "drug @a set all 0");
            context.waitTicks(40);

            run(sp, "tp @p ~ ~ ~ 0 20");
            String[] blocks = {"flask", "distillery", "drying_table", "bunsen_burner", "oak_barrel", "rift_jar", "bottle_rack", "tray", "glass_tube", "cauldron", "mash_tub"};
            for (int i = 0; i < blocks.length; i++) {
                run(sp, "execute at @p run setblock ~" + (i - blocks.length / 2) + " ~ ~4 psychedelicraft:" + blocks[i]);
            }
            run(sp, "execute at @p run summon psychedelicraft:reality_rift ~ ~3 ~8");
            context.waitTicks(60);
            context.takeScreenshot("ps_blocks");

            run(sp, "hallucinate psychedelicraft:rasta_head @a");
            run(sp, "hallucinate psychedelicraft:multiple_entity @a");
            run(sp, "hallucinate psychedelicraft:single_entity @a");
            context.waitTicks(100);
            run(sp, "tp @p ~ ~ ~ 0 20");
            context.waitTicks(2);
            context.takeScreenshot("ps_hallucinations");

            run(sp, "damage @p 4");
            context.waitTicks(3);
            context.takeScreenshot("ps_hurt_overlay");

            run(sp, "tp @p ~ ~ ~ 0 -85");
            context.waitTicks(60);
            context.takeScreenshot("ps_sun_flare");

            context.setScreen(() -> new SettingsScreen(null));
            context.waitTicks(10);
            context.takeScreenshot("ps_settings");
            context.setScreen(() -> null);
        }
    }

    private static void run(TestSingleplayerContext sp, String command) {
        sp.getServer().runCommand(command);
    }
}
