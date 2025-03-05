package com.craftaro.epicsellwands.wand;

import com.craftaro.epicsellwands.listeners.BlockListeners;
import com.craftaro.epicsellwands.settings.Settings;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WandManager {
    private static Map<String, Wand> registeredWands = new LinkedHashMap<>();
    private static Map<XMaterial, Double> registeredPrices = new HashMap<>();

    public Wand addWand(Wand wand) {
        registeredWands.put(wand.getKey().toUpperCase(), wand);
        return wand;
    }

    public Wand getWand(String key) {
        return registeredWands.get(key.toUpperCase());
    }

    public Wand getWand(ItemStack wandItem) {
        NBTItem nbtItem = new NBTItem(wandItem);
        if (!nbtItem.hasKey("wand")) return null;

        Wand wand = registeredWands.get(nbtItem.getString("wand")).clone();
        wand.setUses(nbtItem.getInteger("uses"));
        return wand;
    }

    public void removeWand(Wand wand) {
        registeredWands.remove(wand.getKey());
    }

    public void reKey(String oldKey, String key) {
        Wand wand = getWand(oldKey);
        registeredWands.remove(oldKey);
        registeredWands.put(key, wand);
    }

    public void addPrice(XMaterial material, double price) {
        registeredPrices.put(material, price);
    }

    public Double getPriceFor(XMaterial material) {
        Double price = registeredPrices.get(material);

        if (price == null) {
            Bukkit.getLogger().warning("[EpicSellWands] Price for " + material.name() + " is not defined!");
            return 0.0;
        }
        return price;
    }

    public boolean isSellable(XMaterial material) {
        if ((Settings.PRICE_PLUGIN.getString().equalsIgnoreCase("default"))) {
            return registeredPrices.containsKey(material);
        }
        return true;
    }

    public Collection<Wand> getWands() {
        return Collections.unmodifiableCollection(registeredWands.values());
    }

    public void clearData() {
        registeredWands.clear();
        registeredPrices.clear();
    }
}
