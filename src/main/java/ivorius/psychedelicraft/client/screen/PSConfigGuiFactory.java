/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.config.PSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cpw.mods.fml.client.config.DummyConfigElement.DummyCategoryElement;

/**
 * Created by lukas on 29.06.14.
 */
public class PSConfigGuiFactory implements IModGuiFactory
{
    @Override
    public void initialize(Minecraft minecraftInstance)
    {

    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return ConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element)
    {
        return null;
    }

    public static class ConfigGui extends GuiConfig
    {
        public ConfigGui(GuiScreen parentScreen)
        {
            super(parentScreen, getConfigElements(), Psychedelicraft.MODID, false, false, StatCollector.translateToLocalFormatted("psychedelicraft.configgui.title"));
        }

        private static List<IConfigElement> getConfigElements()
        {
            List<IConfigElement> list = new ArrayList<>();
            list.add(new DummyCategoryElement("psychedelicraft.configgui.general", "psychedelicraft.configgui.ctgy.general", GeneralEntry.class).setRequiresMcRestart(true));
            list.add(new DummyCategoryElement("psychedelicraft.configgui.balancing", "psychedelicraft.configgui.ctgy.balancing", BalancingEntry.class).setRequiresMcRestart(true));
            list.add(new DummyCategoryElement("psychedelicraft.configgui.visual", "psychedelicraft.configgui.ctgy.visual", VisualEntry.class));
            list.add(new DummyCategoryElement("psychedelicraft.configgui.audio", "psychedelicraft.configgui.ctgy.audio", AudioEntry.class));
            return list;
        }

        public static class GeneralEntry extends GuiConfigEntries.CategoryEntry
        {
            public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
            {
                super(owningScreen, owningEntryList, prop);
            }

            @Override
            protected GuiScreen buildChildScreen()
            {
                return new GuiConfig(this.owningScreen,
                        (new ConfigElement(Psychedelicraft.config.getCategory(Configuration.CATEGORY_GENERAL))).getChildElements(),
                        this.owningScreen.modID, Configuration.CATEGORY_GENERAL, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        GuiConfig.getAbridgedConfigPath(Psychedelicraft.config.toString()));
            }
        }

        public static class BalancingEntry extends GuiConfigEntries.CategoryEntry
        {
            public BalancingEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
            {
                super(owningScreen, owningEntryList, prop);
            }

            @Override
            protected GuiScreen buildChildScreen()
            {
                return new GuiConfig(this.owningScreen,
                        (new ConfigElement(Psychedelicraft.config.getCategory(PSConfig.CATEGORY_BALANCING))).getChildElements(),
                        this.owningScreen.modID, PSConfig.CATEGORY_BALANCING, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        GuiConfig.getAbridgedConfigPath(Psychedelicraft.config.toString()));
            }
        }

        public static class VisualEntry extends GuiConfigEntries.CategoryEntry
        {
            public VisualEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
            {
                super(owningScreen, owningEntryList, prop);
            }

            @Override
            protected GuiScreen buildChildScreen()
            {
                return new GuiConfig(this.owningScreen,
                        (new ConfigElement(Psychedelicraft.config.getCategory(PSConfig.CATEGORY_VISUAL))).getChildElements(),
                        this.owningScreen.modID, PSConfig.CATEGORY_VISUAL, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        GuiConfig.getAbridgedConfigPath(Psychedelicraft.config.toString()));
            }
        }

        public static class AudioEntry extends GuiConfigEntries.CategoryEntry
        {
            public AudioEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
            {
                super(owningScreen, owningEntryList, prop);
            }

            @Override
            protected GuiScreen buildChildScreen()
            {
                return new GuiConfig(this.owningScreen,
                        (new ConfigElement(Psychedelicraft.config.getCategory(PSConfig.CATEGORY_AUDIO))).getChildElements(),
                        this.owningScreen.modID, PSConfig.CATEGORY_AUDIO, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        GuiConfig.getAbridgedConfigPath(Psychedelicraft.config.toString()));
            }
        }
    }
}
