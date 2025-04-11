package ivorius.psychedelicraft.util.compat;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;

import net.minecraft.advancement.criterion.AbstractCriterion;

public interface AdvancementCriterionCompat {

    Codec<AbstractCriterion.Conditions> getConditionsCodec();

    public interface Conditions extends AbstractCriterion.Conditions {
        @Override
        default JsonObject toJson() {
            return new JsonObject();
        }
    }
}
