package org.damagesmpcore.damageSmpCore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class DamageSmpCore extends JavaPlugin {
    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        getServer().getPluginManager().registerEvents(new DamageManager(this), this);
        DamageCommand cmd = new DamageCommand(this);
        getCommand("damage").setExecutor(cmd);

        getLogger().info("DamageSmpCore enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("DamageSmpCore disabled.");
    }


    public void reloadPluginConfig() {
        reloadConfig();
        getLogger().info("config.yml reloaded.");
    }
}

