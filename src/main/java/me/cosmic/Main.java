package me.cosmic;

import me.cosmic.items.DeathChestKey;
import me.cosmic.listeners.ChestListeners;
import me.cosmic.listeners.DeathListener;
import me.cosmic.managers.Datamanager;
import me.cosmic.managers.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Main extends JavaPlugin {

    public Datamanager datamanager;
    public DeathChestKey deathChestKey;
    public SQLManager sqlManager;

    @Override
    public void onEnable() {
        this.sqlManager = new SQLManager();
        try {
            sqlManager.connect();
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getLogger().info("Db could not be connected");
        }
        if (sqlManager.isConnected()) {
            Bukkit.getLogger().info("DataBase is connected!");
        }
        this.datamanager = new Datamanager(this);
        this.datamanager.getConfig().addDefault("delayForBroadCastInSeconds", 300);
        this.datamanager.getConfig().addDefault("delayForDestroying", 600);
        this.datamanager.saveConfig();
        System.out.println("Enabled RIP plugin by CosmicPvP");
        System.out.println("This plugin was made as a trial for DevRoom");
        getServer().getPluginManager().registerEvents(new DeathListener(this, this.sqlManager), this);
        getServer().getPluginManager().registerEvents(new ChestListeners(this, this.sqlManager), this);
        this.deathChestKey = new DeathChestKey(this);
        Bukkit.addRecipe(this.deathChestKey.getRecipe());
    }

    @Override
    public void onDisable() {
        System.out.println("Disabling RIP plugin by CosmicPvP");
        sqlManager.disconnect();
    }
}