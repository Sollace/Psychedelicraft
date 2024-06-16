package ivorius.psychedelicraft.compat.tia;

import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.plugin.TlaApiPlugin;

public class Main implements TlaApiPlugin {
    @Override
    public void register(PluginContext context) {
        RecipeCategory.bootstrap(context);
    }
}
