package org.damagesmpcore.damageSmpCore;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DamageManager implements Listener {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    private final File dataFile;
    private final YamlConfiguration dataConfig;

    private final HashMap<UUID, Double> damagePercentMap = new HashMap<>();
    private final Random random = new Random();

    private final double maxPercent;
    private final double minKillBonus;
    private final double maxKillBonus;
    private final double minDeathLoss;
    private final double maxDeathLoss;

    public DamageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        this.minKillBonus = config.getDouble("damage.kill.min");
        this.maxKillBonus = config.getDouble("damage.kill.max");
        this.minDeathLoss = config.getDouble("damage.death.min_loss");
        this.maxDeathLoss = config.getDouble("damage.death.max_loss");
        this.maxPercent = config.getDouble("damage.max_percent");

        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Event: Player Join
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        double saved = dataConfig.getDouble(uuid.toString(), 0.0);
        damagePercentMap.put(uuid, saved);
    }

    // Event: Player Quit
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        dataConfig.set(uuid.toString(), damagePercentMap.getOrDefault(uuid, 0.0));
        saveDataFile();
    }

    // Save all on shutdown
    public void saveAllPlayerData() {
        for (Map.Entry<UUID, Double> entry : damagePercentMap.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }
        saveDataFile();
    }

    private void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Gain on kill
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer != null && entity.getType() != EntityType.PLAYER) {
            addStrength(killer);
        }
    }

    // Lose on death, gain to killer
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deceased = event.getEntity();
        reduceStrength(deceased);

        Player killer = deceased.getKiller();
        if (killer != null && !killer.getUniqueId().equals(deceased.getUniqueId())) {
            addStrength(killer);
        }
    }

    // Modify damage
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        double percent = damagePercentMap.getOrDefault(damager.getUniqueId(), 0.0);
        double bonus = (percent / 100.0) * 4.0; // 4.0 = 2 hearts
        event.setDamage(event.getDamage() + bonus);
    }

    private void updateStrengthMessage(Player player) {
        double percent = damagePercentMap.getOrDefault(player.getUniqueId(), 0.0);
        player.sendMessage(ChatColor.YELLOW + "Your Strength Boost: " + ChatColor.GREEN + String.format("%.1f", percent) + "%");
    }

    private double randomBetween(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private void addStrength(Player player) {
        UUID uuid = player.getUniqueId();
        double current = damagePercentMap.getOrDefault(uuid, 0.0);
        double gain = randomBetween(minKillBonus, maxKillBonus);
        current += gain;
        if (current > maxPercent) current = maxPercent;
        damagePercentMap.put(uuid, current);
        player.sendMessage(ChatColor.GREEN + "+" + String.format("%.1f", gain) + "% damage boost!");
        updateStrengthMessage(player);
    }

    private void reduceStrength(Player player) {
        UUID uuid = player.getUniqueId();
        double current = damagePercentMap.getOrDefault(uuid, 0.0);
        double loss = randomBetween(minDeathLoss, maxDeathLoss);
        current -= loss;
        if (current < 0) current = 0;
        damagePercentMap.put(uuid, current);
        player.sendMessage(ChatColor.RED + "-" + String.format("%.1f", loss) + "% damage lost.");
        updateStrengthMessage(player);
    }

    public double getPlayerStrength(UUID uuid) {
        return damagePercentMap.getOrDefault(uuid, 0.0);
    }

    public HashMap<UUID, Double> getAllPlayerData() {
        return damagePercentMap;
    }
}
