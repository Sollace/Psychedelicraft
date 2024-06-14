package ivorius.psychedelicraft.fluid.container;

import java.util.List;

import org.jetbrains.annotations.UnmodifiableView;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ItemFluidsStackStorage implements ContainerItemContext {

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        return 0;
    }

    @Override
    public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return List.of();
    }

}
