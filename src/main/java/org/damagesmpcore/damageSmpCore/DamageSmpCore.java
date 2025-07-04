package org.damagesmpcore.damageSmpCore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Objects;

public final class DamageSmpCore extends JavaPlugin {
    private File configFile;
    private FileConfiguration config;
    public static NamespacedKey heartkey;
    public static ShapedRecipe recipe;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        heartkey = new NamespacedKey(this, "HEARTRECIPE");

        getServer().getPluginManager().registerEvents(new DamageManager(this), this);
        DamageCommand cmd = new DamageCommand(this);
        Objects.requireNonNull(getCommand("damage")).setExecutor(cmd);
        registerHeartRecipe(this);

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

    public static void registerHeartRecipe(JavaPlugin plugin) {

        ItemStack result = createHeartItem();


        recipe = new ShapedRecipe(heartkey, result);
        recipe.shape("GDG", "DHD", "GDG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('H', new RecipeChoice.ExactChoice(new ItemStack(Material.FERMENTED_SPIDER_EYE)));
        recipe.setGroup("heart_items");
        recipe.setCategory(CraftingBookCategory.MISC);

        Bukkit.addRecipe(recipe);

    }


    public static ItemStack createHeartItem() {

        ItemStack heart = new ItemStack(Material.FERMENTED_SPIDER_EYE);
        ItemMeta meta = heart.getItemMeta();

        meta.displayName(Component.text("§c§lHeart"));
        meta.lore(List.of(
                Component.text("§7A special item that grants you a heart"),
                Component.text("§7when consumed. Use wisely!"),
                Component.text("§7Right-click to consume.")
        ));
        meta.getPersistentDataContainer().set(heartkey, PersistentDataType.BOOLEAN, true);
        heart.setItemMeta(meta);
        return heart;
    }
}

