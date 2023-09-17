package ivorius.psychedelicraft.advancement;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;

public interface PSCriteria {
    MashingTubEventCriterion SIMPLY_MASHING = register("mashed_item", new MashingTubEventCriterion());
    CustomEventCriterion CUSTOM = register("custom", new CustomEventCriterion());
    DrugEffectsChangedCriterion DRUG_EFFECTS_CHANGED = register("drug_effects_changed", new DrugEffectsChangedCriterion());

    CustomEventCriterion.Trigger FEED_VILLAGER = CUSTOM.createTrigger("feed_villager");
    CustomEventCriterion.Trigger HANGOVER = CUSTOM.createTrigger("get_hangover");

    private static <T extends Criterion<?>> T register(String id, T criterion) {
        return Criteria.register(Psychedelicraft.id(id).toString(), criterion);
    }

    static void bootstrap() { }
}
