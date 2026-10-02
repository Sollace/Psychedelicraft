package ivorius.psychedelicraft.client.particle;

import org.joml.Vector3f;

import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class FluidBubbleParticle extends BillboardParticle {

    private boolean survivesInAir;

    FluidBubbleParticle(FluidParticleEffect effect, SpriteProvider spriteProvider, ClientWorld world, double x, double y, double z, double vX, double vY, double vZ, Random random) {
        this(spriteProvider.getSprite(random), world, x, y, z, vX, vY, vZ);
        PSParticleFactories.setColor(this, effect);
    }

    FluidBubbleParticle(DrugDustParticleEffect effect, SpriteProvider spriteProvider, ClientWorld world, double x, double y, double z, double vX, double vY, double vZ, Random random) {
        this(spriteProvider.getSprite(random), world, x, y, z, vX, vY, vZ);
        Vector3f color = effect.getColor();
        red = color.x;
        green = color.y;
        blue = color.z;
        survivesInAir = true;
    }

    private FluidBubbleParticle(Sprite sprite, ClientWorld world,
            double x, double y, double z,
            double vX, double vY, double vZ) {
        super(world, x, y, z, sprite);
        setBoundingBoxSpacing(0.02F, 0.02F);
        scale *= this.random.nextFloat() * 0.6F + 0.2F;
        velocityX = vX * 0.2F + (Math.random() * 2 - 1) * 0.02F;
        velocityY = vY * 0.2F + (Math.random() * 2 - 1) * 0.02F;
        velocityZ = vZ * 0.2F + (Math.random() * 2 - 1) * 0.02F;
        maxAge = (int)(8 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public void tick() {
        lastX = x;
        lastY = y;
        lastZ = z;
        if (maxAge-- <= 0) {
            markDead();
            return;
        }
        velocityY += 0.002;
        move(velocityX, velocityY, velocityZ);
        velocityX *= 0.85F;
        velocityY *= 0.85F;
        velocityZ *= 0.85F;
        if (!survivesInAir && !world.getFluidState(BlockPos.ofFloored(this.x, this.y, this.z)).isIn(FluidTags.WATER)) {
            markDead();
        }
    }

    @Override
    protected BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_OPAQUE;
    }
}