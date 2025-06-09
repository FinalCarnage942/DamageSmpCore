package org.damagesmpcore.damageSmpCore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DamageCommand implements CommandExecutor {

    private final DamageSmpCore plugin;

    public DamageCommand(DamageSmpCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // ─── reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("damagesmpcore.command.damage")) {
                sender.sendMessage(Component.text("You don’t have permission to do that.", NamedTextColor.RED));
                return true;
            }
            plugin.reloadPluginConfig();
            sender.sendMessage(Component.text("DamageSmpCore config reloaded.", NamedTextColor.GREEN));
            return true;
        }

        // ─── show own damage
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Component.text("Console must specify a player: /damage <player>", NamedTextColor.RED));
                return true;
            }
            UUID id = p.getUniqueId();
            double pct = plugin.getConfig().getDouble("players." + id.toString(), 0.0);

            p.sendMessage(Component.text("Your current damage boost: ", NamedTextColor.AQUA)
                    .append(Component.text(String.format("%.2f%%", pct), NamedTextColor.YELLOW)));
            return true;
        }

        // ─── admin commands
        if (!sender.hasPermission("damagesmpcore.command.damage")) {
            sender.sendMessage(Component.text("You don’t have permission to do that.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /damage <player> <set|reset> [value]", NamedTextColor.RED));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found or not online.", NamedTextColor.RED));
            return true;
        }
        UUID tid = target.getUniqueId();

        switch (args[1].toLowerCase()) {
            case "reset" -> {
                plugin.getConfig().set("players." + tid.toString(), 0.0);
                plugin.saveConfig();
                sender.sendMessage(Component.text("Reset " + targetName + "'s damage to 0%.", NamedTextColor.GREEN));
                target.sendMessage(Component.text("Your damage boost has been reset.", NamedTextColor.YELLOW));
            }
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /damage <player> set <value>", NamedTextColor.RED));
                    return true;
                }
                double val;
                try {
                    val = Double.parseDouble(args[2]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(Component.text("Invalid number: " + args[2], NamedTextColor.RED));
                    return true;
                }
                double max = plugin.getConfig().getDouble("damage.max_percent", 100.0);
                val = Math.max(0.0, Math.min(max, val));

                plugin.getConfig().set("players." + tid.toString(), val);
                plugin.saveConfig();

                sender.sendMessage(Component.text(String.format("Set %s's damage to %.2f%%", targetName, val), NamedTextColor.GREEN));
                target.sendMessage(Component.text(String.format("Your damage boost is now %.2f%%", val), NamedTextColor.YELLOW));
            }
            default -> {
                sender.sendMessage(Component.text("Unknown subcommand. Use set or reset.", NamedTextColor.RED));
            }
        }

        return true;
    }
}
