package ivorius.psychedelicraft.compat.modmenu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.IField.IChangeCallback;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.AbstractSlider;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Slider;
import com.minelittlepony.common.client.gui.element.Toggle;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PSClientConfig;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.config.JsonConfig;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entity.drug.DrugType;

import org.jetbrains.annotations.Nullable;

/**
 * In-Game options menu.
 *
 */
class SettingsScreen extends GameGui {
    private final JsonConfig.Loader<PSClientConfig> config;
    private final JsonConfig.Loader<PSConfig> serverConfig;
    private final PSClientConfig defaultConfigValues = new PSClientConfig();

    private final ScrollContainer content = new ScrollContainer();

    public SettingsScreen(@Nullable Screen parent) {
        super(Text.translatable("gui.psychedelicraft.options.title"), parent);

        config = PsychedelicraftClient.getConfigLoader();
        serverConfig = Psychedelicraft.getConfigLoader();

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

        PSClientConfig.Visual visual = config.getData().visual;

        content.addButton(new Label(LEFT - 5, row)).getStyle().setText("gui.psychedelicraft.options.visuals");

        content.addButton(new Label(LEFT, row += 25)).getStyle().setText("gui.psychedelicraft.options.shaders");
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.shaders_2d", visual.shader2DEnabled, z -> visual.shader2DEnabled = z);
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.shaders_3d", visual.shader3DEnabled, z -> visual.shader3DEnabled = z);
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.heat_distortion", visual.doHeatDistortion, z -> visual.doHeatDistortion = z);
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.water_distortion", visual.doWaterDistortion, z -> visual.doWaterDistortion = z);
        createToggle(LEFT, row += 20, "gui.psychedelicraft.option.motion_blur", visual.doMotionBlur, z -> visual.doMotionBlur = z);
        createFormattedSlider(LEFT, row += 25, 0, 8, "gui.psychedelicraft.option.pause_menu_blur", config.getData().visual.pauseMenuBlur, f -> visual.pauseMenuBlur = f);
        row += 10;
        content.addButton(new Label(LEFT - 5, row += 25)).getStyle().setText("gui.psychedelicraft.options.overlays");
        createToggle(LEFT, row += 25, "gui.psychedelicraft.option.water_overlay", visual.waterOverlayEnabled, z -> visual.waterOverlayEnabled = z);
        createToggle(LEFT, row += 25, "gui.psychedelicraft.option.hurt_overlay", visual.hurtOverlayEnabled, z -> visual.hurtOverlayEnabled = z);
        createFormattedSlider(LEFT, row += 25, "gui.psychedelicraft.option.sun_glare_intensity", config.getData().visual.sunFlareIntensity, f -> visual.sunFlareIntensity = f);

        content.addButton(new Label(LEFT - 5, row += 25)).getStyle().setText("gui.psychedelicraft.options.dof");
        content.addButton(new Label(LEFT, row += 25)).getStyle().setText("gui.psychedelicraft.option.focal_point.near");
        var nearDistance = createFormattedSlider(LEFT, row += 25, 0, 99, "gui.psychedelicraft.option.focal_point.distance", config.getData().visual.dofFocalPointNear, f -> visual.dofFocalPointNear = f);
        var nearBlur = createFormattedSlider(LEFT, row += 25, 0, 8, "gui.psychedelicraft.option.focal_point.blur", config.getData().visual.dofFocalBlurNear, f -> visual.dofFocalBlurNear = f);
        content.addButton(new Button(LEFT, row += 25, 150, 20))
            .onClick(sender -> {
                nearDistance.setValue(defaultConfigValues.visual.dofFocalPointNear);
                nearBlur.setValue(defaultConfigValues.visual.dofFocalBlurNear);
            })
            .getStyle().setText(Text.translatable("button.reset"));
        content.addButton(new Label(LEFT, row += 25)).getStyle().setText("gui.psychedelicraft.option.focal_point.far");
        var farDistance = createFormattedSlider(LEFT, row += 25, 100, 400, "gui.psychedelicraft.option.focal_point.distance", config.getData().visual.dofFocalPointFar, f -> visual.dofFocalPointFar = f);
        var farBlur = createFormattedSlider(LEFT, row += 25, 0, 8, "gui.psychedelicraft.option.focal_point.blur", config.getData().visual.dofFocalBlurFar, f -> visual.dofFocalBlurFar = f);
        content.addButton(new Button(LEFT, row += 25, 150, 20))
            .onClick(sender -> {
                farDistance.setValue(defaultConfigValues.visual.dofFocalPointFar);
                farBlur.setValue(defaultConfigValues.visual.dofFocalBlurFar);
            })
            .getStyle().setText(Text.translatable("button.reset"));


        if (RIGHT != LEFT) {
            clear = row;
            row = columnBeginning;
        } else {
            row += 25;
        }

        PSClientConfig.Audio audio = config.getData().audio;

        content.addButton(new Label(RIGHT - 5, row)).getStyle().setText("gui.psychedelicraft.options.sounds");
        content.addButton(new Label(RIGHT, row += 25)).getStyle().setText("gui.psychedelicraft.options.themes");
        for (DrugType type : DrugType.REGISTRY) {
            createToggle(RIGHT, row += 20, type.id().getPath(), audio.hasBackgroundMusic(type), value -> audio.setHasBackgroundMusic(type, value));
        }

        if (client.world == null || client.isIntegratedServerRunning()) {
            row = Math.max(row, clear);
            columnBeginning += row;
            content.addButton(new Label(LEFT - 5, row += 25)).getStyle().setText("gui.psychedelicraft.options.gameplay");

            var gameplay = serverConfig.getData().balancing;

            content.addButton(new Label(LEFT, row += 25)).getStyle().setText("gui.psychedelicraft.options.message_distortion");

            createToggle(LEFT, row += 25, "gui.psychedelicraft.option.gameplay.distort_incoming_messages", gameplay.messageDistortion.incoming, z -> gameplay.messageDistortion.incoming = z);
            createToggle(LEFT, row += 25, "gui.psychedelicraft.option.gameplay.distort_outgoing_messages", gameplay.messageDistortion.outgoing, z -> gameplay.messageDistortion.outgoing = z);

            if (RIGHT != LEFT) {
                clear = row;
                row = columnBeginning;
            } else {
                row += 25;
            }

            content.addButton(new Label(RIGHT, row += 25)).getStyle().setText("gui.psychedelicraft.options.features");
            createToggle(RIGHT, row += 25, "gui.psychedelicraft.option.gameplay.harmonium", gameplay.enableHarmonium, z -> {
                gameplay.enableHarmonium = z;
                return z;
            });
            createToggle(RIGHT, row += 25, "gui.psychedelicraft.option.gameplay.rift_jars", gameplay.enableRiftJars, z -> {
                gameplay.enableRiftJars = z;
                return z;
            });
            createToggle(RIGHT, row += 25, "gui.psychedelicraft.option.gameplay.molotovs", !gameplay.disableMolotovs, z -> {
                gameplay.disableMolotovs = !z;
                return z;
            });

            content.addButton(new Label(RIGHT, row += 25)).getStyle().setText("gui.psychedelicraft.options.balancing");
            createFormattedSlider(RIGHT, row += 25, 0, 1800, "gui.psychedelicraft.option.gameplay.rift_spawnrate", gameplay.randomTicksUntilRiftSpawn / PSConfig.MINUTE, z -> {
                gameplay.randomTicksUntilRiftSpawn = (int)(z * PSConfig.MINUTE);
                return (float)(gameplay.randomTicksUntilRiftSpawn / PSConfig.MINUTE);
            });
        }
    }

    private Toggle createToggle(int x, int y, String key, boolean value, IChangeCallback<Boolean> valueSetter) {
        Toggle toggle = content.addButton(new Toggle(x, y, value)).onChange(valueSetter);
        toggle.getStyle().setText(key);
        return toggle;
    }

    private AbstractSlider<Float> createFormattedSlider(int x, int y, String key, float value, IChangeCallback<Float> valueSetter) {
        return createFormattedSlider(x, y, 0, 1, key, value, valueSetter);
    }

    private AbstractSlider<Float> createFormattedSlider(int x, int y, float min, float max, String key, float value, IChangeCallback<Float> valueSetter) {
        Text label = Text.translatable(key);
        AbstractSlider<Float> slider = content.addButton(new Slider(x, y, min, max, value))
            .onChange(valueSetter)
            .setTextFormat(sender -> formatSliderValue(label, sender));

        slider.getStyle().setText(formatSliderValue(label, slider));
        slider.setWidth(150);
        return slider;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, partialTicks);
        content.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public void removed() {
        config.save();
        if (client.world == null || client.isIntegratedServerRunning()) {
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
