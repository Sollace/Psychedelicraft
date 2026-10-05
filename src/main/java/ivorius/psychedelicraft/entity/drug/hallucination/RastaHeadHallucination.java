/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.ai.control.Control;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class RastaHeadHallucination extends Hallucination implements Control, ChatBot.MessageEmitter {
    private final float distance;

    private float planeRotationX;
    private float planeRotationZ;

    private int id;
    private int maxAge;
    public float scale;

    private float lastPitch;
    private float pitch;

    private float lastYaw;
    private float yaw;

    private Vec3d velocity = Vec3d.ZERO;
    private Vec3d position;
    private Vec3d lastPosition;

    private final ChatBot chatBot;

    private int lastTalkingTicks;
    private int talkingTicks;

    public RastaHeadHallucination(PlayerEntity playerEntity) {
        super(playerEntity);

        id = random.nextInt();
        maxAge = (random.nextInt(59) + 120) * 20;
        scale = 1 + random.nextFloat() / 2F;
        distance = 2 + random.nextFloat() * 5;

        planeRotationX = random.nextFloat() * 180;
        planeRotationZ = random.nextFloat() * 180;

        position = getTargetPosition();
        lastPosition = position;

        chatBot = new ChatBot(new RastaheadPersonality(), playerEntity, this);
    }

    @Override
    public ChatBot getChatBot() {
        return chatBot;
    }

    public Vec3d getPosition(float tickDelta) {
        return lastPosition.add(position.subtract(lastPosition).multiply(tickDelta));
    }

    public float getPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, lastPitch, pitch) + MathHelper.sin((age + tickDelta) / 2F) * 90 * getTalkingTime(tickDelta);
    }

    public float getYaw(float tickDelta) {
        return MathHelper.lerp(tickDelta, lastYaw, yaw);
    }

    public float getTalkingTime(float tickDelta) {
        return MathHelper.lerp(tickDelta, lastTalkingTicks, talkingTicks) / 20F;
    }

    public int getLight() {
        return player.getEntityWorld().getLightLevel(BlockPos.ofFloored(position));
    }

    @Override
    public void update(float alpha) {
        super.update(alpha);

        lastPitch = pitch;
        lastYaw = yaw;
        lastPosition = position;

        position = position.add(velocity);

        planeRotationX = MathHelper.wrapDegrees(planeRotationX + 3);
        planeRotationZ = MathHelper.wrapDegrees(planeRotationZ + 3);

        lastTalkingTicks = talkingTicks;
        if (talkingTicks > 0) {
            talkingTicks--;
        }

        Vec3d wanted = getTargetPosition();
        velocity = wanted.subtract(position).normalize().multiply(Math.log((float)wanted.distanceTo(position)));

        Vec3d positionDifference = player.getEyePos().subtract(position);

        getTargetPitch(positionDifference).ifPresent(angle -> pitch = angle);
        getTargetYaw(positionDifference).ifPresent(angle -> yaw = angle);

        chatBot.tick();
    }

    protected Vec3d getTargetPosition() {
        int seed = player.age + (id * 3);
        Vec3d offset = new Vec3d(
                MathHelper.sin(seed / 50F) * distance,
                MathHelper.sin(seed / 10F) + (id % 5) - 1,
                MathHelper.cos(seed / 50F) * distance
        ).rotateY(planeRotationX * MathHelper.RADIANS_PER_DEGREE).rotateZ(planeRotationZ * MathHelper.RADIANS_PER_DEGREE);
        return player.getEyePos().add(offset);
    }

    protected Optional<Float> getTargetPitch(Vec3d positionDifference) {
        double horDistance = positionDifference.multiply(1, 0, 1).length();
        if (!(Math.abs(positionDifference.getY()) > MathHelper.EPSILON) && !(Math.abs(horDistance) > MathHelper.EPSILON)) {
            return Optional.empty();
        }
        return Optional.of((float)-MathHelper.atan2(positionDifference.getY(), horDistance));
    }

    protected Optional<Float> getTargetYaw(Vec3d positionDifference) {
        if (!(Math.abs(positionDifference.getZ()) > MathHelper.EPSILON) && !(Math.abs(positionDifference.getX()) > MathHelper.EPSILON)) {
            return Optional.empty();
        }
        return Optional.of((float)MathHelper.atan2(
            positionDifference.getZ(),
            positionDifference.getX()
        ) * MathHelper.DEGREES_PER_RADIAN - 90);
    }

    @Override
    public int getMaxHallucinations() {
        return UNLIMITED;
    }

    @Override
    public boolean isDead() {
        return age >= maxAge;
    }

    @Override
    public @Nullable Identifier getType() {
        return HallucinationTypeKeys.RASTA_HEAD;
    }

    @Override
    public void onEmitMessage(String sender, Text message) {
        if (player.getEntityWorld().getRandom().nextFloat() < 0.3F || message.getString().contains("!")) {
            float x = player.getEntityWorld().getRandom().nextFloat();
            float z = player.getEntityWorld().getRandom().nextFloat();
            player.animateDamage((float)(MathHelper.atan2(z, x) * 57.2957763671875 - player.getYaw()));
            player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1, 1);
            player.takeKnockback(0.2F, x, z);
        }
        talkingTicks = 20;
    }
}
