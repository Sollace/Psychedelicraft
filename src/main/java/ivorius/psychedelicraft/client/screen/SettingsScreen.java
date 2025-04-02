package ivorius.psychedelicraft.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.IField.IChangeCallback;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.AbstractSlider;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.EnumSlider;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Slider;
import com.minelittlepony.common.client.gui.element.Toggle;
import com.minelittlepony.common.util.settings.Setting;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PSClientConfig;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.config.PSConfig;
import org.jetbrains.annotations.Nullable;

/**
 * In-Game options menu.
 *
 */
public class SettingsScreen extends GameGui {
    private final PSClientConfig config;
    @Nullable
    private final PSConfig serverConfig;

    private final ScrollContainer content = new ScrollContainer();

    public SettingsScreen(@Nullable Screen parent) {
        super(Text.translatable("gui.psychedelicraft.options.title"), parent);

        client = MinecraftClient.getInstance();
        config = PsychedelicraftClient.getConfig();
        serverConfig = client.world == null || client.isIntegratedServerRunning() ? Psychedelicraft.getConfig() : null;

        content.margin.top = 30;
        content.margin.bottom = 30;
        content.getContentPadding().top = 10;
        content.getContentPadding().right = 10;
        content.getContentPadding().bottom = 40;
        content.getContentPadding().left = 10;
    }

    @Override
    protected void init() {
        content.init(this::rebuildContent);
    }

    private void rebuildContent() {

        int LEFT = content.width / 2 - 210;
        int RIGHT = content.width / 2 + 10;

        if (LEFT < 0) {
            LEFT = content.width / 2 - 100;
            RIGHT = LEFT;
        }

        int row = 0;
        int clear = 0;
        int columnBeginning = 25;

        getChildElements().add(content);

        addButton(new Label(width / 2, 5).setCentered()).getStyle().setText(getTitle());
        addButton(new Button(width / 2 - 100, height - 25))
            .onClick(sender -> finish())
            .getStyle()
                .setText("gui.done");

        content.addButton(new Label(LEFT - 5, row)).getStyle().setText("gui.psychedelicraft.options.visuals");

        content.addButton(new Label(LEFT, row += 25)).getStyle().setText("gui.psychedelicraft.options.shaders");
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.shaders_2d", config.shader2DEnabled);
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.shaders_3d", config.shader3DEnabled);
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.heat_distortion", config.doHeatDistortion);
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.water_distortion", config.doWaterDistortion);
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.motion_blur", config.doMotionBlur);

        content.addButton(new Label(LEFT - 5, row += 25)).getStyle().setText("gui.psychedelicraft.options.dof");
        content.addButton(new Label(LEFT, row += 25)).getStyle().setText("gui.psychedelicraft.option.focal_point.near");
        var nearDistance = createFormattedSlider(LEFT, row += 25, 0, 99, "gui.psychedelicraft.option.focal_point.distance", config.dofFocalPointNear);
        var nearBlur = createFormattedSlider(LEFT, row += 25, 0, 8, "gui.psychedelicraft.option.focal_point.blur", config.dofFocalBlurNear);
        content.addButton(new Button(LEFT, row += 25, 150, 20))
            .onClick(sender -> {
                nearDistance.setValue(config.dofFocalPointNear.getDefault());
                nearBlur.setValue(config.dofFocalBlurNear.getDefault());
            })
            .getStyle().setText(Text.translatable("button.reset"));
        content.addButton(new Label(LEFT, row += 25)).getStyle().setText("gui.psychedelicraft.option.focal_point.far");
        var farDistance = createFormattedSlider(LEFT, row += 25, 100, 400, "gui.psychedelicraft.option.focal_point.distance", config.dofFocalPointFar);
        var farBlur = createFormattedSlider(LEFT, row += 25, 0, 8, "gui.psychedelicraft.option.focal_point.blur", config.dofFocalBlurFar);
        content.addButton(new Button(LEFT, row += 25, 150, 20))
            .onClick(sender -> {
                farDistance.setValue(config.dofFocalPointFar.getDefault());
                farBlur.setValue(config.dofFocalBlurFar.getDefault());
            })
            .getStyle().setText(Text.translatable("button.reset"));

        clear = Math.max(row, clear);

        if (RIGHT != LEFT) {
            clear = row;
            row = columnBeginning;
        } else {
            row += 25;
        }

        content.addButton(new Label(RIGHT - 5, row)).getStyle().setText("gui.psychedelicraft.options.overlays");
        createToggle(RIGHT, row += 25, "gui.psychedelicraft.option.water_overlay", config.waterOverlayEnabled);
        createToggle(RIGHT, row += 25, "gui.psychedelicraft.option.hurt_overlay", config.hurtOverlayEnabled);
        createFormattedSlider(RIGHT, row += 25, "gui.psychedelicraft.option.sun_glare_intensity", config.sunFlareIntensity);
        row += 10;

        content.addButton(new Label(RIGHT - 5, row += 25)).getStyle().setText("gui.psychedelicraft.options.compatibility");
        createToggle(RIGHT, row += 20, "gui.psychedelicraft.option.shaders_iris", config.irisSupport);
        createToggle(RIGHT, row += 20, "gui.psychedelicraft.option.shaders_sodium", config.sodiumSupport);
        row += 10;

        content.addButton(new Label(RIGHT - 5, row += 25)).getStyle().setText("gui.psychedelicraft.options.sounds");
        createToggle(RIGHT, row += 20, "gui.psychedelicraft.option.themes", config.drugsBackgroundMusic);
        createFormattedSlider(RIGHT, row += 25, 0.01F, 1F, "gui.psychedelicraft.option.themes_threshold", config.drugsBackgroundMusicThreshold);
        content.addButton(new Label(RIGHT - 5, row += 25)).getStyle().setText(
                Text.translatable("gui.psychedelicraft.option.themes.explain")
                    .formatted(Formatting.BLUE, Formatting.BOLD, Formatting.UNDERLINE)
                    .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Sollace/Psychedelicraft/wiki/Drug-Background-Music")))
        );

        if (serverConfig != null) {
            row = Math.max(row, clear);
            columnBeginning += row;
            content.addButton(new Label(LEFT - 5, row += 25)).getStyle().setText("gui.psychedelicraft.options.gameplay");

            content.addButton(new Label(LEFT, row += 25)).getStyle().setText("gui.psychedelicraft.options.message_distortion");
            content.addButton(new EnumSlider<>(LEFT, row += 25, serverConfig.messageDistortion.get())).setWidth(150);

            if (RIGHT != LEFT) {
                clear = row;
                row = columnBeginning;
            } else {
                row += 25;
            }

            content.addButton(new Label(RIGHT, row)).getStyle().setText("gui.psychedelicraft.options.features");
            createToggle(RIGHT, row += 25, "gui.psychedelicraft.option.gameplay.harmonium", serverConfig.enableHarmonium);
            createToggle(RIGHT, row += 25, "gui.psychedelicraft.option.gameplay.rift_jars", serverConfig.enableRiftJars);
            createToggle(RIGHT, row += 25, "gui.psychedelicraft.option.gameplay.molotovs", !serverConfig.disableMolotovs.get(), z -> {
                return !serverConfig.disableMolotovs.set(!z);
            });

            if (RIGHT != LEFT) {
            } else {
                row += 25;
            }

            content.addButton(new Label(LEFT, row)).getStyle().setText("gui.psychedelicraft.options.balancing");
            createFormattedSlider(LEFT, row += 25, 0, 1800, "gui.psychedelicraft.option.gameplay.rift_spawnrate", serverConfig.randomTicksUntilRiftSpawn.get() / PSConfig.MINUTE, z -> {
                return (serverConfig.randomTicksUntilRiftSpawn.set((int)(z * PSConfig.MINUTE)) / (float)PSConfig.MINUTE);
            });
        }
    }

