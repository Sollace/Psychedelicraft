/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;

/**
 * Created by lukas on 24.05.14.
 */
public class PSProxy {

    private static PSProxy instance;

    public static PSProxy getInstance() {
        return instance;
    }

    protected PSProxy() {
        instance = this;
    }

    public void createDrugRenderer(DrugProperties drugProperties) {

    }

    public PSConfig createConfig() {
        return new PSConfig();
    }
}
