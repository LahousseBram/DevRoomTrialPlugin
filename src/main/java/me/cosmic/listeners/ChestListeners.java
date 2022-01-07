package me.cosmic.listeners;

import me.cosmic.Main;
import me.cosmic.managers.SQLManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ChestListeners implements Listener {

    private final Main main;
    private final SQLManager sqlManager;

    public ChestListeners(Main main, SQLManager sqlManager) {
        this.main = main;
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
    public void onDeathChestOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK){
            if (event.getClickedBlock().getState() instanceof TileState) {
                TileState tileState = (TileState) event.getClickedBlock().getState();
                PersistentDataContainer persistentDataContainer = tileState.getPersistentDataContainer();
                if (persistentDataContainer.isEmpty()) return;
                try {
                    if (persistentDataContainer.get(new NamespacedKey(main, "isDeathChest"), PersistentDataType.INTEGER) == 0)
                        return;
                    else {
                        if (!(event.getClickedBlock().getType() == Material.CHEST)) return;
                        player.sendMessage(ChatColor.RED + "You cannot destroy a death chest");
                        event.setCancelled(true);
                        return;
                    }
                } catch (Error e) {
                    return;
                }
            } else if (event.getClickedBlock().getType() == Material.OAK_WALL_SIGN) {
                Location loc = new Location(event.getClickedBlock().getWorld(), event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ()+1);
                if (loc.getBlock().getState() instanceof TileState) {
                    if(!isDeathChest(loc.getBlock())) return;
                    player.sendMessage("You cannot destroy the sign on a death chest");
                    event.setCancelled(true);
                }
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            String playerUniqueID = player.getUniqueId().toString();
            try {
                if (event.getClickedBlock().getType().equals(Material.CHEST) && event.getClickedBlock() != null) {
                    Block block = event.getClickedBlock();
                    if (block.getState() instanceof TileState) {
                        TileState tileState = (TileState) block.getState();
                        PersistentDataContainer container = tileState.getPersistentDataContainer();
                        try {
                            if (container.get(new NamespacedKey(main, "isDeathChest"), PersistentDataType.INTEGER) == 0)
                                return;
                        } catch (Exception e) {
                            return;
                        }
                        String playerUniqueIDChest = container.get(new NamespacedKey(main, "ChestOwner"), PersistentDataType.STRING);
                        if (!(playerUniqueIDChest.equals(playerUniqueID))) {
                            ItemStack itemUsed = event.getItem();
                            player.sendMessage("1");
                            if (itemUsed == null){
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "It appears that you are not the owner of that deathchest!");
                            }
                            player.sendMessage("2");
                            PersistentDataContainer itemContainer = itemUsed.getItemMeta().getPersistentDataContainer();
                            player.sendMessage("3");
                            NamespacedKey key = new NamespacedKey(this.main, "isDeathChestKey");
                            player.sendMessage("4");
                            if (itemContainer.has(key, PersistentDataType.INTEGER)) {
                                player.sendMessage("5");
                                if (itemContainer.get(key, PersistentDataType.INTEGER) != 1) {
                                    player.sendMessage("6");
                                    event.setCancelled(true);
                                    player.sendMessage(ChatColor.RED + "It appears that you are not the owner of that deathchest!");
                                } else {
                                    player.sendMessage("7");
                                    player.getInventory().removeItem(itemUsed);
                                }
                            } else {
                                player.sendMessage("8");
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "It appears that you are not the owner of that deathchest!");
                            }
                        } else {
                            player.sendMessage(ChatColor.GREEN + "You are the owner of this deathchest and you are allowed to access it's contents");
                        }
                    }
                }
            } catch (Error e) {
                player.sendMessage("Error: " + e);
            }
        }
    }

    public static void dropContentsOfDeathChest(ItemStack[] contents, World world, Location location) {
        for (ItemStack content : contents) {
            if (content != null) {
                world.dropItemNaturally(location, content);
            }
        }
    }

    @EventHandler
    public void onCloseDeathChest(InventoryCloseEvent event) {
        if (event.getInventory().getType().equals(InventoryType.CHEST)) {
            Player player = (Player) event.getPlayer();
            Block deathChest = event.getInventory().getLocation().getBlock();
            if (!(deathChest.getState() instanceof TileState)) return;
            TileState tileState = (TileState) deathChest.getState();
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            try {
                if (container.get(new NamespacedKey(main, "isDeathChest"), PersistentDataType.INTEGER) == 0) return;
            } catch (Exception e) {
                return;
            }
            ItemStack[] contents = event.getInventory().getContents();
            if (contents.length > 0) {
                Location location = deathChest.getLocation();
                World world = location.getWorld();
                dropContentsOfDeathChest(contents, world, location);
                despawnChest(deathChest, world, location, player);
                Location loc = new Location(world, deathChest.getX()+1, deathChest.getY(), deathChest.getZ());
                Block deathChestDouble = loc.getBlock();
                if (container.has(new NamespacedKey(main, "isDoubleDeathChest"), PersistentDataType.INTEGER)) {
                    if (container.get(new NamespacedKey(main, "isDoubleDeathChest"), PersistentDataType.INTEGER) == 1) {
                        destroyDoubleChest(deathChest);
                    }
                }

                this.sqlManager.removeDeathChest(player);

                world.spawnParticle(Particle.ELECTRIC_SPARK, deathChest.getLocation(), 30);

                container.remove(new NamespacedKey(main, "isDeathChest"));
                container.remove(new NamespacedKey(main, "ChestOwner"));
                tileState.update();
                player.sendMessage(ChatColor.GREEN + "Your death chest was destroyed and if there were any remaining items in there, they were dropped on the ground.");
            }
        }
    }
}
