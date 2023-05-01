package ivorius.psychedelicraft.fluid.alcohol;

import java.util.Locale;
import java.util.Map;

import net.minecraft.text.Text;

public enum Texture {
    THIN,
    SMOOTH,
    FIZZY,
    CHUNKY,
    THICK;

    private static final Map<Texture, Texture[]> MATURATION_PHASES = Map.of(
        THIN, new Texture[] { THIN, SMOOTH, CHUNKY, THICK },
        SMOOTH, new Texture[] { SMOOTH, CHUNKY, THICK },
        FIZZY, new Texture[] { FIZZY, CHUNKY, THICK },
        CHUNKY, new Texture[] { CHUNKY, CHUNKY, CHUNKY },
        THICK, new Texture[] { THICK, CHUNKY }
    );

    public Texture applyState(int distillation, int fermentation, int maturation) {
        Texture[] phases = Texture.MATURATION_PHASES.get(this);
        int combinedProgress = (int)((fermentation + maturation) / 32F) * phases.length;
        return phases[combinedProgress];
    }

    private final Text name = Text.translatable("psychedelicraft.alcohol.texture." + name().toLowerCase(Locale.ROOT));

    public Text getName() {
        return name;
    }
}
