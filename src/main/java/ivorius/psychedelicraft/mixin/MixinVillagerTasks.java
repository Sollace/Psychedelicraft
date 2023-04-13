package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.entity.DealerTaskListProvider;
import ivorius.psychedelicraft.entity.PSTradeOffers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.LoseJobOnSiteLossTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;

@Mixin(LoseJobOnSiteLossTask.class)
abstract class MixinLoseJobOnSiteLossTask {
    @Inject(method = "method_47038(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;J)Z", at = @At("HEAD"), cancellable = true)
    private static void onTryLoseJobSite(ServerWorld world, VillagerEntity entity, long time,
            CallbackInfoReturnable<Boolean> info) {
        if (entity.getVillagerData().getProfession() == PSTradeOffers.DRUG_ADDICT_PROFESSION) {
            info.setReturnValue(false);
        }
    }
}

@Mixin(VillagerTaskListProvider.class)
abstract class MixinVillagerTaskListProvider {
    @Shadow
    static Pair<Integer, Task<LivingEntity>> createBusyFollowTask() { return null; }

    @Inject(method = "createWorkTasks(Lnet/minecraft/village/VillagerProfession;F)Lcom/google/common/collect/ImmutableList;", at = @At("HEAD"), cancellable = true)
    private static void onCreateWorkTasks(VillagerProfession profession, float speed,
            CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> info) {
        if (profession == PSTradeOffers.DRUG_DEALER_PROFESSION) {
            info.setReturnValue(DealerTaskListProvider.createWorkTasks(createBusyFollowTask(), speed));
        }
    }
}