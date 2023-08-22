package ivorius.psychedelicraft.item;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.util.Compat119;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class PaperBagItem extends Item {

    public PaperBagItem(Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, new ItemDispenserBehavior(){
            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Contents contents = getContents(stack);
                if (contents.isEmpty() || stack.getCount() > 1) {
                    return super.dispenseSilently(pointer, stack);
                }
                Contents.Builder builder = new Contents.Builder(contents);
                ItemStack dispensed = builder.split(1);
                setContents(stack, builder.build());
                return super.dispenseSilently(pointer, dispensed);
            }
        });
    }

    @Override
    public boolean onStackClicked(ItemStack bag, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT || bag.getCount() != 1) {
            return false;
        }

        Contents contents = getContents(bag);
        Contents.Builder builder = new Contents.Builder(contents);

        ItemStack slotStack = slot.getStack();

        if (slotStack.isEmpty()) {
            if (!contents.isEmpty()) {
                // dispense into empty slot
                builder.add(slot.insertStack(builder.split(contents.stack().getMaxCount())));
                player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 1, 1);
                setContents(bag, builder.build());
                return true;
            }
        } else if (canPickUp(slotStack) && builder.canAdd(slotStack)) {
            // pick up from filled slot
            int maxTaken = getMaxCountForItem(slotStack.getItem()) - contents.count();

            builder.add(slot.takeStackRange(slotStack.getCount(), maxTaken, player));
            player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
            setContents(bag, builder.build());
            return true;
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack clickedStack, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !(cursorStack.getItem() instanceof PaperBagItem || slot.canTakePartial(player)) || clickedStack.getCount() != 1) {
            return false;
        }

        Contents contents = getContents(clickedStack);
        Contents.Builder builder = new Contents.Builder(contents);

        if (cursorStack.isEmpty()) {
            // remove from bag into held stack
            cursorStackReference.set(builder.split(64));
            player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
            setContents(clickedStack, builder.build());
            return true;
        }

        if (builder.canAdd(cursorStack)) {
            int maxTaken = getMaxCountForItem(cursorStack.getItem()) - contents.count();

            if (canPickUp(cursorStack)) {
                // insert into bag from held stack
                builder.add(cursorStack.split(maxTaken));
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
                setContents(clickedStack, builder.build());
                return true;
            }

            // bag to bag transfer
            if (cursorStack.getItem() instanceof PaperBagItem) {
                Contents.Builder cursorContents = new Contents.Builder(getContents(cursorStack));
                builder.add(cursorContents.split(maxTaken));
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
                setContents(clickedStack, builder.build());
                setContents(cursorStack, cursorContents.build());
                return true;
            }
        }

        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack bag = user.getStackInHand(hand);
        Contents contents = getContents(bag);

        if (!contents.isEmpty()) {
            user.dropItem(removeItems(bag, contents, user.isSneaky() ? 1 : contents.stack().getMaxCount()), false, false);
            user.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 1, 1);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(bag, world.isClient());
        }
        return TypedActionResult.fail(bag);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack bag = context.getStack();

        Contents contents = getContents(bag);

        Contents.Builder builder = new Contents.Builder(contents);

        if (context.getWorld().getOtherEntities(context.getPlayer(),
                Box.from(context.getHitPos()).expand(0.25),
                e -> e instanceof ItemEntity i && canPickUp(i.getStack()))
            .stream().filter(e -> {
                if (builder.add(((ItemEntity)e).getStack())) {
                    if (((ItemEntity)e).getStack().isEmpty()) {
                        e.discard();
                    }
                    return true;
                }
                return false;
            }).findAny().isEmpty()) {

            if (!contents.isEmpty()) {
                context.getPlayer().dropItem(removeItems(bag, contents, context.getPlayer().isSneaky() ? 1 : contents.stack().getMaxCount()), false, false);
                context.getPlayer().playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 1, 1);
                return ActionResult.SUCCESS;
            }

            return ActionResult.FAIL;
        }

        if (bag.getCount() > 1) {
            bag = bag.split(1);
            setContents(bag, builder.build());
            context.getPlayer().getInventory().insertStack(bag);
        } else {
            setContents(bag, builder.build());
        }
        context.getPlayer().playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
        return ActionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient() || !selected || stack.getCount() != 1) {
            return;
        }

        if (entity instanceof PlayerEntity player) {
            Inventory inv = player.getInventory();

            if (inv.getStack(slot) != stack) {
                return;
            }

            System.out.println(slot);

            Contents contents = getContents(stack);
            if (contents.isFull() || contents.isEmpty()) {
                return;
            }

            Contents.Builder builder = new Contents.Builder(contents);


            boolean changed = false;

            if (slot < inv.size() - 1) {
                changed |= builder.add(inv.getStack(slot + 1));
            }
            if (slot > 0) {
                changed |= builder.add(inv.getStack(slot - 1));
            }

            if (changed) {
                player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, player.getSoundCategory(), 1, 1);
                setContents(stack, builder.build());
                inv.setStack(slot, stack);
            }
        }
    }

    private static ItemStack removeItems(ItemStack bag, Contents contents, int count) {
        ItemStack dispensed = Compat119.copyWithCount(contents.stack(), Math.min(contents.count(), count));
        setContents(bag, new Contents(contents.stack(), contents.count() - dispensed.getCount()));
        return dispensed;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Contents contents = getContents(stack);
        if (!contents.isEmpty()) {
            tooltip.add(Text.literal(contents.count() + " x ").append(contents.stack().getName()));
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        Contents contents = getContents(stack);
        if (!contents.isEmpty()) {
            return Text.translatable(getTranslationKey(stack) + ".filled", contents.stack().getName());
        }
        return super.getName(stack);
    }

    private boolean canPickUp(ItemStack incomingStack) {
        return incomingStack.isIn(PSTags.Items.CAN_GO_INTO_PAPER_BAG);
    }

    public static Contents getContents(ItemStack stack) {
        return Optional.ofNullable(stack.getSubNbt("contents")).map(Contents::fromNbt).orElse(Contents.EMPTY);
    }

    public static void setContents(ItemStack stack, Contents contents) {
        contents.writeNbt(stack.getOrCreateSubNbt("contents"));
    }

    public static int getMaxCountForItem(Item item) {
        if (item instanceof BottleItem) {
            return 1;
        }
        return 64 * 1000;
    }

    public record Contents(ItemStack stack, int count) {
        static final Contents EMPTY = new Contents(ItemStack.EMPTY, 0);

        public static Contents fromNbt(NbtCompound nbt) {
            return from(ItemStack.fromNbt(nbt.getCompound("stack")), Math.max(0, nbt.getInt("count")));
        }

        public static Contents from(ItemStack stack, int count) {
            if (count <= 0 || stack.isEmpty()) {
                return EMPTY;
            }
            return new Contents(stack, count);
        }

        public boolean isFull() {
            return count >= getMaxCountForItem(stack.getItem());
        }

        public boolean isEmpty() {
            return stack.isEmpty() || count <= 0;
        }

        public NbtCompound writeNbt(NbtCompound nbt) {
            nbt.put("stack", stack.writeNbt(new NbtCompound()));
            nbt.putInt("count", count);
            return nbt;
        }

        static class Builder {
            private ItemStack stack;
            private int count;

            public Builder(Contents contents) {
                stack = contents.stack();
                count = contents.count();
            }

            public boolean canAdd(ItemStack stack) {
                if (this.stack.isEmpty()) {
                    return true;
                }
                if (stack.getItem() instanceof PaperBagItem) {
                    return canAdd(getContents(stack).stack());
                }
                return (this.stack.isEmpty() || ItemStack.canCombine(this.stack, stack)) && count < getMaxCountForItem(stack.getItem());
            }

            public boolean add(ItemStack stack) {
                if (stack.getItem() instanceof PaperBagItem) {
                    Builder builder = new Builder(getContents(stack));
                    this.stack = builder.stack.copy();
                    count += builder.split(getMaxCountForItem(builder.stack.getItem()) - count).getCount();
                    setContents(stack, builder.build());
                    return true;
                }
                if (canAdd(stack)) {
                    this.stack = Compat119.copyWithCount(stack, 1);
                    count += stack.split(getMaxCountForItem(stack.getItem()) - count).getCount();
                    return true;
                }
                return false;
            }

            public ItemStack split(int count) {
                ItemStack dispensed = Compat119.copyWithCount(stack, Math.min(this.count, count));
                this.count -= dispensed.getCount();
                return dispensed;
            }

            public Contents build() {
                return Contents.from(stack, count);
            }
        }
    }
}
