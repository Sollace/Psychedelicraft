package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.item.component.BagContentsComponent;
import ivorius.psychedelicraft.item.component.PSComponents;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
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
                BagContentsComponent contents = BagContentsComponent.get(stack);
                if (contents.isEmpty() || stack.getCount() > 1) {
                    return super.dispenseSilently(pointer, stack);
                }
                BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);
                ItemStack dispensed = builder.split(1);
                BagContentsComponent.set(stack, builder);
                return super.dispenseSilently(pointer, dispensed);
            }
        });
    }

    @Override
    public boolean onStackClicked(ItemStack bag, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT || bag.getCount() != 1) {
            return false;
        }

        BagContentsComponent contents = BagContentsComponent.get(bag);

        ItemStack slotStack = slot.getStack();

        if (slotStack.isEmpty()) {
            if (!contents.isEmpty()) {
                BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);
                // dispense into empty slot
                builder.add(slot.insertStack(builder.split(contents.stack().getMaxCount())));
                player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 1, 1);
                BagContentsComponent.set(bag, builder);
                return true;
            }
        } else if (canPickUp(slotStack)) {
            BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);
            if (builder.canAdd(slotStack)) {
                // pick up from filled slot
                int maxTaken = BagContentsComponent.getMaxCountForItem(slotStack.getItem()) - contents.count();

                builder.add(slot.takeStackRange(slotStack.getCount(), maxTaken, player));
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
                BagContentsComponent.set(bag, builder);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack clickedStack, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !(cursorStack.getItem() instanceof PaperBagItem || slot.canTakePartial(player)) || clickedStack.getCount() != 1) {
            return false;
        }

        BagContentsComponent contents = BagContentsComponent.get(clickedStack);
        BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);

        if (cursorStack.isEmpty()) {
            // remove from bag into held stack
            cursorStackReference.set(builder.split(64));
            player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
            BagContentsComponent.set(clickedStack, builder);
            return true;
        }

        if (builder.canAdd(cursorStack)) {
            int maxTaken = BagContentsComponent.getMaxCountForItem(cursorStack.getItem()) - contents.count();

            if (canPickUp(cursorStack)) {
                // insert into bag from held stack
                builder.add(cursorStack.split(maxTaken));
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
                BagContentsComponent.set(clickedStack, builder);
                return true;
            }

            // bag to bag transfer
            /*
            if (cursorStack.getItem() instanceof PaperBagItem) {
                Contents.Builder cursorContents = new Contents.Builder(getContents(cursorStack));
                builder.add(cursorContents.split(maxTaken));
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
                setContents(clickedStack, builder.build());
                setContents(cursorStack, cursorContents.build());
                return true;
            }
            */
        }

        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack bag = user.getStackInHand(hand);
        ItemStack swithdrawn = BagContentsComponent.withdraw(bag, user.isSneaky() ? 1 : BagContentsComponent.get(bag).stack().getMaxCount());

        if (!swithdrawn.isEmpty()) {
            user.dropItem(swithdrawn, false, false);
            user.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 1, 1);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(bag, world.isClient());
        }
        return TypedActionResult.fail(bag);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack bag = context.getStack();

        BagContentsComponent contents = BagContentsComponent.get(bag);

        BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);

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
                context.getPlayer().dropItem(builder.split(context.getPlayer().isSneaky() ? 1 : contents.stack().getMaxCount()), false, false);
                bag.set(PSComponents.BAG_CONTENTS, builder.build());
                context.getPlayer().playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 1, 1);
                return ActionResult.SUCCESS;
            }
            bag.set(PSComponents.BAG_CONTENTS, builder.build());
            return ActionResult.FAIL;
        }

        if (bag.getCount() > 1) {
            bag = bag.split(1);
            bag.set(PSComponents.BAG_CONTENTS, builder.build());
            context.getPlayer().getInventory().insertStack(bag);
        } else {
            bag.set(PSComponents.BAG_CONTENTS, builder.build());
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

            BagContentsComponent contents = BagContentsComponent.get(stack);
            if (contents.isFull() || contents.isEmpty()) {
                return;
            }

            BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);

            boolean changed = false;

            if (slot < inv.size() - 1) {
                changed |= builder.add(inv.getStack(slot + 1));
            }
            if (slot > 0) {
                changed |= builder.add(inv.getStack(slot - 1));
            }

            if (changed) {
                player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 1, 1);
                stack.set(PSComponents.BAG_CONTENTS, builder.build());
                inv.setStack(slot, stack);
            }
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        if (!BagContentsComponent.get(stack).isEmpty()) {
            return Text.translatable(getTranslationKey(stack) + ".filled", BagContentsComponent.get(stack).stack().getName());
        }
        return super.getName(stack);
    }

    private boolean canPickUp(ItemStack incomingStack) {
        return incomingStack.isIn(PSTags.Items.CAN_GO_INTO_PAPER_BAG);
    }
}
