package me.cosmic.listeners;

import me.cosmic.Main;
import me.cosmic.managers.Datamanager;
import me.cosmic.managers.SQLManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class DeathListener implements Listener {

    public Datamanager datamanager;
    private static Main main;
    private static SQLManager sqlManager;

    public DeathListener(Main main, SQLManager sqlManager) {
        this.main = main;
        this.datamanager = new Datamanager(main);
        this.sqlManager = sqlManager;
    }

    public void removeDeathSign(World world, Location loc) {
        Location loc1 = new Location(world, loc.getX()+1, loc.getY(), loc.getZ()-1);
        Location loc2 = new Location(world, loc.getX(), loc.getY(), loc.getZ()-1);

        if (loc1.getBlock().getType() == Material.OAK_WALL_SIGN) {
            loc1.getBlock().setType(Material.AIR);
        } else if (loc2.getBlock().getType() == Material.OAK_WALL_SIGN) {
            loc2.getBlock().setType(Material.AIR);
        }
    }

    public boolean despawnChest(Block block, World world, Location loc, Player player) {
        //start remove single chest
        if (block.getType() != Material.CHEST) return false;
        if (!(block.getState() instanceof TileState)) return false;
        TileState tileState = (TileState) block.getState();
        PersistentDataContainer container = tileState.getPersistentDataContainer();
        //start remove sign
        Location loc2 = new Location(world, loc.getX(), loc.getY(), loc.getZ()-1);
        player.sendMessage(""+loc2.getBlock().getType());
        loc2.getBlock().setType(Material.AIR);
        //end remove sign
        if(!(container.has(new NamespacedKey(main, "isDeathChest"), PersistentDataType.INTEGER))) return false;
        int deathChest = container.get(new NamespacedKey(main, "isDeathChest"), PersistentDataType.INTEGER);
        if(deathChest != 1) return false;
        block.setType(Material.AIR);
        container.remove(new NamespacedKey(main, "isDeathChest"));
        container.remove(new NamespacedKey(main, "ChestOwner"));
        tileState.update();
        player.sendMessage(ChatColor.GREEN + "Your death chest was destroyed and if there were any remaining items in there, they were dropped on the ground.");
        //end remove single chest
        return true;
    }

    public void destroyDoubleChest(Block block) {
        Location checkP1 = new Location(block.getWorld(), block.getX()+1, block.getY(), block.getZ());
        Location checkM1 = new Location(block.getWorld(), block.getX()-1, block.getY(), block.getZ());

        if (checkP1.getBlock().getState() instanceof TileState) {
            Block b = checkP1.getBlock();
            TileState tileState = (TileState) b.getState();
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            if (container.has(new NamespacedKey(main, "isDoubleDeathChest"), PersistentDataType.INTEGER)) {
                b.setType(Material.AIR);
                container.remove(new NamespacedKey(main, "isDeathChest"));
                container.remove(new NamespacedKey(main, "ChestOwner"));
                tileState.update();
            }
        } else if (checkM1.getBlock().getState() instanceof TileState) {
            Block b = checkM1.getBlock();
            TileState tileState = (TileState) b.getState();
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            if (container.has(new NamespacedKey(main, "isDoubleDeathChest"), PersistentDataType.INTEGER)) {
                removeDeathSign(b.getWorld(), b.getLocation());
                b.setType(Material.AIR);
                container.remove(new NamespacedKey(main, "isDeathChest"));
                container.remove(new NamespacedKey(main, "ChestOwner"));
                tileState.update();
            }
        }
    }

    public boolean isDeathChest(Block block) {
        if (block.getState() instanceof TileState) {
            TileState tileState = (TileState) block.getState();
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            return container.get(new NamespacedKey(main, "isDeathChest"), PersistentDataType.INTEGER) == 1;
        }
        return false;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        org.bukkit.block.data.type.Chest chestData1;
        org.bukkit.block.data.type.Chest chestData2;

        if (event.getEntityType() == EntityType.PLAYER) {
            org.bukkit.block.Chest bChest;

            Player player = event.getEntity().getPlayer();

            boolean execute = true;

            int amount = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null)
                    amount += 1;
            }

            if (amount != 0) {
                Location loc = player.getLocation();

                final List<ItemStack> content = new ArrayList<ItemStack>();
                content.addAll(event.getDrops());

                final ItemStack[] items = content.toArray(new ItemStack[content.size()]);
                final double x = loc.getX();

                final Location x1 = loc.clone();
                final Location x2 = loc.clone();
                final Location x3 = new Location(loc.getWorld(), x, loc.getY(), loc.getZ() - 1);

                x2.setX(x + 1);

                Block block1 = x1.getBlock();
                Block signBlock = x3.getBlock();
                player.sendMessage(signBlock.getType() + "");
                Block block2 = x2.getBlock();
                int itemAmount = 0;
                for (final ItemStack item : items) {
                    itemAmount++;
                }

                //Single chest
                if (itemAmount < 27) {
                    block1.setType(Material.CHEST);
                    if (block1.getState() instanceof TileState) {
                        TileState tileState = (TileState) block1.getState();
                        PersistentDataContainer container = tileState.getPersistentDataContainer();
                        container.set(new NamespacedKey(main, "isDeathChest"), PersistentDataType.INTEGER, 1);
                        container.set(new NamespacedKey(main, "ChestOwner"), PersistentDataType.STRING, player.getUniqueId().toString());
                        signBlock.setType(Material.OAK_WALL_SIGN);
                        Sign sign = (Sign) signBlock.getState();
                        sign.setLine(0, "Deathchest of:");
                        sign.setLine(1, player.getDisplayName());
                        sign.update();
                        tileState.update();
                    }
                }
                //Double Chest
                else {
                    block1.setType(Material.CHEST);
                    block2.setType(Material.CHEST);

                    Chest chest1 = (Chest) block1.getState();
                    Chest chest2 = (Chest) block2.getState();

                    chestData1 = (org.bukkit.block.data.type.Chest) chest1.getBlockData();
                    chestData2 = (org.bukkit.block.data.type.Chest) chest2.getBlockData();

                    signBlock.setType(Material.OAK_WALL_SIGN);
                    Sign sign = (Sign) signBlock.getState();
                    sign.setLine(0, "Deathchest of:");
                    sign.setLine(1, player.getDisplayName());
                    sign.update();

                    chestData1.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
                    block1.setBlockData(chestData1, true);
                    chestData2.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
                    block2.setBlockData(chestData2, true);

                    if (block2.getState() instanceof TileState) {
                        TileState tileState1 = (TileState) block1.getState();
                        PersistentDataContainer container1 = tileState1.getPersistentDataContainer();
                        container1.set(new NamespacedKey(main, "isDeathChest"), PersistentDataType.INTEGER, 1);
                        NamespacedKey key1 = new NamespacedKey(main, "ChestOwner");
                        container1.set(key1, PersistentDataType.STRING, player.getUniqueId().toString());
                        container1.set(new NamespacedKey(main, "isDoubleDeathChest"), PersistentDataType.INTEGER, 1);
                        tileState1.update();
                    }
                    if (block2.getState() instanceof TileState) {
                        TileState tileState2 = (TileState) block2.getState();
                        PersistentDataContainer container2 = tileState2.getPersistentDataContainer();
                        container2.set(new NamespacedKey(main, "isDeathChest"), PersistentDataType.INTEGER, 1);
                        NamespacedKey key2 = new NamespacedKey(main, "ChestOwner");
                        container2.set(key2, PersistentDataType.STRING, player.getUniqueId().toString());
                        container2.set(new NamespacedKey(main, "isDoubleDeathChest"), PersistentDataType.INTEGER, 1);
                        tileState2.update();
                    }
                }

                bChest = (org.bukkit.block.Chest) x1.getBlock().getState();
                bChest.setCustomName(player.getName() + "'s death chest!");
                bChest.update();
                for (final ItemStack item : items) {
                    bChest.getInventory().addItem(item);
                }

                sqlManager.registerDeathChest(loc, player);

                event.getDrops().clear();

                main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
                    int counter = 0;

                    public void run() {
                        if (isDeathChest(block1) && counter < 5) {
                            player.sendMessage(ChatColor.GRAY + "Your death chest has spawned on the following coords: (x: " + ChatColor.RED + loc.getX() + ChatColor.GRAY + ", y: " + ChatColor.RED + loc.getY() + ChatColor.GRAY + ", z: " + ChatColor.RED + loc.getZ() + ChatColor.GRAY + ")");
                            counter++;
                        }
                    }
                }, 0L, 6000L);

                main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
                    public void run() {
                        Location loc = block1.getLocation();
                        Location particleLocation = new Location(block1.getWorld(), loc.getX()+0.5, loc.getY()+1, loc.getZ()+0.5);
                        boolean running = true;
                        try {
                            if (block1.getState() instanceof TileState) {
                                TileState tileState = (TileState) block1.getState();
                                if (block1.getType() == Material.CHEST) {
                                    PersistentDataContainer pdc = tileState.getPersistentDataContainer();
                                    NamespacedKey nsk = new NamespacedKey(main, "isDeathChest");
                                    if (pdc.has(nsk, PersistentDataType.INTEGER)) {
                                        if (pdc.get(nsk, PersistentDataType.INTEGER) == 1) {
                                            block1.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, particleLocation, 4);
                                        }else {
                                            running = false;
                                        }
                                    }else {
                                        running = false;
                                    }
                                }else {
                                    running = false;
                                }
                            } else {
                                running = false;
                            }
                        } catch (Exception e) {
                            //oof
                        }
                    }
                }, 0L, 2L);

                int delayConfigurable = datamanager.getConfig().getInt("delayForBroadCastInSeconds");
                Long ticksDelayConfigurable = delayConfigurable * 20L;

                main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                    public void run() {
                        main.getServer().broadcastMessage(ChatColor.GRAY + "There is a deathchest that belongs to player: " + ChatColor.RED + player.getDisplayName() + ChatColor.GRAY + ". It is located on the following coords: (x: " + ChatColor.RED + loc.getX() + ChatColor.GRAY + ", y: " + ChatColor.RED + loc.getY() + ChatColor.GRAY + ", z: " + ChatColor.RED + loc.getZ() + ChatColor.GRAY + ")");
                    }
                }, ticksDelayConfigurable);

                int delayUntilDestroyConfigurable = datamanager.getConfig().getInt("delayForDestroying");
                Long ticksDelayUntilDestroy = delayUntilDestroyConfigurable * 20L;

                main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                    public void run() {
                        player.sendMessage("trying to get the state and cast it to chest...");
                        player.sendMessage(block1.getState() + "");
                        if (!(block1.getState() instanceof Chest)) return;
                        Chest chest = (Chest) block1.getState();
                        player.sendMessage("Successfully cast to chest!");
                        player.sendMessage(ChatColor.GRAY + "Your death chest was destroyed and it's contents were dropped on the ground.");
                        player.sendMessage(ChatColor.RED + "WARNING! All players can see and pickup the items.");
                        removeDeathSign(loc.getWorld(), loc);
                        ItemStack[] items = chest.getInventory().getContents();
                        sqlManager.removeDeathChest(player);
                        for (ItemStack item : items) {
                            player.sendMessage(item + "");
                        }
                        player.sendMessage(bChest.getInventory().getContents().length + "");
                        ChestListeners.dropContentsOfDeathChest(items, block1.getWorld(), block1.getLocation());
                        despawnChest(block1, block1.getWorld(), block1.getLocation(), player);
                        destroyDoubleChest(block1);
                    }
                }, ticksDelayUntilDestroy);
                player.sendMessage("Your items got saved in a death chest that spawned on the location of your death.");
            }
        }
    }
}
