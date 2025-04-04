package com.craftaro.epicsellwands.listeners;

import com.Zrips.CMI.CMI;
import com.earth2me.essentials.IEssentials;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.third_party.com.cryptomorin.xseries.XSound;
import com.craftaro.epicsellwands.player.PlayerManager;
import com.craftaro.epicsellwands.wand.Wand;
import com.craftaro.epicsellwands.wand.WandManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.craftaro.epicsellwands.EpicSellWands;
import com.craftaro.epicsellwands.settings.Settings;
import com.songoda.third_party.org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;

public class BlockListeners implements Listener {

    private final EpicSellWands plugin;
    private final PlayerManager playerManager;
    private final WandManager wandManager;

    public BlockListeners(EpicSellWands plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        this.wandManager = plugin.getWandManager();
    }

    public enum PriceSource {
        DEFAULT,
        SHOPGUIPLUS,
        ECONOMYSHOPGUI,
        ESSENTIALS,
        CMI
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasLore())
            return;

        ItemStack wandItem = player.getItemInHand();

        if (!new NBTItem(wandItem).hasKey("wand")) return;
        Wand wand = plugin.getWandManager().getWand(wandItem);

        event.setCancelled(true);

        Block block = event.getClickedBlock();
        if ((block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST))
                && !Settings.ALLOW_ALL_CONTAINERS.getBoolean() || Settings.ALLOW_ALL_CONTAINERS.getBoolean()
                && block.getState() instanceof InventoryHolder) {
            InventoryHolder holder = (InventoryHolder) block.getState();
            Inventory inventory = holder.getInventory();

            if (!EconomyManager.isEnabled()) {
                player.sendMessage("§cEconomy plugin is missing");
                this.plugin.getLogger().warning("A player tried using a wand but economy is not available!");
                return;
            }

            if (!player.hasPermission("epicsellwands.use")) {
                plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(player);
                return;
            }

            if (playerManager.hasActiveCooldown(player)) {
                plugin.getLocale().getMessage("event.use.cooldown")
                        .processPlaceholder("seconds",
                                (playerManager.getActiveCooldown(player) - System.currentTimeMillis()) / 1000).sendPrefixedMessage(player);
                return;
            }
            int slot = 0;
            double totalSale = 0.0;
            NumberFormat format = NumberFormat.getInstance();
            format.setGroupingUsed(true);

            // Items that we are selling.
            HashMap<XMaterial, SoldItem> items = new HashMap<>();

            // Loop through all the inventory items.
            for (ItemStack chestItem : inventory) {
                if (chestItem == null || chestItem.getType().equals(Material.AIR)) {
                    slot++;
                    continue;
                }

                // Get the compatible material for this item.
                XMaterial material = XMaterial.matchXMaterial(chestItem);

                // Is this item sellable?
                if (wandManager.isSellable(material)) {
                    // Get the item price
                    Double singleSale = getPriceForItem(material, player);

                    // Check if price is undefined
                    if (singleSale == null || singleSale == 0) {
                        slot++;
                        continue;
                    }

                    // Remove the item from the inventory.
                    inventory.setItem(slot, new ItemStack(Material.AIR));

                    double itemValue = singleSale * chestItem.getAmount() * Settings.PRICE_MULTIPLIER.getDouble() * wand.getWandMultiplier();

                    // Add the price of this item to the total sale.
                    totalSale += itemValue;

                    // Add the item to the map.
                    if (items.containsKey(material))
                        items.get(material).addAmount(chestItem.getAmount()).addTotal(itemValue);
                    else
                        items.put(material, new SoldItem(material, chestItem.getAmount(), itemValue));
                }
                slot++;
            }

            if (items.isEmpty()) {
                plugin.getLocale().getMessage("event.use.empty").sendPrefixedMessage(player);
                return;
            }
            if (EconomyManager.deposit(player, totalSale)) {
                plugin.getLocale().getMessage("event.use.sale")
                        .processPlaceholder("amount", format.format(totalSale)).sendPrefixedMessage(player);
                if (Settings.SALE_BREAKDOWN.getBoolean()) {
                    for (SoldItem soldItem : items.values()) {
                        if (soldItem == null || soldItem.material == null) continue;
                        plugin.getLocale().getMessage("event.use.breakdown")
                                .processPlaceholder("amount", soldItem.getAmount())
                                .processPlaceholder("item", WordUtils
                                        .capitalizeFully(soldItem.material.name().toLowerCase()
                                                .replace("_", " ")))
                                .processPlaceholder("price", format.format(soldItem.getTotal()))
                                .sendPrefixedMessage(player);
                    }
                }

                int remainingUses = wand.use();
                if (remainingUses == 0) {
                    player.setItemInHand(null);
                    XSound.ENTITY_ITEM_BREAK.play(player);
                    plugin.getLocale().getMessage("event.use.broken")
                            .sendPrefixedMessage(player);
                } else {
                    player.setItemInHand(wand.asItemStack());
                    if (wand.getUses() != -1)
                        plugin.getLocale().getMessage("event.use.left")
                                .processPlaceholder("uses", remainingUses)
                                .sendPrefixedMessage(player);
                }
            } else {
                this.plugin.getLogger().info("Transaction has failed for Inventory Sale (player: "
                        + player.getName() + " amount: " + totalSale + ")");
            }

            if (Settings.COOLDOWN.getInt() > 0)
                playerManager.addNewCooldown(player);
        }
    }

    /**
     * Fetch price for an item using multiple methods
     *
     * @param material The XMaterial of the item
     * @param player The player selling the item
     * @return The price of the item
     */
    private double getPriceForItem(XMaterial material, Player player) {
        PriceSource priceSource = PriceSource.valueOf(Settings.PRICE_PLUGIN.getString());
        switch (priceSource) {
            case SHOPGUIPLUS:
                try {
                    Class.forName("net.brcdev.shopgui.ShopGuiPlusApi");
                    Double shopGUIPlusPrice = net.brcdev.shopgui.ShopGuiPlusApi.getItemStackPriceSell(player, material.parseItem());
                    if (shopGUIPlusPrice != null && shopGUIPlusPrice > 0) {
                        return shopGUIPlusPrice;
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    plugin.getLogger().warning("[EpicSellWands] ShopGUIPlus not found.");
                } catch (Exception e) {
                    plugin.getLogger().warning("[EpicSellWands] ShopGUIPlus pricing error: " + e.getMessage());
                }
                break;
            case ECONOMYSHOPGUI:
                try {
                    Class.forName("me.gypopo.economyshopgui.api.EconomyShopGUIHook");
                    Double economyShopGUIPrice = EconomyShopGUIHook.getItemSellPrice(player, material.parseItem());
                    if (economyShopGUIPrice != null && economyShopGUIPrice > 0) {
                        return economyShopGUIPrice;
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    plugin.getLogger().warning("[EpicSellWands] EconomyShopGUI not found.");
                } catch (Exception e) {
                    plugin.getLogger().warning("[EpicSellWands] EconomyShopGUI pricing error: " + e.getMessage());
                }
                break;
            case ESSENTIALS:
                try {
                    Class.forName("com.earth2me.essentials.Essentials");
                    Plugin essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
                    if (essentialsPlugin instanceof IEssentials) {
                        IEssentials ess = (IEssentials) essentialsPlugin;
                        ItemStack itemStack = material.parseItem();

                        BigDecimal essentialsPrice = ess.getWorth().getPrice(ess, itemStack);
                        if (essentialsPrice != null && essentialsPrice.signum() >= 0) {
                            return essentialsPrice.doubleValue();
                        }
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    plugin.getLogger().warning("[EpicSellWands] Essentials not found.");
                } catch (Exception e) {
                    plugin.getLogger().warning("[EpicSellWands] Essentials pricing error: " + e.getMessage());
                }
                break;
            case CMI:
                try {
                    Class.forName("com.Zrips.CMI.CMI");
                    Plugin cmiPlugin = Bukkit.getPluginManager().getPlugin("CMI");
                    if (cmiPlugin != null) {
                        ItemStack itemStack = material.parseItem();
                        double cmiPrice = CMI.getInstance().getWorthManager().getWorth(itemStack).getSellPrice();
                        if (cmiPrice != 0.0 && cmiPrice > 0.0) {
                            return cmiPrice;
                        }
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    plugin.getLogger().warning("[EpicSellWands] CMI not found.");
                } catch (Exception e) {
                    plugin.getLogger().warning("[EpicSellWands] CMI pricing error: " + e.getMessage());
                }
                break;
            default:
                return wandManager.getPriceFor(material);
        }
        return wandManager.getPriceFor(material);
    }

    private static class SoldItem {

        private final XMaterial material;
        private int amount;
        private double total;

        public SoldItem(XMaterial material, int amount, double total) {
            this.material = material;
            this.amount = amount;
            this.total = total;
        }

        public XMaterial getMaterial() {
            return material;
        }

        public int getAmount() {
            return amount;
        }

        public double getTotal() {
            return total;
        }

        public SoldItem addTotal(double amount) {
            this.total += amount;
            return this;
        }

        public SoldItem addAmount(double amount) {
            this.amount += amount;
            return this;
        }
    }
}