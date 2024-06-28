/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import java.util.Optional;

import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class SimpleDrug implements Drug {
    static <T extends SimpleDrug> P4<RecordCodecBuilder.Mu<T>, Double, Double, Boolean, Integer> fillCodecFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                Codec.DOUBLE.fieldOf("effect").forGetter(SimpleDrug::getDesiredValue),
                Codec.DOUBLE.fieldOf("effectActive").forGetter(SimpleDrug::getActiveValue),
                Codec.BOOL.fieldOf("locked").forGetter(SimpleDrug::isLocked),
                Codec.INT.fieldOf("ticksActive").forGetter(SimpleDrug::getTicksActive)
        );
    }
    public static <T extends SimpleDrug> MapCodec<T> createCodec(DrugType<T> type) {
        return RecordCodecBuilder.<T>mapCodec(instance -> {
            return SimpleDrug.<T>fillCodecFields(instance).apply(instance, (effect, effectActive, locked, ticksActive) -> {
                var i = type.create();
                i.setActiveValue(effectActive);
                i.setDesiredValue(effect);
                i.setLocked(locked);
                i.setTicksActive(ticksActive);
                return i;
            });
        });
    }

    protected double effect;
    protected double effectActive;
    protected boolean locked = false;

    private final double decreaseSpeed;
    private final double decreaseSpeedPlus;
    private final boolean invisible;

    private final DrugType<? extends SimpleDrug> type;

    private int ticksActive;

    public SimpleDrug(DrugType<? extends SimpleDrug> type, double decSpeed, double decSpeedPlus) {
        this(type, decSpeed, decSpeedPlus, false);
    }

    public SimpleDrug(DrugType<? extends SimpleDrug> type, double decSpeed, double decSpeedPlus, boolean invisible) {
        this.type = type;
        decreaseSpeed = decSpeed;
        decreaseSpeedPlus = decSpeedPlus;

        this.invisible = invisible;
    }

    @Override
    public final DrugType<? extends SimpleDrug> getType() {
        return type;
    }

    public void setActiveValue(double value) {
        effectActive = value;
    }

    @Override
    public double getActiveValue() {
        return effectActive;
    }

    public double getDesiredValue() {
        return effect;
    }

    @Override
    public int getTicksActive() {
        return ticksActive;
    }

    protected void setTicksActive(int ticksActive) {
        this.ticksActive = ticksActive;
    }

    @Override
    public void setDesiredValue(double value) {
        effect = value;
    }

    @Override
    public void addToDesiredValue(double value) {
        if (!locked) {
            effect += value;
        }
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean isVisible() {
        return !invisible;
    }

    @Override
    public final void update(DrugProperties properties) {
        if (getActiveValue() > 0) {
            ticksActive++;
            if (ticksActive == 1) {
                properties.markDirty();
            }

            if (!properties.asEntity().getWorld().isClient) {
                if (tickSideEffects(properties, properties.asEntity().getWorld().random)) {
                    reset(properties);
                    properties.markDirty();
                }
            } else {
                tickClientEffects(properties, properties.asEntity().getWorld().random);
            }
        } else {
            ticksActive = 0;
        }

        if (!locked) {
            effect *= decreaseSpeed;
            effect -= decreaseSpeedPlus;
        }

        effect = MathHelper.clamp(effect, 0, 1);
        setActiveValue(MathUtils.nearValue(effectActive, effect, 0.05, 0.005));
    }

    protected boolean tickSideEffects(DrugProperties properties, Random random) {
        if (Drug.HEART_BEAT_SPEED.get(properties) > 3) {
            properties.asEntity().damage(properties.damageOf(PSDamageTypes.HEART_ATTACK), Integer.MAX_VALUE);
            return true;
        }
        return false;
    }

    protected void tickClientEffects(DrugProperties properties, Random random) {

    }

    @Override
    public void onWakeUp(DrugProperties drugProperties) {
        reset(drugProperties);
    }

    @Override
    public void reset(DrugProperties drugProperties) {
        if (!locked) {
            effect = 0;
        }
    }

    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        setDesiredValue(compound.getDouble("effect"));
        setActiveValue(compound.getDouble("effectActive"));
        setLocked(compound.getBoolean("locked"));
        ticksActive = compound.getInt("ticksActive");
    }

    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        compound.putDouble("effect", getDesiredValue());
        compound.putDouble("effectActive", getActiveValue());
        compound.putBoolean("locked", isLocked());
        compound.putInt("ticksActive", getTicksActive());
    }

    @Override
    public Optional<Text> trySleep(BlockPos pos) {
        return Optional.empty();
    }

    protected static void rotateEntityPitch(Entity entity, double amount) {
        entity.setPitch((float)MathHelper.clamp(entity.getPitch() + amount, -90, 90));
    }

    protected static void rotateEntityYaw(Entity entity, double amount) {
        entity.setYaw((entity.getYaw() + (float)amount) % 360);
        entity.prevYaw = entity.getYaw();
    }
}
