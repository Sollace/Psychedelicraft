/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Created by lukas on 24.05.14.
 */
public interface PSProxy
{
    void preInit();

    void spawnColoredParticle(Entity entity, float[] color, Vec3d direction, float speed, float size);

    void createDrugRenderer(DrugProperties drugProperties);

    void loadConfig(String configID);
}
