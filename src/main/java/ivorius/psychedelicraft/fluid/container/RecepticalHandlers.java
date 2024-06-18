package ivorius.psychedelicraft.fluid.container;

import java.util.Optional;
import java.util.function.Function;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.util.Util;

public interface RecepticalHandlers {

    static void bootstrap() {
        RecepticalHandler.register(stack -> stack.isIn(ConventionalItemTags.BUCKETS) || stack.isIn(ConventionalItemTags.EMPTY_BUCKETS), new RecepticalHandler() {

            private final Function<SimpleFluid, Optional<Item>> filledBuckets = Util.memoize(fluid -> {
                String path = fluid.getId().getPath() + "_bucket";
                return Registries.ITEM.getIds().stream().filter(id -> id.getPath().equals(path)).findFirst().map(Registries.ITEM::get);
            });

            @Override
            public ItemStack toFilled(Item item, ItemFluids contents) {
                if (contents.amount() < FluidVolumes.BUCKET) {
                    return PSItems.FILLED_BUCKET.getDefaultStack();
                }
                return filledBuckets.apply(contents.fluid()).orElse(PSItems.FILLED_BUCKET).getDefaultStack();
            }

            @Override
            public ItemStack toEmpty(Item item) {
                return Items.BUCKET.getDefaultStack();
            }
        });
        RecepticalHandler.registerPair(Items.BOWL, PSItems.FILLED_BOWL);
        RecepticalHandler.register(stack -> stack.getItem() instanceof GlassBottleItem || stack.getItem() instanceof PotionItem || stack.getItem() == PSItems.FILLED_GLASS_BOTTLE, new RecepticalHandler() {
            private final Function<SimpleFluid, Optional<Item>> filledBottles = Util.memoize(fluid -> {
                String path = fluid.getId().getPath() + "_bottle";
                String altPath = fluid.getId().getPath() + "_glass_bottle";
                return Registries.ITEM.getIds().stream().filter(id -> id.getPath().equals(path) || id.getPath().equals(altPath)).findFirst().map(Registries.ITEM::get);
            });

            @Override
            public ItemStack toFilled(Item item, ItemFluids contents) {
                if (contents.amount() < FluidVolumes.BOTTLE) {
                    return PSItems.FILLED_GLASS_BOTTLE.getDefaultStack();
                }
                if (contents.fluid() == SimpleFluid.forVanilla(Fluids.WATER)) {
                    return PotionContentsComponent.createStack(Items.POTION, Potions.WATER);
                }
                return filledBottles.apply(contents.fluid()).orElse(PSItems.FILLED_GLASS_BOTTLE).getDefaultStack();
            }

            @Override
            public ItemStack toEmpty(Item item) {
                return Items.GLASS_BOTTLE.getDefaultStack();
            }
        });
    }
}
