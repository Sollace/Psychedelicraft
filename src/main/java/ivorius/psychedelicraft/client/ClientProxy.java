/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client;

import ivorius.psychedelicraft.PSProxy;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;

public class ClientProxy extends PSProxy {
    @Override
    public void createDrugRenderer(DrugProperties drugProperties) {
        drugProperties.renderer = new DrugRenderer();
    }
}
