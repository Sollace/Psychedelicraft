package ivorius.psychedelicraft.advancement;

import net.minecraft.advancement.criterion.Criteria;

public interface PSCriteria {
    MashingTubEventCriterion SIMPLY_MASHING = Criteria.register(new MashingTubEventCriterion());
    CustomEventCriterion CUSTOM = Criteria.register(new CustomEventCriterion());
    DrugEffectsChangedCriterion DRUG_EFFECTS_CHANGED = Criteria.register(new DrugEffectsChangedCriterion());

    CustomEventCriterion.Trigger FEED_VILLAGER = CUSTOM.createTrigger("feed_villager");
    CustomEventCriterion.Trigger BREATHE_SMOKE_ON_ENTITY = CUSTOM.createTrigger("breathe_smoke_on_entity");
    CustomEventCriterion.Trigger FEED_BABY_VILLAGER = CUSTOM.createTrigger("feed_baby_villager");
    CustomEventCriterion.Trigger HANGOVER = CUSTOM.createTrigger("get_hangover");
    CustomEventCriterion.Trigger TRAY_HARDEN = CUSTOM.createTrigger("tray_harden");
    CustomEventCriterion.Trigger SIDE_EFFECT = CUSTOM.createTrigger("side_effect");
    CustomEventCriterion.Trigger SUCK_PACIFIER = CUSTOM.createTrigger("suck_pacifier");
    CustomEventCriterion.Trigger HEART_ATTACK = CUSTOM.createTrigger("heart_attack");
    CustomEventCriterion.Trigger CANCER = CUSTOM.createTrigger("cancer");
    CustomEventCriterion.Trigger CURE_CANCER = CUSTOM.createTrigger("cure_cancer");

    static void bootstrap() { }
}
