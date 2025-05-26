package ivorius.psychedelicraft.util.compat;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;

import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;

public interface AdvancementCriterionCompat {

    Codec<CriterionConditions> getConditionsCodec();

    public interface Conditions extends CriterionConditions {
        @Override
        default JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            return new JsonObject();
        }
    }
}
