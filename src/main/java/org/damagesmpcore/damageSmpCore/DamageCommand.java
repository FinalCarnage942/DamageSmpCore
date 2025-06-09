package org.damagesmpcore.damageSmpCore;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class DamageCommand implements CommandExecutor {

    private final DamageManager damageManager;
    private final DamageSmpCore plugin;

    public DamageCommand(DamageManager damageManager, DamageSmpCore plugin) {
        this.damageManager = damageManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Permission check for reload
            if (!sender.hasPermission("damagesmpcore.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to reload the config.");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "DamageSmpCore config reloaded!");
            return true;
        }

        // Show damage percent for players only
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        double percent = damageManager.getPlayerStrength(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Your current damage boost is: " +
                ChatColor.GREEN + String.format("%.1f", percent) + "%");
        return true;
    }
}
