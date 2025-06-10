package ivorius.psychedelicraft.recipe;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.Impurities.Impurity;
import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.util.PacketCodecUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record ImpuritiesPredicate(Set<Impurity> require, Set<Impurity> reject, Optional<Set<Impurity>> allow) implements Predicate<Impurities> {
    public static final ImpuritiesPredicate EMPTY = new ImpuritiesPredicate(Set.of(), Set.of(), Optional.empty());
    public static final Codec<ImpuritiesPredicate> CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecUtils.setOf(Impurity.CODEC).optionalFieldOf("require", Set.of()).forGetter(ImpuritiesPredicate::require),
            CodecUtils.setOf(Impurity.CODEC).optionalFieldOf("reject", Set.of()).forGetter(ImpuritiesPredicate::reject),
            CodecUtils.setOf(Impurity.CODEC).optionalFieldOf("allow").forGetter(ImpuritiesPredicate::allow)
    ).apply(i, ImpuritiesPredicate::new));
    private static final PacketCodec<RegistryByteBuf, Set<Impurity>> IMPURITY_SET_PACKET_CODEC = PacketCodecUtils.ofEnum(Impurity.class).collect(PacketCodecs.toCollection(HashSet::new));
    public static final PacketCodec<RegistryByteBuf, ImpuritiesPredicate> PACKET_CODEC = PacketCodec.tuple(
            IMPURITY_SET_PACKET_CODEC, ImpuritiesPredicate::require,
            IMPURITY_SET_PACKET_CODEC, ImpuritiesPredicate::reject,
            PacketCodecs.optional(IMPURITY_SET_PACKET_CODEC), ImpuritiesPredicate::allow,
            ImpuritiesPredicate::new
    );

    public ImpuritiesPredicate {
        require = Set.copyOf(require);
        reject = Set.copyOf(reject);
        allow = allow.map(Set::copyOf);
    }

    @Override
    public boolean test(Impurities impurities) {
        boolean hasRequired = require.isEmpty() || impurities.impurities().containsAll(require);
        boolean hasRejected = !reject.isEmpty() && impurities.impurities().stream().anyMatch(reject::contains);
        return hasRequired && !hasRejected;
    }

    public Impurities permitted(Impurities impurities) {
        return this.allow.map(i -> Impurities.overlap(impurities, i)).orElse(impurities);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Set<Impurity> require = new HashSet<>();
        private final Set<Impurity> reject = new HashSet<>();
        private Optional<Set<Impurity>> allow = Optional.empty();

        private Builder() {}

        public Builder require(Impurity...impurities) {
            this.require.addAll(List.of(impurities));
            return this;
        }

        public Builder reject(Impurity...impurities) {
            this.reject.addAll(List.of(impurities));
            return this;
        }

        public Builder allow(Impurity...impurities) {
            if (this.allow.isEmpty()) {
                this.allow = Optional.of(new HashSet<>(Set.of(impurities)));
            } else {
                this.allow.get().addAll(List.of(impurities));
            }
            return this;
        }

        public ImpuritiesPredicate build() {
            return new ImpuritiesPredicate(require, reject, allow);
        }
    }
}
