package org.damagesmpcore.damageSmpCore;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class DamageSmpCore extends JavaPlugin {

    private DamageManager damageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        damageManager = new DamageManager(this);
        this.getCommand("damagepercent").setExecutor(new DamageCommand(damageManager, this));
    }

    @Override
    public void onDisable() {
        for (UUID uuid : damageManager.getAllPlayerData().keySet()) {
            double percent = damageManager.getPlayerStrength(uuid);
            getConfig().set("players." + uuid.toString(), percent);
        }
        saveConfig();
    }

}
