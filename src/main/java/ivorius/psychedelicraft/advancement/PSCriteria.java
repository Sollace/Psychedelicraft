package ivorius.psychedelicraft.advancement;

import net.minecraft.advancement.criterion.Criteria;

public interface PSCriteria {
    MashingTubEventCriterion SIMPLY_MASHING = Criteria.register(new MashingTubEventCriterion());

    static void bootstrap() { }
}
