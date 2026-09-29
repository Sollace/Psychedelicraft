package ivorius.psychedelicraft.mixin.client;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.hallucination.EntityIdentitySwapHallucination;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderManager.class)
abstract class MixinEntityRenderDispatcher {
    @ModifyVariable(method = "getAndUpdateRenderState", at = @At("HEAD"), argsOnly = true)
    private Entity swapEntity(Entity entity) {
        return DrugProperties.of((Entity)MinecraftClient.getInstance().player)
            .stream()
            .flatMap(properties -> properties.getHallucinations()
                    .getEntities().<EntityIdentitySwapHallucination>getHallucinations(EntityIdentitySwapHallucination.class).stream()
            )
            .map(h -> h.matchOrAttach(entity))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(entity);
    }
}
