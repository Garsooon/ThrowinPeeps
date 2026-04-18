package com.garsooon.throwinpeeps;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ThrowinPeepsPlugin extends JavaPlugin implements CommandExecutor {

    private static final Logger log = Logger.getLogger("Minecraft");//lazy
    private ThrowListener throwListener;
    final Set<String> optedOut = new HashSet<>();
    private File optedOutFile;

    @Override
    public void onEnable() {
        optedOutFile = new File(getDataFolder(), "opted-out.txt");
        getDataFolder().mkdirs();
        loadOptedOut();

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
        if (optedOut.contains(name)) {
            optedOut.remove(name);
            player.sendMessage(ChatColor.GREEN + "You can now be picked up by other players.");
        } else {
            optedOut.add(name);
            player.sendMessage(ChatColor.RED + "You have opted out of being picked up.");
        }

        saveOptedOut();
        return true;
    }

    private void loadOptedOut() {
        if (!optedOutFile.exists()) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(optedOutFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    optedOut.add(line);
                }
            }
            reader.close();
        } catch (IOException e) {
            log.warning("[ThrowinPeeps] Failed to load opted-out list: " + e.getMessage());
        }
    }

    private void saveOptedOut() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(optedOutFile));
            for (String name : optedOut) {
                writer.write(name);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            log.warning("[ThrowinPeeps] Failed to save opted-out list: " + e.getMessage());
        }
    }
}
