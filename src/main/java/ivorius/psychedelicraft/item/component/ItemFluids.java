package ivorius.psychedelicraft.item.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.container.FluidTransferUtils;
import ivorius.psychedelicraft.fluid.container.RecepticalHandler;
import ivorius.psychedelicraft.fluid.container.VariantMarshal;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record ItemFluids(SimpleFluid fluid, int amount, Map<String, Integer> attributes) {
    public static final ItemFluids EMPTY = new ItemFluids(PSFluids.EMPTY, 0, Map.of());
    public static final Codec<Map<String, Integer>> ATTRIBUTES_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);
    public static final Codec<ItemFluids> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SimpleFluid.CODEC.fieldOf("fluid").forGetter(ItemFluids::fluid),
            Codec.INT.optionalFieldOf("amount", 1).forGetter(ItemFluids::amount),
            ATTRIBUTES_CODEC.optionalFieldOf("attributes", Map.of()).forGetter(ItemFluids::attributes)
    ).apply(instance, ItemFluids::create));
    public static final PacketCodec<RegistryByteBuf, ItemFluids> PACKET_CODEC = PacketCodec.tuple(
            SimpleFluid.PACKET_CODEC, ItemFluids::fluid,
            PacketCodecs.INTEGER, ItemFluids::amount,
            PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.INTEGER), ItemFluids::attributes,
            ItemFluids::create
    );

    @Deprecated
    public static ItemFluids fromCustom(ItemStack stack) {
        NbtComponent tag = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (tag != null) {
            NbtCompound nbt = tag.getNbt();
            if (nbt.contains("fluid", NbtElement.COMPOUND_TYPE)) {
                NbtCompound fluidTag = nbt.getCompound("fluid");
                nbt.remove("fluid");
                if (nbt.isEmpty()) {
                    stack.remove(DataComponentTypes.CUSTOM_DATA);
                }

                ItemFluids fluids = create(
                        SimpleFluid.byId(Identifier.validate(fluidTag.getString("id")).result().orElse(SimpleFluid.EMPTY_KEY)),
                        fluidTag.getInt("level"),
                        ATTRIBUTES_CODEC.decode(NbtOps.INSTANCE, fluidTag.getCompound("attributes")).result().map(pair -> pair.getFirst()).orElse(Map.of())
                );
                stack.set(PSComponents.FLUIDS, fluids);
                return fluids;
            }
        }
        return ItemFluids.EMPTY;
    }

    @NotNull
    public static ItemFluids direct(ItemStack stack) {
        ItemFluids fluids = stack.get(PSComponents.FLUIDS);
        return fluids == null ? fromCustom(stack) : fluids;
    }

    @NotNull
    public static ItemFluids of(ItemStack stack) {
        ItemFluids fluids = stack.get(PSComponents.FLUIDS);
        if (fluids == null) {
            var fabricContents = FluidTransferUtils.getContents(stack);
            if (fabricContents.isPresent()) {
                return of(fabricContents.get().getRight(), fabricContents.get().getLeft().intValue());
            }
        }
        return fluids == null ? EMPTY : fluids;
    }

    public static ItemFluids of(FluidVariant variant, int capacity) {
        Optional<? extends ItemFluids> fluidsOptional = variant.getComponents().get(PSComponents.FLUIDS);
        ItemFluids fluids = fluidsOptional == null ? null : fluidsOptional.orElse(null);
        if (fluids == null) {
            SimpleFluid fluid = SimpleFluid.forVanilla(variant.getFluid());
            return create(fluid, capacity, Map.of());
        }
        return fluids.ofAmount(capacity);
    }

    public FluidVariant toVariant() {
        return FluidVariant.of(fluid().getPhysical().getStandingFluid(), ComponentChanges.builder().add(PSComponents.FLUIDS, this).build());
    }

    public static ItemFluids create(SimpleFluid fluid, int amount, Map<String, Integer> attributes) {
        if (fluid.isEmpty() || amount <= 0) {
            return EMPTY;
        }
        return new ItemFluids(fluid, amount, Map.copyOf(attributes));
    }

    public static ItemStack set(ItemStack stack, ItemFluids fluids) {
        int capacity = FluidCapacity.get(stack);
        if (capacity > 0) {
            if (capacity < fluids.amount()) {
                fluids = create(fluids.fluid(), capacity, fluids.attributes());
            }
            stack = fluids.isEmpty() ? RecepticalHandler.get(stack).toEmpty(stack.getItem()) : RecepticalHandler.get(stack).toFilled(stack.getItem(), fluids);
            stack.set(PSComponents.FLUIDS, fluids);
        }
        return stack;
    }

    public ItemFluids withAttribute(String attribute, int value) {
        Map<String, Integer> attributes = new HashMap<>(attributes());
        attributes.put(attribute, value);
        return create(fluid, amount, attributes);
    }

    public ItemFluids withAttributes(Map<String, Integer> attributes) {
        return create(fluid(), amount(), attributes);
    }

    public ItemFluids ofAmount(int amount) {
        if (amount == amount()) {
            return this;
        }
        if (amount == 0) {
            return EMPTY;
        }
        return create(fluid(), amount, attributes());
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean canCombine(ItemFluids fluids) {
        return isEmpty() || fluids.isEmpty() || (fluid() == fluids.fluid() && attributes().equals(fluids.attributes()));
    }

    public int getHash() {
        return fluid.getHash(this);
    }

    public boolean isRoughlyEqual(ItemFluids fluids) {
        return fluid() == fluids.fluid() && getHash() == fluids.getHash();
    }

    public Text getName() {
        return fluid().getName(this);
    }

    public void appendTooltip(List<Text> tooltip, TooltipType type) {
        fluid().appendTooltip(this, tooltip, type);
    }

    public interface Transaction {
        static Transaction begin(ItemStack initialStack) {
            if (initialStack.get(PSComponents.FLUIDS) == null) {
                return new VariantMarshal.FabricTransaction(initialStack);
            }
            return new DirectTransaction(initialStack);
        }

        int capacity();

        ItemFluids withdraw(int amount);

        /**
         * Deposits up to {maxAmount} of the given fluid.
         *
         * @return The remaining fluid that could not be inserted.
         */
        default ItemFluids deposit(ItemFluids fluids) {
            return deposit(fluids, fluids.amount());
        }

        /**
         * Deposits up to {maxAmount} of the given fluid.
         *
         * @return The remaining fluid that could not be inserted.
         */
        ItemFluids deposit(ItemFluids fluids, int maxAmount);

        ItemFluids fluids();

        ItemStack toItemStack();

        default boolean canAccept(ItemFluids fluids) {
            return canAccept(fluids, fluids.amount());
        }

        default boolean canAccept(ItemFluids fluids, int amount) {
            return fluids().canCombine(fluids) && Math.min(fluids.amount(), (capacity() - fluids().amount())) >= amount;
        }
    }

    public static class DirectTransaction implements Transaction {
        private ItemStack stack;
        private final int capacity;
        private ItemFluids fluids;

        public DirectTransaction(ItemStack initialStack) {
            this(initialStack, FluidCapacity.get(initialStack), direct(initialStack));
        }

        public DirectTransaction(ItemStack initialStack, int capacity, ItemFluids fluids) {
            this.stack = initialStack.copy();
            this.capacity = capacity;
            this.fluids = fluids;
        }

        @Override
        public int capacity() {
            return capacity;
        }

        @Override
        public ItemFluids fluids() {
            return fluids;
        }

        @Override
        public ItemFluids withdraw(int amount) {
            if (capacity <= 0) {
                return EMPTY;
            }
            ItemFluids removed = fluids.ofAmount(Math.min(amount, fluids.amount()));
            this.fluids = fluids.ofAmount(fluids.amount() - removed.amount());
            return removed;
        }

        @Override
        public ItemFluids deposit(ItemFluids fluids, int maxAmount) {
            if (capacity <= 0 || !this.fluids().canCombine(fluids)) {
                return fluids;
            }

            int maxInserted = Math.min(Math.min(capacity - this.fluids.amount(), fluids.amount()), maxAmount);
            if (maxInserted > 0) {
                this.fluids = fluids.ofAmount(this.fluids.amount() + maxInserted);

                if (maxInserted >= fluids.amount()) {
                    return EMPTY;
                }

                return fluids.ofAmount(fluids.amount() - maxInserted);
            }

            return fluids;
        }

        @Override
        public ItemStack toItemStack() {
            return set(stack, fluids);
        }
    }
}
