package ivorius.psychedelicraft.world.gen.loot;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public class LootExtensionEntry extends LeafEntry {
    public static final MapCodec<LootExtensionEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("value").forGetter(entry -> entry.value)
    ).and(addLeafFields(instance)).apply(instance, LootExtensionEntry::new));

    public static LeafEntry.Builder<?> builder(RegistryKey<LootTable> key) {
        return builder((weight, quality, conditions, functions) -> new LootExtensionEntry(key, weight, quality, conditions, functions));
    }

    private final RegistryKey<LootTable> value;

    public LootExtensionEntry(RegistryKey<LootTable> value, int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) {
        super(weight, quality, conditions, functions);
        this.value = value;
    }

    @Override
    public LootPoolEntryType getType() {
        return PSLootTableEntryType.LOOT_TABLE_EXTENSION;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        context.getLookup().getOptionalEntry(RegistryKeys.LOOT_TABLE, value).map(RegistryEntry::value).orElse(LootTable.EMPTY).generateUnprocessedLoot(context, lootConsumer);
    }

    @Override
    public void validate(LootTableReporter reporter) {
        if (reporter.isInStack(value)) {
            reporter.report("Table " + value.getValue() + " is recursively called");
            return;
        }
        super.validate(reporter);
    }
}
