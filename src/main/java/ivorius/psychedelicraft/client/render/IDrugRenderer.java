/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

/**
 * Created by lukas on 17.02.14.
 */
public interface IDrugRenderer {
    void update(DrugProperties drugProperties, LivingEntity entity);

    void distortScreen(float tickDelta, LivingEntity entity, int age, DrugProperties drugProperties);

    void renderOverlaysAfterShaders(MatrixStack matrices, float par1, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties);

    void renderOverlaysBeforeShaders(MatrixStack matrices, float par1, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties);

    void renderAllHallucinations(float par1, DrugProperties drugProperties);

    float getCurrentHeatDistortion();

    float getCurrentWaterDistortion();

    float getCurrentWaterScreenDistortion();
}
