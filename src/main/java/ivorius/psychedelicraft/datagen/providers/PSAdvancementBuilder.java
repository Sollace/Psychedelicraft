package ivorius.psychedelicraft.datagen.providers;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class PSAdvancementBuilder {
    private static final Identifier BACKGROUND = Identifier.ofVanilla("textures/gui/advancements/backgrounds/stone.png");

    public static PSAdvancementBuilder create(Identifier id, ItemConvertible icon) {
        return new PSAdvancementBuilder(id, icon, null);
    }

    private final Advancement.Builder builder = Advancement.Builder.create();

    private final Identifier id;


    @Nullable
    private Identifier background = BACKGROUND;

    private boolean toast = true;
    private boolean hidden;
    private boolean announce = true;
    private final ItemConvertible icon;
    private Consumer<ItemStack> iconCustomisation = stack -> {};
    private AdvancementFrame frame = AdvancementFrame.TASK;
    @Nullable
    private String group;

    private PSAdvancementBuilder(Identifier id, ItemConvertible icon, @Nullable Parent parent) {
        this.id = id;
        this.icon = icon;
        if (parent != null) {
            builder.parent(parent.entry());
        }
    }

    public PSAdvancementBuilder icon(Consumer<ItemStack> iconCustomisation) {
        this.iconCustomisation = iconCustomisation;
        return this;
    }

    public PSAdvancementBuilder frame(AdvancementFrame frame) {
        this.frame = frame;
        return this;
    }

    public PSAdvancementBuilder background(Identifier background) {
        this.background = background;
        return this;
    }

    public PSAdvancementBuilder showToast() {
        this.toast = true;
        return this;
    }

    public PSAdvancementBuilder hidden() {
        this.hidden = true;
        return this;
    }

    public PSAdvancementBuilder visible() {
        this.hidden = false;
        return this;
    }

    public PSAdvancementBuilder announce() {
        this.announce = true;
        return this;
    }

    public PSAdvancementBuilder doNotAnnounce() {
        this.announce = false;
        return this;
    }

    public PSAdvancementBuilder group(String group) {
        this.group = group;
        return this;
    }

    public PSAdvancementBuilder rewards(AdvancementRewards.Builder builder) {
        this.builder.rewards(builder.build());
        return this;
    }

    public PSAdvancementBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        builder.criterion(name, criterion);
        return this;
    }

    public PSAdvancementBuilder criteriaMerger(AdvancementRequirements.CriterionMerger merger) {
        builder.criteriaMerger(merger);
        return this;
    }

    public Parent build(Consumer<AdvancementEntry> exporter) {
        Identifier id = group == null ? this.id : this.id.withPrefixedPath(group + "/");
        String key = Util.createTranslationKey("advancements", this.id);
        ItemStack icon = this.icon.asItem().getDefaultStack();
        iconCustomisation.accept(icon);
        AdvancementEntry advancement = builder.display(
                icon,
                Text.translatable(key + ".title"),
                Text.translatable(key + ".description"), background, frame, toast, announce, hidden)
                .build(id);
        exporter.accept(advancement);
        return new Parent(advancement, group);
    }

    public record Parent(AdvancementEntry entry, String group) {
        public Parent children(Consumer<Parent> children) {
            children.accept(this);
            return this;
        }

        public PSAdvancementBuilder child(Identifier id, ItemConvertible icon) {
            return new PSAdvancementBuilder(id, icon, this).group(group);
        }

    }
}
