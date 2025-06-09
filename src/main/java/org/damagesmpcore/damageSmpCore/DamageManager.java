package org.damagesmpcore.damageSmpCore;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class DamageManager implements Listener {
    private final DamageSmpCore plugin;

    public DamageManager(DamageSmpCore plugin) {
        this.plugin = plugin;
    }

    private double getDamagePercent(UUID uuid) {
        return plugin.getConfig().getDouble("players." + uuid, 0.0);
    }

    private void setDamagePercent(UUID uuid, double percent) {
        double max = plugin.getConfig().getDouble("damage.max_percent", 100.0);
        percent = Math.max(0.0, Math.min(max, percent));
        plugin.getConfig().set("players." + uuid, percent);
        plugin.saveConfig();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer == null) return;

        double minGain = plugin.getConfig().getDouble("damage.kill.min", 1.0);
        double maxGain = plugin.getConfig().getDouble("damage.kill.max", 3.5);
        double gain    = minGain + Math.random() * (maxGain - minGain);

        UUID id = killer.getUniqueId();
        double before = getDamagePercent(id);
        setDamagePercent(id, before + gain);

        String victimName = e.getEntity() instanceof Player
                ? ((Player)e.getEntity()).getName()
                : e.getEntity().getType().name().toLowerCase().replace('_',' ');
        killer.sendMessage(String.format("§aYou killed %s! +%.2f%% damage",
                victimName, gain, getDamagePercent(id)));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        UUID id = e.getEntity().getUniqueId();
        double minLoss = plugin.getConfig().getDouble("damage.death.min_loss", 10.0);
        double maxLoss = plugin.getConfig().getDouble("damage.death.max_loss", 30.0);
        double loss    = minLoss + Math.random() * (maxLoss - minLoss);

        double before = getDamagePercent(id);
        setDamagePercent(id, before - loss);

        e.getEntity().sendMessage(
                String.format("§cYou died! -%.2f%% damage (now %.2f%%)", loss, getDamagePercent(id))
        );
    }
}

