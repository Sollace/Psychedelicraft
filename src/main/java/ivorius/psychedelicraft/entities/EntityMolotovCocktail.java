/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities;

import ivorius.psychedelicraft.fluids.ExplodingFluid;
import ivorius.psychedelicraft.items.ItemMolotovCocktail;
import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.World.ExplosionSourceType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

public class EntityMolotovCocktail extends ThrownItemEntity {

    public EntityMolotovCocktail(EntityType<EntityMolotovCocktail> type, World world) {
        super(type, world);
    }

    public EntityMolotovCocktail(World world, LivingEntity owner) {
        super(PSEntityList.MOLOTOV_COCKTAIL, owner, world);
    }

    public EntityMolotovCocktail(World world, double x, double y, double z) {
        super(PSEntityList.MOLOTOV_COCKTAIL, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return PSItems.molotovCocktail;
    }

    @Override
    public void tick() {
        super.tick();
        spawnParticles(1);
    }

    private void spawnParticles(float spread) {
        world.addParticle(ParticleTypes.FLAME,
                world.random.nextTriangular(getX(), 0.5 * spread),
                world.random.nextTriangular(getY(), 0.5 * spread),
                world.random.nextTriangular(getZ(), 0.5 * spread),
                0, 0, 0
        );

        world.addParticle(ParticleTypes.LAVA,
                world.random.nextTriangular(getX(), 0.5 * spread),
                world.random.nextTriangular(getY() + getHeight(), 0.5 * spread),
                world.random.nextTriangular(getZ(), 0.5 * spread),
                0, 0, 0
        );
    }


    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        //ExplodingFluid fluid = ItemMolotovCocktail.getExplodingFluid(getItem());
        // TODO Implement hit damage
        //       (Sollace edit "I assume based on the explosion strength and velocity, right? Right??)
        //        It was already implemented with 4 hit damage so that's the only thing I can imagine...
        hit.getEntity().damage(DamageSource.thrownProjectile(this, getOwner()), 4);
    }


    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (world.isClient) {
            return;
        }

        playSound(SoundEvents.BLOCK_GLASS_BREAK, 1, 1);

        float explosionStrength = 0;
        float fireStrength = 0;

        ExplodingFluid fluid = ItemMolotovCocktail.getExplodingFluid(getItem());
        if (fluid != null) {
            explosionStrength = fluid.explosionStrength(getItem());
            fireStrength = fluid.fireStrength(getItem());
        }

        for (int i = 0; i < fireStrength * 2; i++) {
            world.addParticle(ParticleTypes.FLAME,
                    world.random.nextTriangular(getX(), 0.5 * fireStrength),
                    world.random.nextTriangular(getY(), 0.5 * fireStrength),
                    world.random.nextTriangular(getZ(), 0.5 * fireStrength),
                    0, 0, 0
            );

            world.addParticle(ParticleTypes.LAVA,
                    world.random.nextTriangular(getX(), 0.5 * fireStrength),
                    world.random.nextTriangular(getY() + getHeight(), 0.5 * fireStrength),
                    world.random.nextTriangular(getZ(), 0.5 * fireStrength),
                    0, 0, 0
            );
        }

        world.createExplosion(
                this,
                DamageSource.thrownProjectile(this, getOwner()),
                new ExplosionBehavior() {
                    @Override
                    public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
                        return state.isReplaceable();
                    }
                },
                getPos(),
                explosionStrength,
                fireStrength > 0, ExplosionSourceType.MOB
        );
    }
}
