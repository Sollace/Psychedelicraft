package ivorius.psychedelicraft.advancement;

import net.minecraft.advancement.criterion.Criteria;

public interface PSCriteria {
    MashingTubEventCriterion SIMPLY_MASHING = Criteria.register(new MashingTubEventCriterion());
    CustomEventCriterion CUSTOM = Criteria.register(new CustomEventCriterion());
    DrugEffectsChangedCriterion DRUG_EFFECTS_CHANGED = Criteria.register(new DrugEffectsChangedCriterion());

    CustomEventCriterion.Trigger FEED_VILLAGER = CUSTOM.createTrigger("feed_villager");
    CustomEventCriterion.Trigger HANGOVER = CUSTOM.createTrigger("get_hangover");

    static void bootstrap() { }
}
