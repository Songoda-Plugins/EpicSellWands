package com.songoda.epicsellwands.wand;

import com.songoda.epicsellwands.settings.Settings;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.core.third_party.de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
        if (wandItem == null || wandItem.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(wandItem);
        if (!nbtItem.hasKey("wand")) return null;

        String wandKey = nbtItem.getString("wand");
        Wand baseWand = registeredWands.get(wandKey);

        if (baseWand == null) {
            Bukkit.getLogger().warning("[EpicSellWands] Wand with key '" + wandKey + "' not found in registeredWands.");
            return null;
        }

        Wand wand = baseWand.clone();
        if (nbtItem.hasKey("uses")) {
            wand.setUses(nbtItem.getInteger("uses"));
        }
        if (nbtItem.hasKey("wandMultiplier")) {
            wand.setWandMultiplier(nbtItem.getDouble("wandMultiplier"));
        }
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
        //Force true for non Default Price
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
