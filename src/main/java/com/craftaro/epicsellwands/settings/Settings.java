package com.craftaro.epicsellwands.settings;

import com.songoda.core.configuration.Config;
import com.songoda.core.configuration.ConfigSetting;
import com.songoda.core.hooks.EconomyManager;
import com.craftaro.epicsellwands.EpicSellWands;

import java.util.stream.Collectors;

public class Settings {

    static final Config config = EpicSellWands.getInstance().getCoreConfig();

    public static final ConfigSetting COOLDOWN = new ConfigSetting(config, "General.Cooldown", 30,
            "The amount of time a user must wait before they can",
            "Use a sell wand again.");

    public static final ConfigSetting PRICE_PLUGIN = new ConfigSetting(config,"Price Source",
            "DEFAULT",
            "Choose price source for selling items. Options: DEFAULT, SHOPGUIPLUS, ECONOMYSHOPGUI, ESSENTIALS, CMI",
            "Default: prices.yml ");

    public static final ConfigSetting PRICE_MULTIPLIER = new ConfigSetting(config, "General.Price Multiplier", 1.0,
            "The amount all item prices will be multiplied by.");

    public static final ConfigSetting SALE_BREAKDOWN = new ConfigSetting(config, "General.Sale Breakdown", true,
            "Should sales be broken down?");

    public static final ConfigSetting ALLOW_ALL_CONTAINERS = new ConfigSetting(config, "General.Allow All Containers", false,
            "Allow sell wands to be used with all containers.",
            "Setting this option to false will only allow wands",
            "to sell the contents of chests.");

    public static final ConfigSetting ECONOMY_PLUGIN = new ConfigSetting(config, "Main.Economy", EconomyManager.getEconomy() == null ? "Vault" : EconomyManager.getEconomy().getName(),
            "Which economy plugin should be used?",
            "Supported plugins you have installed: \"" + EconomyManager.getManager().getRegisteredPlugins().stream().collect(Collectors.joining("\", \"")) + "\".");

    public static final ConfigSetting LANGUGE_MODE = new ConfigSetting(config, "System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    public static void setupConfig() {
        config.load();
        config.setAutoremove(true).setAutosave(true);

        config.saveChanges();
    }
}
