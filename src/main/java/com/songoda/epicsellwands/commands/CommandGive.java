package com.songoda.epicsellwands.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicsellwands.EpicSellWands;
import com.songoda.epicsellwands.wand.Wand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandGive extends AbstractCommand {

    EpicSellWands plugin;

    public CommandGive(EpicSellWands plugin) {
        super(CommandType.CONSOLE_OK, "give");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length > 3 || args.length < 2) return ReturnType.SYNTAX_ERROR;

        if (Bukkit.getPlayer(args[0]) == null && !args[0].trim().toLowerCase().equals("all")) {
            sender.sendMessage(args[0] + " is not a player...");
            return ReturnType.SYNTAX_ERROR;
        }

        Wand wand = plugin.getWandManager().getWands().stream()
                .filter(w -> w.getKey().equals(args[1].toUpperCase())).findFirst().orElse(null);

        if (wand == null) {
            plugin.getLocale().newMessage("&7The wand &6" + args[1] + " &7does not exist. Try one of these:").sendPrefixedMessage(sender);
            sender.sendMessage(TextUtils.formatText("&6" +
                    plugin.getWandManager().getWands().stream().map(w -> w.getKey().toLowerCase()).collect(Collectors.joining(", "))));
        } else {
            if (args.length == 3) {
                try {
                    Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    plugin.getLocale().newMessage("&7The number &6" + args[2] + " &7is in fact not a number at all...").sendPrefixedMessage(sender);
                    return ReturnType.FAILURE;
                }
            }
            int amount = args.length == 3 ? Integer.parseInt(args[2]) : 1;
            ItemStack itemStack = wand.asItemStack();
            if (!args[0].trim().toLowerCase().equals("all")) {
                Player player = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                for (int i = 0; i < amount; i++)
                    player.getInventory().addItem(itemStack);
                plugin.getLocale().getMessage("command.give.success")
                        .processPlaceholder("type", TextUtils.formatText(wand.getName()))
                        .sendPrefixedMessage(player);
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (int i = 0; i < amount; i++)
                        player.getInventory().addItem(itemStack);
                    plugin.getLocale().getMessage("command.give.success")
                            .processPlaceholder("type", TextUtils.formatText(wand.getName()))
                            .sendPrefixedMessage(player);
                }
            }
        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            players.add("all");
            players.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            return players;
        } else if (args.length == 2) {
            return plugin.getWandManager().getWands().stream().map(w -> w.getKey().toLowerCase()).collect(Collectors.toList());
        } else if (args.length == 3) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicsellwands.admin";
    }

    @Override
    public String getSyntax() {
        return "give <player/all> <wand> [amount]";
    }

    @Override
    public String getDescription() {
        return "Gives an operator the ability to give a wand of his or her choice.";
    }
}
