package com.craftaro.epicsellwands.commands;

import com.songoda.core.commands.AbstractCommand;
import com.craftaro.epicsellwands.EpicSellWands;
import com.craftaro.epicsellwands.gui.GuiAdmin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandAdmin extends AbstractCommand {

    EpicSellWands plugin;

    public CommandAdmin(EpicSellWands plugin) {
        super(CommandType.PLAYER_ONLY, "admin");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        plugin.getGuiManager().showGUI((Player) sender, new GuiAdmin(plugin));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicsellwands.admin";
    }

    @Override
    public String getSyntax() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return "Access admin panel.";
    }
}
