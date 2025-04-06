package ivorius.psychedelicraft.mixin.compat;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.util.compat.ItemSubPredicate;
import ivorius.psychedelicraft.util.compat.StackCompat;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.NbtPredicate;

@Mixin(NbtPredicate.class)
abstract class MixinNbtPredicate {
    @Shadow @Mutable
    private @Final NbtCompound nbt;

    @Unique @Nullable
    private List<ItemSubPredicate<?>> itemSubPredicates;

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    private void onInit(NbtCompound nbt) {
        if (nbt.contains("psychedelicraft:sub_predicates", NbtElement.COMPOUND_TYPE)) {
            itemSubPredicates = ItemSubPredicate.readNbt(nbt.getCompound("psychedelicraft:sub_predicates"));
            this.nbt.remove("psychedelicraft:sub_predicates");
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "test(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
    private void onTest(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        if (itemSubPredicates != null && info.getReturnValue()) {
            info.setReturnValue(itemSubPredicates.stream().allMatch(predicate -> {
                var value = StackCompat.get(stack, predicate.getComponentType());
                return value != null && ((ItemSubPredicate<Object>)predicate).test(stack, value);
            }));
        }
    }
}
