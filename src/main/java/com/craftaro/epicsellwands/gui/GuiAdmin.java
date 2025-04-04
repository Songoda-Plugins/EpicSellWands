package com.craftaro.epicsellwands.gui;

import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicsellwands.wand.Wand;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.craftaro.epicsellwands.EpicSellWands;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiAdmin extends Gui {

    private final EpicSellWands plugin;

    public GuiAdmin(EpicSellWands plugin) {
        this.plugin = plugin;
        setTitle("EpicSellWands Admin");
        paint();
    }

    void paint() {
        if (inventory != null)
            inventory.clear();

        List<Wand> wands = new ArrayList<>(plugin.getWandManager().getWands());
        //Dynamically set the number of rows based on the number of wands
        int rows = Math.min(6, Math.max(3, (wands.size() / 9) + 2));
        setRows(rows);

        setButton(8, GuiUtils.createButtonItem(XMaterial.REDSTONE, "Create Wand"),
                (event) -> {
                    Wand wand = new Wand("WAND_" + (wands.size() + 1),
                            "Wand " + (wands.size() + 1),
                            XMaterial.WOODEN_HOE,
                            1.0);
                    plugin.getWandManager().addWand(wand);
                    guiManager.showGUI(event.player, new GuiEditWand(plugin, this, wand));
                });

        for (int i = 0; i < wands.size(); i++) {
            Wand wand = wands.get(i);
            ItemStack item = wand.asItemStack();

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.addAll(Arrays.asList("", TextUtils.formatText("&6Left Click &7to edit"), TextUtils.formatText("&6Right Click &7to take")));
            meta.setLore(lore);
            item.setItemMeta(meta);

            setButton(i + 9, item, (event) -> {
                if (event.clickType == ClickType.LEFT) {
                    guiManager.showGUI(event.player, new GuiEditWand(plugin, this, wand));
                } else if (event.clickType == ClickType.RIGHT) {
                    if (event.player.getInventory().firstEmpty() == -1) {
                        event.player.sendMessage("§cYour inventory is full!");
                    } else {
                        event.player.getInventory().addItem(wand.asItemStack());
                    }
                }
            });
        }
    }
}
