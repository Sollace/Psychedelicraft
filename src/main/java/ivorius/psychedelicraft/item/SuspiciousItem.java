package ivorius.psychedelicraft.item;

import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

public class SuspiciousItem extends Item {

    private final ItemConvertible hallucinatedFormSupplier;

    public static ItemConvertible createForms(ItemConvertible... items) {
        Random rng = Random.create();
        return () -> {
            return items[rng.nextInt(items.length)].asItem();
        };
    }

    @Nullable
    private Item chosenItem;

    public SuspiciousItem(Settings settings, ItemConvertible hallucinatedFormSupplier) {
        super(settings);
        this.hallucinatedFormSupplier = hallucinatedFormSupplier;
    }

    public Optional<Item> getHallucinatedItem() {
        if (Psychedelicraft.getGlobalDrugProperties().filter(DrugProperties::isTripping).isPresent()) {
            if (chosenItem == null) {
                chosenItem = hallucinatedFormSupplier.asItem();
            }
            return Optional.of(chosenItem);
        }
        chosenItem = null;
        return Optional.empty();
    }

    @Override
    @Deprecated
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        getHallucinatedItem().ifPresent(item -> {
            item.appendTooltip(item.getDefaultStack(), context, displayComponent, textConsumer, type);
        });
    }

    @Override
    public Text getName(ItemStack stack) {
        return getHallucinatedItem().map(i -> i.getName(stack)).orElseGet(() -> super.getName(stack));
    }
}
