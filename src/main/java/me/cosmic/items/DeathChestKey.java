package me.cosmic.items;

import me.cosmic.Main;
import me.cosmic.managers.SQLManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class DeathChestKey {

    private Main main;

    public DeathChestKey (Main main) {
        this.main = main;
    }

    public ShapedRecipe getRecipe() {

        ItemStack deathChestKey = new ItemStack(Material.STICK);
        NamespacedKey key = new NamespacedKey(this.main, "DeathChestKey");

        ItemMeta meta = deathChestKey.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(this.main, "isDeathChestKey"), PersistentDataType.INTEGER, 1);
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "This key allows you to access the death chest of other players");
        meta.setDisplayName(ChatColor.RED + "Death Chest Key");
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setLore(lore);
        deathChestKey.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(key, deathChestKey);
        recipe.shape(" T "," E ","   ");
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('E', Material.EMERALD);
        return recipe;
    }

}
