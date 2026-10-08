package ivorius.psychedelicraft.client.particle;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

public class BitsParticle extends BillboardParticle {

    public BitsParticle(SimpleParticleType effect, SpriteProvider spriteProvider, ClientWorld world, double x, double y, double z, double velocityX, double velocityY,
            double velocityZ, Random random) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.getSprite(random));
        scale *= 0.2F;
        scale += random.nextFloat() * 0.03F;
        this.velocityX *= 0.3F;
        this.velocityY *= 0.1F;
        this.velocityZ *= 0.3F;
        this.alpha = 0.8F;
    }

    @Override
    protected RenderType getRenderType() {
        return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

}
