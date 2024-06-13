/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.fluid.Combustable;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
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

public class MolotovCocktailEntity extends ThrownItemEntity {

    public MolotovCocktailEntity(EntityType<MolotovCocktailEntity> type, World world) {
        super(type, world);
    }

    public MolotovCocktailEntity(World world, LivingEntity owner) {
        super(PSEntities.MOLOTOV_COCKTAIL, owner, world);
    }

    public MolotovCocktailEntity(World world, double x, double y, double z) {
        super(PSEntities.MOLOTOV_COCKTAIL, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return PSItems.MOLOTOV_COCKTAIL;
    }

    @Override
    public ItemStack getStack() {
        ItemStack stack = super.getStack();
        stack.setHolder(this);
        return stack;
    }

    @Override
    public void tick() {
        super.tick();
        if (Combustable.fromStack(getStack()).getFireStrength(getStack()) > 0) {
            spawnParticles(1);
        }
    }

    private void spawnParticles(float spread) {
        getWorld().addParticle(ParticleTypes.FLAME,
                getWorld().getRandom().nextTriangular(getX(), 0.5 * spread),
                getWorld().getRandom().nextTriangular(getY(), 0.5 * spread),
                getWorld().getRandom().nextTriangular(getZ(), 0.5 * spread),
                0, 0, 0
        );

        getWorld().addParticle(ParticleTypes.LAVA,
                getWorld().getRandom().nextTriangular(getX(), 0.5 * spread),
                getWorld().getRandom().nextTriangular(getY() + getHeight(), 0.5 * spread),
                getWorld().getRandom().nextTriangular(getZ(), 0.5 * spread),
                0, 0, 0
        );
    }


    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        damageEntity(hit.getEntity(), 1);
        float explosionStrength = Combustable.fromStack(getStack()).getExplosionStrength(getStack());
        if (explosionStrength > 0) {
            getWorld().getOtherEntities(this, getBoundingBox().expand(explosionStrength), i -> i.distanceTo(this) <= explosionStrength).forEach(e -> {
                damageEntity(hit.getEntity(), 1 - (e.distanceTo(this) / explosionStrength));
            });
        }
    }

    private void damageEntity(Entity entity, float percentageScale) {
        Combustable combustable = Combustable.fromStack(getStack());
        float explosionStrength = combustable.getExplosionStrength(getStack());
        float fireStrength = combustable.getFireStrength(getStack());
        entity.damage(PSDamageTypes.create(getWorld(), getOwner(), this, PSDamageTypes.molotov(entity, getOwner())), percentageScale * Math.max(4, explosionStrength * 0.6F + fireStrength * 0.3F));
        if (fireStrength > 0) {
            entity.isOnFire();
            entity.setFireTicks((int)Math.max(10, 3 * fireStrength));
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (getWorld().isClient || isRemoved()) {
            return;
        }

        playSound(SoundEvents.BLOCK_GLASS_BREAK, 1, 1);

        Combustable combustable = Combustable.fromStack(getStack());
        float explosionStrength = combustable.getExplosionStrength(getStack());
        float fireStrength = combustable.getFireStrength(getStack());

        if (fireStrength > 0) {
            for (int i = 0; i < fireStrength * 2; i++) {
                getWorld().addParticle(ParticleTypes.FLAME,
                        getWorld().getRandom().nextTriangular(getX(), 0.5 * fireStrength),
                        getWorld().getRandom().nextTriangular(getY(), 0.5 * fireStrength),
                        getWorld().getRandom().nextTriangular(getZ(), 0.5 * fireStrength),
                        0, 0, 0
                );

                getWorld().addParticle(ParticleTypes.LAVA,
                        getWorld().getRandom().nextTriangular(getX(), 0.5 * fireStrength),
                        getWorld().getRandom().nextTriangular(getY() + getHeight(), 0.5 * fireStrength),
                        getWorld().getRandom().nextTriangular(getZ(), 0.5 * fireStrength),
                        0, 0, 0
                );
            }
        }

        if (explosionStrength > 0) {
            getWorld().createExplosion(
                    this,
                    getDamageSources().thrown(this, getOwner()),
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
        } else {
            playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, 1);
        }

        discard();
    }
}
