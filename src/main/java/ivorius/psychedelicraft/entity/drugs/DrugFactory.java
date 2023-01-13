/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drugs;

import net.minecraft.entity.LivingEntity;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by lukas on 01.11.14.
 */
public interface DrugFactory {
    void createDrugs(LivingEntity entity, List<Pair<String, Drug>> drugs);

    void addManagedDrugNames(List<String> drugNames);
}
