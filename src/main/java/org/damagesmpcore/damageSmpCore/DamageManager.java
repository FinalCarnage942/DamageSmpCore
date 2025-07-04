package org.damagesmpcore.damageSmpCore;


import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
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
        double gain = minGain + Math.random() * (maxGain - minGain);

        UUID id = killer.getUniqueId();
        double before = getDamagePercent(id);
        setDamagePercent(id, before + gain);

        String victimName = e.getEntity() instanceof Player
                ? ((Player) e.getEntity()).getName()
                : e.getEntity().getType().name().toLowerCase().replace('_', ' ');
        killer.sendMessage(String.format("§aYou killed %s! +%.2f%% damage",
                victimName, gain, getDamagePercent(id)));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        UUID attacker = Objects.requireNonNull(e.getPlayer().getKiller()).getUniqueId();
        UUID id = e.getPlayer().getUniqueId();
        double minLoss = plugin.getConfig().getDouble("damage.death.min_loss", 10.0);
        double maxLoss = plugin.getConfig().getDouble("damage.death.max_loss", 30.0);
        double loss = minLoss + Math.random() * (maxLoss - minLoss);

        double before = getDamagePercent(id);
        setDamagePercent(id, before - loss);

        e.getEntity().sendMessage(
                String.format("§cYou died! -%.2f%% damage (now %.2f%%)", loss, getDamagePercent(id))
        );
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player attacker) {
            double damage = e.getDamage();      // Calculated damage before armor reduction
            double finalDamage = e.getFinalDamage(); // Damage after armor/truce/etc.
            // Now you can apply your custom percentage modifier
            double percent = getDamagePercent(attacker.getUniqueId());
            double modified = finalDamage + finalDamage * (percent / 100.0);
            e.setDamage(modified);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            boolean gotit = e.getPlayer().discoverRecipe(DamageSmpCore.recipe.getKey()); // Allow players to see the heart recipe
            e.getPlayer().sendMessage(String.valueOf(gotit));

        }, 20L); // Run after 1 second
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (!e.getAction().toString().contains("RIGHT")) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.FERMENTED_SPIDER_EYE) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        if (meta.getPersistentDataContainer().has(DamageSmpCore.heartkey)) {
            Player player = e.getPlayer();

            player.sendMessage(Component.text("§cYou feel your health surge!"));
            AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
            assert healthAttribute != null;
            healthAttribute.setBaseValue(healthAttribute.getValue() + 2.0);


            item.setAmount(item.getAmount() - 1); // Consume 1 heart
            e.setCancelled(true); // Cancel normal eating behavior
        }
    }


}

