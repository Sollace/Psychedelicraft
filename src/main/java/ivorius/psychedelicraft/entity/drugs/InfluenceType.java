package ivorius.psychedelicraft.entity.drugs;

import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.nbt.NbtCompound;

public record InfluenceType (String identifier, Function<InfluenceType, DrugInfluence> constructor) {
    private static final BiMap<String, InfluenceType> REGISTRY = HashBiMap.create();

    public static final InfluenceType DEFAULT = of("default", DrugInfluence::new);
    public static final InfluenceType HARMONIUM = of("harmonium", DrugInfluence::new);

    static InfluenceType of(String id, Function<InfluenceType, DrugInfluence> constructor) {
        var type = new InfluenceType(id, constructor);
        REGISTRY.put(id, type);
        return type;
    }

    public DrugInfluence create() {
        return constructor.apply(this);
    }

    public DrugInfluence create(NbtCompound compound) {
        DrugInfluence instance = create();
        instance.fromNbt(compound);
        return instance;
    }

    public static Optional<InfluenceType> of(String id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }
}