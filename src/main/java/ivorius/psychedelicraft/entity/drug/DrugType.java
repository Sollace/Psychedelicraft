/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.type.*;

/**
 * Created by lukas on 22.10.14.
 */
public record DrugType (Identifier id, Function<DrugType, Drug> constructor, DrugAttributeFunctions functions) {
    public static final Registry<DrugType> REGISTRY = FabricRegistryBuilder.createSimple(RegistryKey.<DrugType>ofRegistry(Psychedelicraft.id("drugs"))).buildAndRegister();
    public static final DrugType ALCOHOL = register("alcohol", AlcoholDrug.FUNCTIONS, type -> new AlcoholDrug(type, 1, 0.0002d));
    public static final DrugType CANNABIS = register("cannabis", CannabisDrug.FUNCTIONS, type -> new CannabisDrug(1, 0.0002d));
    public static final DrugType BROWN_SHROOMS = register("brown_shrooms", BrownShroomsDrug.FUNCTIONS, type -> new BrownShroomsDrug(1, 0.0002d));
    public static final DrugType RED_SHROOMS = register("red_shrooms", RedShroomsDrug.FUNCTIONS, type -> new RedShroomsDrug(1, 0.0002d));
    public static final DrugType TOBACCO = register("tobacco", TobaccoDrug.FUNCTIONS, type -> new TobaccoDrug(1, 0.003d));
    public static final DrugType COCAINE = register("coccaine", CocaineDrug.FUNCTIONS, type -> new CocaineDrug(1, 0.0003d));
    public static final DrugType CAFFEINE = register("caffeine", CaffeineDrug.functions(1), type -> new CaffeineDrug(type, 1, 0.0002d));
    public static final DrugType SUGAR = register("sugar", CaffeineDrug.functions(0), type -> new CaffeineDrug(type, 1, 0.0002d));
    public static final DrugType BATH_SALTS = register("bath_salts", BathSaltsDrug.FUNCTIONS, type -> new BathSaltsDrug(1, 0.00012d));
    public static final DrugType SLEEP_DEPRIVATION = register("sleep_deprivation", SleepDeprivationDrug.FUNCTIONS, type -> new SleepDeprivationDrug());
    public static final DrugType LSD = register("lsd", LsdDrug.FUNCTIONS, type -> new LsdDrug(type, 1, 0.0003d));
    public static final DrugType ATROPINE = register("atropine", AtropineDrug.FUNCTIONS, type -> new AtropineDrug(1, 0.0003d));
    public static final DrugType KAVA = register("kava", KavaDrug.FUNCTIONS, type -> new KavaDrug(1, 0.0002d));
    public static final DrugType WARMTH = register("warmth", WarmthDrug.FUNCTIONS, type -> new WarmthDrug(1, 0.004d));
    public static final DrugType PEYOTE = register("peyote", PeyoteDrug.FUNCTIONS, type -> new PeyoteDrug(1, 0.0002d));
    public static final DrugType ZERO = register("zero", DrugAttributeFunctions.empty(), type -> new SimpleDrug(type, 1, 0.0001d));
    public static final DrugType POWER = register("power", PowerDrug.FUNCTIONS, type -> new PowerDrug(0.95, 0.0001d));
    public static final DrugType HARMONIUM = register("harmonium", DrugAttributeFunctions.empty(), type -> new HarmoniumDrug(1, 0.0003d));

    public Drug create() {
        return constructor.apply(this);
    }

    static DrugType register(String name, DrugAttributeFunctions functions, Function<DrugType, Drug> constructor) {
        DrugType type = new DrugType(Psychedelicraft.id(name), constructor, functions);
        PSSounds.register("drug." + name);
        return Registry.register(REGISTRY, type.id(), type);
    }
}
