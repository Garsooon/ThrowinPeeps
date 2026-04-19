package com.garsooon.throwinpeeps;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class ThrowListener implements Listener {


    private final ThrowinPeepsPlugin plugin;
    private final Map<String, String> carrying = new HashMap<>();
    private final Map<String, Integer> thrown = new HashMap<>();

    private static final long IMMUNITY_TICKS = 200L;
    private static final double THROW_FORWARD = 1.8;
    private static final double THROW_UP      = 0.65;

    public ThrowListener(ThrowinPeepsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player carrier = event.getPlayer();
        Entity clicked = event.getRightClicked();

        if (!(clicked instanceof Player)) return;
        Player target = (Player) clicked;

        if (carrier.equals(target)) return;

        if (!plugin.throwEnabled.contains(carrier.getName())) return;

        if (carrying.containsKey(carrier.getName())) {
            carrier.sendMessage(ChatColor.RED + "You're already carrying someone! Sneak to throw first.");
            return;
        }

        if (carrying.containsValue(target.getName())) {
            carrier.sendMessage(ChatColor.RED + target.getName() + " is already being carried.");
            return;
        }

        carrier.setPassenger(target);
        carrying.put(carrier.getName(), target.getName());

        carrier.sendMessage(ChatColor.GREEN + "Picked up " + ChatColor.YELLOW + target.getName()
                + ChatColor.GREEN + "! Sneak to throw.");
        target.sendMessage(ChatColor.YELLOW + "You were picked up by " + ChatColor.GREEN + carrier.getName()
                + ChatColor.YELLOW + "!");
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player carrier = event.getPlayer();
        String carrierName = carrier.getName();

        if (!carrying.containsKey(carrierName)) return;

        String carriedName = carrying.remove(carrierName);
        carrier.eject();

        Player carried = plugin.getServer().getPlayer(carriedName);
        if (carried == null) return;

        scheduleImmunityExpiry(carriedName);

        Vector throwVec = carrier.getLocation().getDirection()
                .setY(0)
                .normalize()
                .multiply(THROW_FORWARD)
                .setY(THROW_UP);
        carried.setVelocity(throwVec);

        carrier.sendMessage(ChatColor.RED + "You threw " + ChatColor.YELLOW + carriedName + ChatColor.RED + "!");
        carried.sendMessage(ChatColor.RED + "You were thrown by " + ChatColor.YELLOW + carrierName + ChatColor.RED + "! Fall damage disabled.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;

        String name = ((Player) event.getEntity()).getName();
        if (!thrown.containsKey(name)) return;

        event.setCancelled(true);
        clearImmunity(name);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String name = event.getPlayer().getName();

        if (carrying.containsKey(name)) {
            event.getPlayer().eject();
            carrying.remove(name);
        }
        carrying.values().remove(name);

        clearImmunity(name);
        plugin.removeThrowEnabled(name);
    }

    private void scheduleImmunityExpiry(final String playerName) {
        clearImmunity(playerName);

        int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> thrown.remove(playerName), IMMUNITY_TICKS);

        thrown.put(playerName, taskId);
    }

    private void clearImmunity(String playerName) {
        Integer taskId = thrown.remove(playerName);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    public void cleanup() {
        carrying.clear();
        for (int taskId : thrown.values()) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
        thrown.clear();
    }
}
