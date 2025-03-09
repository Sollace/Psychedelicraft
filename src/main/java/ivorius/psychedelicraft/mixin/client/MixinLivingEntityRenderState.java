package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugPropertiesContainer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

@Mixin(LivingEntityRenderState.class)
abstract class MixinLivingEntityRenderState implements DrugPropertiesContainer.Mutable {
    private DrugProperties psychedelicraft_drugProperties;

    public DrugProperties getDrugProperties() {
        return psychedelicraft_drugProperties;
    }

    @Override
    public void setDrugProperties(DrugProperties properties) {
        psychedelicraft_drugProperties = properties;
    }
}
