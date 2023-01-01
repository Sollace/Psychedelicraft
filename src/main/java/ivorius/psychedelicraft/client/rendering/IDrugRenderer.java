/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.entity.LivingEntity;

/**
 * Created by lukas on 17.02.14.
 */
public interface IDrugRenderer {
    void update(DrugProperties drugProperties, LivingEntity entity);

    void distortScreen(float par1, LivingEntity entity, int rendererUpdateCount, DrugProperties drugProperties);

    void renderOverlaysAfterShaders(float par1, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties);

    void renderOverlaysBeforeShaders(float par1, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties);

    void renderAllHallucinations(float par1, DrugProperties drugProperties);

    float getCurrentHeatDistortion();

    float getCurrentWaterDistortion();

    float getCurrentWaterScreenDistortion();
}
