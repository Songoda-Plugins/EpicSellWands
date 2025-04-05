package com.songoda.epicsellwands.wand;

import com.songoda.core.gui.GuiUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.songoda.core.utils.ItemUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicsellwands.EpicSellWands;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Wand implements Cloneable {

    private String key, name;
    private XMaterial type;
    private List<String> lore = new ArrayList<>(
            Arrays.asList("&7Right-click a chest with",
                    "&7this wand to sell it's",
                    "&7contents."));
    private boolean enchanted = false;
    private int uses = -1;
    private double wandMultiplier = 1.0;

    private String recipeLayout;
    private List<String> recipeIngredients = new ArrayList<>();

    public Wand(String key, String name, XMaterial type, Double wandMultiplier) {
        this.key = key;
        this.name = name;
        this.type = type;
        this.wandMultiplier = wandMultiplier;
    }

    public ItemStack asItemStack() {
        ItemStack item = GuiUtils.createButtonItem(type, TextUtils.formatText(name), lore.toArray(new String[0]));

        if (item == null || item.getType() == Material.AIR) {
            return new ItemStack(Material.BARRIER);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            for (String line : this.lore)
                lore.add(TextUtils.formatText(line));
            if (uses != -1)
                lore.add(EpicSellWands.getInstance().getLocale().getMessage("general.nametag.uses")
                        .processPlaceholder("uses", Integer.toString(uses)).toText());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        if (enchanted)
            ItemUtils.addGlow(item);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("wand", key);
        nbtItem.setInteger("uses", uses);
        nbtItem.setDouble("wandMultiplier", wandMultiplier);
        return nbtItem.getItem();
    }

    public double getWandMultiplier() {
        return wandMultiplier;
    }

    public Wand setWandMultiplier(double wandMultiplier) {
        this.wandMultiplier = wandMultiplier;
        return this;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(XMaterial type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public XMaterial getType() { return type; }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public Wand setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public Wand setEnchanted(boolean enchanted) {
        this.enchanted = enchanted;
        return this;
    }

    public int getUses() {
        return uses;
    }

    public int use() {
        if (uses != -1)
            uses--;
        return uses;
    }

    public Wand setUses(int uses) {
        this.uses = uses;
        return this;
    }

    public String getRecipeLayout() {
        return recipeLayout;
    }

    public Wand setRecipeLayout(String recipeLayout) {
        this.recipeLayout = recipeLayout;
        return this;
    }

    public List<String> getRecipeIngredients() {
        return recipeIngredients;
    }

    public Wand setRecipeIngredients(List<String> recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
        return this;
    }

    public Wand clone() {
        try {
            return (Wand) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
}
