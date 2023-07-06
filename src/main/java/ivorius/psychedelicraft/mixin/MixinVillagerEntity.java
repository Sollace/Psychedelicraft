package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.entity.PSTradeOffers;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;

@Mixin(VillagerEntity.class)
abstract class MixinVillagerEntity extends MerchantEntity implements VillagerDataContainer {
    MixinVillagerEntity() { super(null, null); }

    @Inject(method = "interactMob",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onInteractMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(PSItems.HASH_MUFFIN) && !isBaby()) {
            VillagerProfession profession = getVillagerData().getProfession();
            if (profession == VillagerProfession.NITWIT || profession == VillagerProfession.NONE) {
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                if (!getWorld().isClient) {
                    getWorld().playSoundFromEntity(null, this, SoundEvents.ENTITY_GENERIC_EAT, getSoundCategory(),
                            1 + random.nextFloat(),
                            random.nextFloat() * 0.7F + 0.3F
                    );
                    setVillagerData(getVillagerData().withProfession(PSTradeOffers.DRUG_ADDICT_PROFESSION));
                    ((VillagerEntity)(Object)this).reinitializeBrain((ServerWorld)getWorld());
                    PSCriteria.FEED_VILLAGER.trigger(player);
                }
                info.setReturnValue(ActionResult.SUCCESS);
            } else {
                info.setReturnValue(ActionResult.CONSUME);
            }
        }
    }

    @Inject(method = "afterUsing", at = @At("RETURN"))
    private void onAfterUsing(TradeOffer offer, CallbackInfo info) {
        if (getVillagerData().getProfession() == PSTradeOffers.DRUG_ADDICT_PROFESSION) {
            damage(PSDamageTypes.create(getWorld(), PSDamageTypes.OVERDOSE), (offer.getUses() * offer.getSellItem().getCount()) + 1);
        }
    }
}