    private void createToggle(int x, int y, String key, Setting<Boolean> valueSetter) {
        content.addButton(new Toggle(x, y, valueSetter.get())).onChange(valueSetter).styled(s -> s.setText(key));
    }

    private void createToggle(int x, int y, String key, boolean value, IChangeCallback<Boolean> valueSetter) {
        content.addButton(new Toggle(x, y, value)).onChange(valueSetter).styled(s -> s.setText(key));
    }

    private AbstractSlider<Float> createFormattedSlider(int x, int y, String key, Setting<Float> valueSetter) {
        return createFormattedSlider(x, y, 0, 1, key, valueSetter);
    }

    private AbstractSlider<Float> createFormattedSlider(int x, int y, float min, float max, String key, Setting<Float> valueSetter) {
        return createFormattedSlider(x, y, min, max, key, valueSetter.get(), valueSetter);
    }

    private AbstractSlider<Float> createFormattedSlider(int x, int y, float min, float max, String key, float value, IChangeCallback<Float> valueSetter) {
        Text label = Text.translatable(key);
        AbstractSlider<Float> slider = content.addButton(new Slider(x, y, min, max, value))
            .onChange(valueSetter)
            .setTextFormat(sender -> formatSliderValue(label, sender));
        slider.setWidth(150);
        return slider;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        renderBackground(context, mouseX, mouseY, tickDelta);
        super.render(context, mouseX, mouseY, tickDelta);
        content.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public void removed() {
        config.save();
        if (serverConfig != null) {
            serverConfig.save();
        }
    }

    private Text formatSliderValue(Text label, AbstractSlider<Float> slider) {
        float value = slider.getValue();

        if (value < 0.001F) {
            return Text.translatable("gui.psychedelicraft.slider.value.off", label);
        }

        value *= 100F;
        value = Math.round(value);
        value /= 100F;

        return Text.translatable("gui.psychedelicraft.slider.value", label, value);
    }
}
