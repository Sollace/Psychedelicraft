package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.util.SignType;

@Mixin(SignType.class)
public interface SignTypeAccessor {
    @Invoker("register")
    static SignType callRegister(SignType type) {
        return type;
    }
}
