package ivorius.psychedelicraft.item.component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.joml.Vector3f;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.util.RaytraceUtil;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

public record ItemDrugs(List<DrugInfluence> influences, Optional<Vector3f> smokeColor) implements TooltipAppender {
    public static final Vector3f DEFAULT_SMOKE_COLOR = new Vector3f(1, 1, 1);

    public static final ItemDrugs EMPTY = new ItemDrugs(List.of(), Optional.empty());
    public static final Codec<ItemDrugs> CODEC = RecordCodecBuilder.create(i -> i.group(
            DrugInfluence.CODEC.listOf().fieldOf("influences").forGetter(ItemDrugs::influences),
            DrugInfluence.COLOR_CODEC.optionalFieldOf("smoke_color").forGetter(ItemDrugs::smokeColor)
    ).apply(i, ItemDrugs::of));
    public static final PacketCodec<RegistryByteBuf, ItemDrugs> PACKET_CODEC = PacketCodec.tuple(
            DrugInfluence.PACKET_CODEC.collect(PacketCodecs.toList()), ItemDrugs::influences,
            DrugInfluence.COLOR_PACKET_CODEC, ItemDrugs::smokeColor,
            ItemDrugs::of
    );

    public static ItemDrugs of(List<DrugInfluence> influences) {
        return of(influences, Optional.empty());
    }

    public static ItemDrugs of(List<DrugInfluence> influences, Optional<Vector3f> smokeColor) {
        return new ItemDrugs(influences, smokeColor);
    }

    public static ItemDrugs of(DrugInfluence...influences) {
        return of(List.of(influences));
    }

    public ItemDrugs {
        influences = List.copyOf(influences);
    }

    public ItemDrugs withSmoke(Vector3f smokeColor) {
        return new ItemDrugs(influences, Optional.of(smokeColor));
    }

    public static ItemDrugs get(ItemStack stack) {
        return stack.getOrDefault(PSComponents.DRUGS, EMPTY);
    }

    public void applyTo(ItemStack stack, DrugProperties properties) {
        Impurities impurities = Impurities.get(stack);
        properties.addAll(impurities.modifyEffects(influences));
        if (impurities.impurities().contains(Impurities.Impurity.SILICA)) {
            properties.asEntity().damage(properties.damageOf(PSDamageTypes.GLASS_SHARD), 1.5F);
            properties.asEntity().playSound(PSSounds.ITEM_BROKEN_GLASS_EAT);
        }
        smokeColor.ifPresent(smokeColor -> {
            properties.startBreathingSmoke(10 + properties.asEntity().getWorld().random.nextInt(10), smokeColor);
            properties.rollCancerDance();

            EntityHitResult hit = RaytraceUtil.raycastEntities(properties.asEntity(), 3);

            if (hit != null) {
                PSCriteria.BREATHE_SMOKE_ON_ENTITY.trigger(properties.asEntity(), hit.getEntity());
                DrugProperties.of(hit.getEntity()).ifPresent(target -> {
                    target.addAll(influences.stream().map(i -> i.copyWithMaximum(i.getTargetInfluence() * 0.1F)).toList());
                    if (target.asEntity().getWorld().random.nextInt(10) == 0) {
                        if (target.rollCancerDance()) {
                            PSCriteria.CANCER.trigger(target.asEntity(), properties.asEntity());
                        }
                    }
                });
                if (hit.getEntity() instanceof LivingEntity l) {
                    if (l instanceof MobEntity mob) {
                        mob.playAmbientSound();
                    }
                    if (l instanceof MerchantEntity villager) {
                        villager.setHeadRollingTimeLeft(100);
                    } else {
                        hit.getEntity().damage(properties.asEntity().getDamageSources().playerAttack(properties.asEntity()), 0.1F);
                    }
                    if (l instanceof InteractionObserver observer) {
                        observer.onInteractionWith(EntityInteraction.VILLAGER_HURT, properties.asEntity());
                    }
                }
            }
        });
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (type.isAdvanced() && !influences.isEmpty()) {
            tooltip.accept(Text.translatable("psychedelicraft.item.contained_drug_effects").formatted(Formatting.GRAY));

            influences.forEach(influence -> {
                tooltip.accept(Text.translatable("psychedelicraft.item.contained_drug_effects.entry",
                        influence.getDrugType().id(),
                        Text.literal(String.format(Math.abs(influence.getTargetInfluence()) > MathHelper.EPSILON ? "%.2f" : "%.0f", influence.getTargetInfluence())),
                        Text.literal(String.format(Math.abs(influence.getBaseIncrease()) > MathHelper.EPSILON ? "%.3f" : "%.0f", influence.getBaseIncrease()))
                ).formatted(Formatting.DARK_PURPLE));
            });
        }
    }
}
