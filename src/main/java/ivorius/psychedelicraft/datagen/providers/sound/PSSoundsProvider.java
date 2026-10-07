package ivorius.psychedelicraft.datagen.providers.sound;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder.EntryBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;

public class PSSoundsProvider extends FabricSoundsProvider {

    public PSSoundsProvider(DataOutput output, CompletableFuture<WrapperLookup> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    public String getName() {
        return "Psychedelicraft Sounds";
    }

    @Override
    protected void configure(WrapperLookup registryLookup, SoundExporter exporter) {
        exporter.add(PSSounds.ENTITY_PLAYER_HEARTBEAT, SoundTypeBuilder.of(PSSounds.ENTITY_PLAYER_HEARTBEAT)
                .sound(EntryBuilder.ofFile(Psychedelicraft.id("heart_beat")))
        );
        exporter.add(PSSounds.ENTITY_PLAYER_BREATH, SoundTypeBuilder.of(PSSounds.ENTITY_PLAYER_BREATH)
                .sound(EntryBuilder.ofFile(Psychedelicraft.id("breath")))
        );
        exporter.add(PSSounds.ENTITY_PLAYER_SQUEAK, SoundTypeBuilder.of(PSSounds.ENTITY_PLAYER_SQUEAK)
                .sound(EntryBuilder.ofFile(Psychedelicraft.id("squeak/squeak")), 3));
        exporter.add(PSSounds.ENTITY_PLAYER_PACIFIER_SQUEAK, SoundTypeBuilder.of(PSSounds.ENTITY_PLAYER_PACIFIER_SQUEAK)
                .sound(EntryBuilder.ofFile(Psychedelicraft.id("pacifier/pacifier")), 9)
        );
        exporter.add(PSSounds.ITEM_BROKEN_GLASS_EAT, SoundTypeBuilder.of(PSSounds.ITEM_BROKEN_GLASS_EAT)
                .sound(EntryBuilder.ofFile(Psychedelicraft.id("broken_glass/glass")), 4)
        );
        exporter.add(PSSounds.BLOCK_TRAY_HARDEN, SoundTypeBuilder.of(PSSounds.BLOCK_TRAY_HARDEN)
                .sound(EntryBuilder.ofFile(Identifier.ofVanilla("mob/turtle/egg/egg_crack")), 5)
        );
        exporter.add(PSSounds.BLOCK_VALVE_OPEN, SoundTypeBuilder.of(PSSounds.BLOCK_VALVE_OPEN)
                .sound(EntryBuilder.ofFile(Identifier.ofVanilla("mob/parrot/idle")).volume(0.7F), 1)
        );
        exporter.add(PSSounds.BLOCK_VALVE_CLOSE, SoundTypeBuilder.of(PSSounds.BLOCK_VALVE_CLOSE)
                .sound(EntryBuilder.ofFile(Identifier.ofVanilla("mob/parrot/idle")).volume(0.7F), 1)
        );

        List<Function<EntryBuilder, EntryBuilder>> variationFuncs = List.of(
                Function.identity(),
                b -> b.volume(0.9F),
                b -> b.pitch(0.9F),
                b -> b.volume(0.9F).pitch(0.9F),
                b -> b.pitch(1.1F),
                b -> b.volume(0.9F).pitch(1.1F)
        );
        var builder = SoundTypeBuilder.of(PSSounds.BLOCK_BUNSEN_BURNER_WORK);
        Stream.of(1, 2, 3).forEach(index -> {
            variationFuncs.forEach(func -> {
                builder.sound(func.apply(EntryBuilder.ofFile(Identifier.ofVanilla("block/candle/extinguish" + index)).attenuationDistance(8)));
            });
        });
        exporter.add(PSSounds.BLOCK_BUNSEN_BURNER_WORK, builder);
        exporter.add(PSSounds.BLOCK_BUNSEN_BURNER_OVERHEAT, SoundTypeBuilder.of(PSSounds.BLOCK_BUNSEN_BURNER_OVERHEAT)
                .sound(EntryBuilder.ofFile(Identifier.ofVanilla("fire/fire")))
        );
        exporter.add(PSSounds.BLOCK_BUNSEN_BURNER_FILL, SoundTypeBuilder.of(PSSounds.BLOCK_BUNSEN_BURNER_FILL)
                .sound(EntryBuilder.ofFile(Identifier.ofVanilla("item/armor/equip_leather")), 6)
        );
        exporter.add(PSSounds.ITEM_SYRINGE_INJECT, SoundTypeBuilder.of(PSSounds.ITEM_SYRINGE_INJECT)
                .sound(EntryBuilder.ofFile(Psychedelicraft.id("inject/inject")), 2)
        );
        exporter.add(PSSounds.BLOCK_RIFT_JAR_TOGGLE, SoundTypeBuilder.of(PSSounds.BLOCK_RIFT_JAR_TOGGLE)
                .sound(EntryBuilder.ofFile(Identifier.ofVanilla("block/end_portal/eyeplace")), 3)
        );
        exporter.add(PSSounds.BLOCK_RIFT_JAR_OPEN, SoundTypeBuilder.of(PSSounds.BLOCK_RIFT_JAR_OPEN)
                .sound(EntryBuilder.ofFile(Psychedelicraft.id("rift_jar/jar_open")))
        );
        exporter.add(PSSounds.BLOCK_RIFT_JAR_CLOSE, SoundTypeBuilder.of(PSSounds.BLOCK_RIFT_JAR_CLOSE)
                .sound(EntryBuilder.ofFile(Psychedelicraft.id("rift_jar/jar_open")))
        );

        DrugType.REGISTRY.forEach(type -> {
            exporter.add(type.soundEvent(), SoundTypeBuilder.of()
                    .sound(EntryBuilder.ofFile(Psychedelicraft.id("drugs/generic")).stream(true))
            );
        });
    }
}
