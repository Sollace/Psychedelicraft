package ivorius.psychedelicraft.client.particle;

import org.joml.Vector3f;

import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;

public class FluidBubbleParticle extends SpriteBillboardParticle {

    private boolean survivesInAir;

    FluidBubbleParticle(FluidParticleEffect effect, SpriteProvider spriteProvider, ClientWorld world, double x, double y, double z, double vX, double vY, double vZ) {
        this(spriteProvider, world, x, y, z, vX, vY, vZ);
        PSParticleFactories.setColor(this, effect);
    }

    FluidBubbleParticle(DrugDustParticleEffect effect, SpriteProvider spriteProvider, ClientWorld world, double x, double y, double z, double vX, double vY, double vZ) {
        this(spriteProvider, world, x, y, z, vX, vY, vZ);
        Vector3f color = effect.getColor();
        red = color.x;
        green = color.y;
        blue = color.z;
        survivesInAir = true;
    }

    private FluidBubbleParticle(SpriteProvider spriteProvider, ClientWorld world,
            double x, double y, double z,
            double vX, double vY, double vZ) {
        super(world, x, y, z);
        setSprite(spriteProvider);
        setBoundingBoxSpacing(0.02F, 0.02F);
        scale *= this.random.nextFloat() * 0.6F + 0.2F;
        velocityX = vX * 0.2F + (Math.random() * 2 - 1) * 0.02F;
        velocityY = vY * 0.2F + (Math.random() * 2 - 1) * 0.02F;
        velocityZ = vZ * 0.2F + (Math.random() * 2 - 1) * 0.02F;
        maxAge = (int)(8 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public void tick() {
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
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
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }
}