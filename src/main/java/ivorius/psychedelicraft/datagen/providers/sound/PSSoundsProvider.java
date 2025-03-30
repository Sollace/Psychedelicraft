package ivorius.psychedelicraft.datagen.providers.sound;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.datagen.providers.sound.SoundTypeBuilder.Sound;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class PSSoundsProvider extends SoundsProvider {

    public PSSoundsProvider(DataOutput output, CompletableFuture<WrapperLookup> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    public String getName() {
        return "Psychedelicraft Sounds";
    }

    @Override
    protected void generate(BiConsumer<SoundEvent, SoundTypeBuilder> exporter) {
        exporter.accept(PSSounds.ENTITY_PLAYER_HEARTBEAT, SoundTypeBuilder.of(PSSounds.ENTITY_PLAYER_HEARTBEAT)
                .category(SoundCategory.PLAYERS)
                .sound(Sound.builder(Psychedelicraft.id("heart_beat")).build())
        );
        exporter.accept(PSSounds.ENTITY_PLAYER_BREATH, SoundTypeBuilder.of(PSSounds.ENTITY_PLAYER_BREATH)
                .category(SoundCategory.PLAYERS)
                .sound(Sound.builder(Psychedelicraft.id("breath")).build())
        );
        exporter.accept(PSSounds.ENTITY_PLAYER_PACIFIER_SQUEAK, SoundTypeBuilder.of(PSSounds.ENTITY_PLAYER_PACIFIER_SQUEAK)
                .category(SoundCategory.PLAYERS)
                .subtitle("subtitles.psychedelicraft.entity.player.pacify")
                .sound(Sound.builder(Psychedelicraft.id("pacifier/pacifier")).build(), 9)
        );
        exporter.accept(PSSounds.BLOCK_TRAY_HARDEN, SoundTypeBuilder.of(PSSounds.BLOCK_TRAY_HARDEN)
                .category(SoundCategory.BLOCKS)
                .sound(Sound.builder(Identifier.ofVanilla("mob/turtle/egg/egg_crack")).build(), 5)
        );
        exporter.accept(PSSounds.BLOCK_VALVE_OPEN, SoundTypeBuilder.of(PSSounds.BLOCK_VALVE_OPEN)
                .category(SoundCategory.BLOCKS)
                .sound(Sound.builder(Identifier.ofVanilla("mob/parrot/idle")).volume(0.7F).build(), 1)
        );
        exporter.accept(PSSounds.BLOCK_VALVE_CLOSE, SoundTypeBuilder.of(PSSounds.BLOCK_VALVE_CLOSE)
                .category(SoundCategory.BLOCKS)
                .sound(Sound.builder(Identifier.ofVanilla("mob/parrot/idle")).volume(0.7F).build(), 1)
        );

        List<Function<Sound.Builder, Sound.Builder>> variationFuncs = List.of(
                Function.identity(),
                b -> b.volume(0.9F),
                b -> b.pitch(0.9F),
                b -> b.volume(0.9F).pitch(0.9F),
                b -> b.pitch(1.1F),
                b -> b.volume(0.9F).pitch(1.1F)
        );
        var builder = SoundTypeBuilder.of(PSSounds.BLOCK_BUNSEN_BURNER_WORK)
                .category(SoundCategory.BLOCKS);
        Stream.of(1, 2, 3).forEach(index -> {
            variationFuncs.forEach(func -> {
                builder.sound(func.apply(Sound.builder(Identifier.ofVanilla("block/candle/extinguish" + index)).attenuationDistance(8)).build());
            });
        });
        exporter.accept(PSSounds.BLOCK_BUNSEN_BURNER_WORK, builder);
        exporter.accept(PSSounds.BLOCK_BUNSEN_BURNER_OVERHEAT, SoundTypeBuilder.of(PSSounds.BLOCK_BUNSEN_BURNER_OVERHEAT)
                .category(SoundCategory.BLOCKS)
                .sound(Sound.builder(Identifier.ofVanilla("fire/fire")).build())
        );
        exporter.accept(PSSounds.BLOCK_BUNSEN_BURNER_FILL, SoundTypeBuilder.of(PSSounds.BLOCK_BUNSEN_BURNER_FILL)
                .category(SoundCategory.BLOCKS)
                .sound(Sound.builder(Identifier.ofVanilla("item/armor/equip_leather")).build(), 6)
        );
        exporter.accept(PSSounds.ITEM_SYRINGE_INJECT, SoundTypeBuilder.of(PSSounds.ITEM_SYRINGE_INJECT)
                .category(SoundCategory.PLAYERS)
                .subtitle("subtitle.psychedelicraft.item_syringe_inject")
                .sound(Sound.builder(Psychedelicraft.id("inject/inject")).build(), 2)
        );
        exporter.accept(PSSounds.BLOCK_RIFT_JAR_TOGGLE, SoundTypeBuilder.of(PSSounds.BLOCK_RIFT_JAR_TOGGLE)
                .category(SoundCategory.BLOCKS)
                .subtitle("subtitle.psychedelicraft.rift_jar_toggles")
                .sound(Sound.builder(Identifier.ofVanilla("block/end_portal/eyeplace")).build(), 3)
        );
        exporter.accept(PSSounds.BLOCK_RIFT_JAR_OPEN, SoundTypeBuilder.of(PSSounds.BLOCK_RIFT_JAR_OPEN)
                .category(SoundCategory.BLOCKS)
                .subtitle("subtitle.psychedelicraft.rift_jar_open")
                .sound(Sound.builder(Psychedelicraft.id("rift_jar/jar_open")).build())
        );
        exporter.accept(PSSounds.BLOCK_RIFT_JAR_CLOSE, SoundTypeBuilder.of(PSSounds.BLOCK_RIFT_JAR_CLOSE)
                .category(SoundCategory.BLOCKS)
                .subtitle("subtitle.psychedelicraft.rift_jar_close")
                .sound(Sound.builder(Psychedelicraft.id("rift_jar/jar_open")).build())
        );

        DrugType.REGISTRY.forEach(type -> {
            exporter.accept(type.soundEvent(), SoundTypeBuilder.of()
                    .category(SoundCategory.AMBIENT)
                    .sound(Sound.builder(Psychedelicraft.id("drugs/generic")).stream(true).build())
            );
        });
    }
}
