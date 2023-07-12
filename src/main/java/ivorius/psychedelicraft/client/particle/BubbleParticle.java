package ivorius.psychedelicraft.client.particle;

import net.minecraft.util.math.Vec3f;

import ivorius.psychedelicraft.particle.BubbleParticleEffect;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

public class BubbleParticle extends SpriteBillboardParticle {
    BubbleParticle(BubbleParticleEffect effect, SpriteProvider spriteProvider, ClientWorld world,
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

        Vec3f color = effect.getColor();
        red = color.getX();
        green = color.getY();
        blue = color.getZ();
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
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }
}