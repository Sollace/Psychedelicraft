/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs;

import ivorius.psychedelicraft.entities.drugs.effects.*;
import net.minecraft.entity.LivingEntity;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 01.11.14.
 */
public class DrugFactoryPsychedelicraft implements DrugFactory
{
    @Override
    public void createDrugs(LivingEntity entity, List<Pair<String, Drug>> drugs) {
        addDrug("Alcohol", new AlcoholDrug(1, 0.0002d), drugs);
        addDrug("Cannabis", new CannabisDrug(1, 0.0002d), drugs);
        addDrug("BrownShrooms", new BrownShroomsDrug(1, 0.0002d), drugs);
        addDrug("RedShrooms", new RedShroomsDrug(1, 0.0002d), drugs);
        addDrug("Tobacco", new TobaccoDrug(1, 0.003d), drugs);
        addDrug("Cocaine", new CocaineDrug(1, 0.0003d), drugs);
        addDrug("Caffeine", new CaffeineDrug(1, 0.0002d), drugs);
        addDrug("Warmth", new WarmthDrug(1, 0.004d), drugs);
        addDrug("Peyote", new PeyoteDrug(1, 0.0002d), drugs);
        addDrug("Zero", new ZeroDrug(1, 0.0001d), drugs);
        addDrug("Power", new PowerDrug(0.95, 0.0001d), drugs);
        addDrug("Harmonium", new HarmoniumDrug(1, 0.0003d), drugs);
    }

    @Override
    public void addManagedDrugNames(List<String> drugNames) {
        Collections.addAll(drugNames, "Alcohol", "Cannabis", "BrownShrooms", "RedShrooms",
                "Tobacco", "Cocaine", "Caffeine", "Warmth", "Peyote", "Zero", "Power", "Harmonium");
    }

    public void addDrug(String key, Drug drug, List<Pair<String, Drug>> drugs) {
        drugs.add(new ImmutablePair<>(key, drug));
    }
}
