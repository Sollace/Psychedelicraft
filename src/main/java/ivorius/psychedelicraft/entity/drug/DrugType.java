/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.type.*;

/**
 * Created by lukas on 22.10.14.
 */
public record DrugType<T extends Drug> (
        Identifier id,
        Function<DrugType<T>, T> constructor,
        Function<DrugType<T>, MapCodec<T>> codecFunction,
        DrugAttributeFunctions functions) {
    public static final Registry<DrugType<?>> REGISTRY = FabricRegistryBuilder.createSimple(RegistryKey.<DrugType<?>>ofRegistry(Psychedelicraft.id("drugs"))).buildAndRegister();
    public static final DrugType<AlcoholDrug> ALCOHOL = register("alcohol", AlcoholDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new AlcoholDrug(type, 1, 0.0002d));
    public static final DrugType<CannabisDrug> CANNABIS = register("cannabis", CannabisDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new CannabisDrug(1, 0.0002d));
    public static final DrugType<BrownShroomsDrug> BROWN_SHROOMS = register("brown_shrooms", BrownShroomsDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new BrownShroomsDrug(1, 0.0002d));
    public static final DrugType<RedShroomsDrug> RED_SHROOMS = register("red_shrooms", RedShroomsDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new RedShroomsDrug(1, 0.0002d));
    public static final DrugType<TobaccoDrug> TOBACCO = register("tobacco", TobaccoDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new TobaccoDrug(1, 0.003d));
    public static final DrugType<CocaineDrug> COCAINE = register("coccaine", CocaineDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new CocaineDrug(1, 0.0003d));
    public static final DrugType<CaffeineDrug> CAFFEINE = register("caffeine", CaffeineDrug.functions(1), SimpleDrug::createCodec, type -> new CaffeineDrug(type, 1, 0.0002d));
    public static final DrugType<CaffeineDrug> SUGAR = register("sugar", CaffeineDrug.functions(0), SimpleDrug::createCodec, type -> new CaffeineDrug(type, 1, 0.0002d));
    public static final DrugType<BathSaltsDrug> BATH_SALTS = register("bath_salts", BathSaltsDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new BathSaltsDrug(1, 0.00012d));
    public static final DrugType<SleepDeprivationDrug> SLEEP_DEPRIVATION = register("sleep_deprivation", SleepDeprivationDrug.FUNCTIONS, SleepDeprivationDrug.CODEC, type -> new SleepDeprivationDrug());
    public static final DrugType<LsdDrug> LSD = register("lsd", LsdDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new LsdDrug(type, 1, 0.0003d));
    public static final DrugType<AtropineDrug> ATROPINE = register("atropine", AtropineDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new AtropineDrug(1, 0.0003d));
    public static final DrugType<KavaDrug> KAVA = register("kava", KavaDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new KavaDrug(1, 0.0002d));
    public static final DrugType<WarmthDrug> WARMTH = register("warmth", WarmthDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new WarmthDrug(1, 0.004d));
    public static final DrugType<PeyoteDrug> PEYOTE = register("peyote", PeyoteDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new PeyoteDrug(1, 0.0002d));
    public static final DrugType<SimpleDrug> ZERO = register("zero", DrugAttributeFunctions.empty(), SimpleDrug::createCodec, type -> new SimpleDrug(type, 1, 0.0001d));
    public static final DrugType<PowerDrug> POWER = register("power", PowerDrug.FUNCTIONS, SimpleDrug::createCodec, type -> new PowerDrug(0.95, 0.0001d));
    public static final DrugType<HarmoniumDrug> HARMONIUM = register("harmonium", DrugAttributeFunctions.empty(), SimpleDrug::createCodec, type -> new HarmoniumDrug(1, 0.0003d));

    public T create() {
        return constructor.apply(this);
    }

    public MapCodec<T> codec() {
        return this.codecFunction.apply(this);
    }

    public SoundEvent soundEvent() {
        return Registries.SOUND_EVENT.getOrEmpty(id.withPrefixedPath("drug.")).orElse(PSSounds.DRUG_GENERIC);
    }

    static <T extends Drug> DrugType<T> register(String name, DrugAttributeFunctions functions, MapCodec<T> codec, Function<DrugType<T>, T> constructor) {
        return register(name, functions, t -> codec, constructor);
    }

    static <T extends Drug> DrugType<T> register(String name, DrugAttributeFunctions functions, Function<DrugType<T>, MapCodec<T>> codec, Function<DrugType<T>, T> constructor) {
        DrugType<T> type = new DrugType<>(Psychedelicraft.id(name), Util.memoize(constructor), codec, functions);
        PSSounds.register("drug." + name);
        return Registry.register(REGISTRY, type.id(), type);
    }
}
