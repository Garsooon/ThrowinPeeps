package com.garsooon.throwinpeeps;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ThrowinPeepsPlugin extends JavaPlugin implements CommandExecutor {

    private static final Logger log = Logger.getLogger("Minecraft");//lazy
    private ThrowListener throwListener;
    final Set<String> throwEnabled = new HashSet<>();

    @Override
    public void onEnable() {
        throwListener = new ThrowListener(this);
        getServer().getPluginManager().registerEvents(throwListener, this);
        getCommand("throwinpeeps").setExecutor(this);

        log.info("[ThrowinPeeps] enabled!");
    }

    @Override
    public void onDisable() {
        if (throwListener != null) {
            throwListener.cleanup();
        }
        log.info("[ThrowinPeeps] disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("throwinpeeps.toggle")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("toggle")) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /throwinpeeps toggle");
            return true;
        }

        String name = player.getName();
        if (throwEnabled.contains(name)) {
            throwEnabled.remove(name);
            player.sendMessage(ChatColor.RED + "Throw ability disabled.");
        } else {
            throwEnabled.add(name);
            player.sendMessage(ChatColor.GREEN + "Throw ability enabled.");
        }

        return true;
    }

    void removeThrowEnabled(String name) {
        throwEnabled.remove(name);
    }
}
