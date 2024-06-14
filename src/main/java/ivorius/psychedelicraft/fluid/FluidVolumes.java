/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

/**
 * Created by lukas on 22.10.14.
 */
public interface FluidVolumes {
    int BUCKET = (int)FluidConstants.BUCKET;

    int CAULDRON = BUCKET;
    int GLASS_BOTTLE = (int)FluidConstants.BOTTLE;
    int BOWL = BUCKET / 20;

    int MUG = BUCKET / 2;
    int CUP = BUCKET / 4;
    int CHALLICE = BUCKET / 5;
    int SHOT = BUCKET / 25;
    int BOTTLE = BUCKET * 2;
    int SYRINGE = BUCKET / 100;

    int BARREL = BUCKET * 8;
    int VAT = BUCKET * 9;
    int FLASK = BUCKET * 8;
}
