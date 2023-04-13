package ivorius.psychedelicraft.advancement;

import net.minecraft.advancement.criterion.Criteria;

public interface PSCriteria {
    MashingTubEventCriterion SIMPLY_MASHING = Criteria.register(new MashingTubEventCriterion());
    CustomEventCriterion CUSTOM = Criteria.register(new CustomEventCriterion());

    CustomEventCriterion.Trigger FEED_VILLAGER = CUSTOM.createTrigger("feed_villager");

    static void bootstrap() { }
}
