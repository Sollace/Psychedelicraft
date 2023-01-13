/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drugs;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drugs.effects.*;

/**
 * Created by lukas on 22.10.14.
 */
public record DrugType (Identifier id, Supplier<Drug> constructor) {
    public static final Registry<DrugType> REGISTRY = FabricRegistryBuilder.createSimple(DrugType.class, Psychedelicraft.id("drugs")).buildAndRegister();
    public static final DrugType ALCOHOL = register("alcohol", () -> new AlcoholDrug(1, 0.0002d));
    public static final DrugType CANNABIS = register("cannabis", () -> new CannabisDrug(1, 0.0002d));
    public static final DrugType BROWN_SHROOMS = register("brown_shrooms", () -> new BrownShroomsDrug(1, 0.0002d));
    public static final DrugType RED_SHROOMS = register("red_shrooms", () -> new RedShroomsDrug(1, 0.0002d));
    public static final DrugType TOBACCO = register("tobacco", () -> new TobaccoDrug(1, 0.003d));
    public static final DrugType COCAINE = register("coccaine", () -> new CocaineDrug(1, 0.0003d));
    public static final DrugType CAFFEINE = register("caffeine", () -> new CaffeineDrug(1, 0.0002d));
    public static final DrugType WARMTH = register("warmth", () -> new WarmthDrug(1, 0.004d));
    public static final DrugType PEYOTE = register("peyote", () -> new PeyoteDrug(1, 0.0002d));
    public static final DrugType ZERO = register("zero", () -> new ZeroDrug(1, 0.0001d));
    public static final DrugType POWER = register("power", () -> new PowerDrug(0.95, 0.0001d));
    public static final DrugType HARMONIUM = register("harmonium", () -> new HarmoniumDrug(1, 0.0003d));

    public Drug create() {
        return constructor.get();
    }

    static DrugType register(String name, Supplier<Drug> constructor) {
        DrugType type = new DrugType(Psychedelicraft.id(name), constructor);
        return Registry.register(REGISTRY, type.id(), type);
    }
}
