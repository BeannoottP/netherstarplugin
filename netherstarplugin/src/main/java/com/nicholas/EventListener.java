package com.nicholas;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.checkerframework.checker.units.qual.N;
import org.checkerframework.checker.units.qual.t;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.meta.CompassMeta;


public class EventListener implements Listener {




    //used to run methods from singletonlogic class
    private static SingletonLogic plugin = SingletonLogic.getInstance();

    private static ItemStack netherstaritemstack = new ItemStack(Material.NETHER_STAR);

    //used to check if ns player opens chest or storage
    public static boolean isStorage = false;

    //used to store items for what the nether star can be stored in NO LONGER NEEDED
    /*private static List<Material> storageitems = new ArrayList<>(Arrays.asList(
                                                                Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.HOPPER, 
                                                                Material.CHEST_MINECART, Material.HOPPER_MINECART, Material.SHULKER_BOX, 
                                                                Material.DISPENSER, Material.DROPPER, Material.CRAFTING_TABLE, Material.FURNACE, 
                                                                Material.BLAST_FURNACE, Material.SMOKER, Material.BARREL, Material.BREWING_STAND));*/

    
    





    /*
     * For non holders: Updates compass location for everyone on every player move not including nether star player
     *          Redundant but makes it a bit smoother, can remove if performance issues
     * 
     * For Holder: If in not overworld, do nothing
     * otherwise update nether star location to current location
     */
    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent event) {
        if(plugin.stopMove) {
            event.setCancelled(true);
        }

        if (NetherStar.NSLOCATION == null) {return;}
        
        if (event.getPlayer() != NetherStar.NSPLAYER) {
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                p.setCompassTarget(NetherStar.NSLOCATION);
            }
            if(event.getPlayer().getWorld().getEnvironment() != Environment.NETHER) {return;}
            PlayerInventory playerInventory = event.getPlayer().getInventory();
            int i = 0;
            for(ItemStack item : playerInventory) {
                if(item == null) {
                    i++;
                    continue;
                }
                if(item.getType().equals(Material.COMPASS)) {
                    CompassMeta compassmeta = (CompassMeta) event.getPlayer().getInventory().getItem(i).getItemMeta();
                    if(NetherStar.NSPLAYER == null) {
                        compassmeta.setLodestone(NetherStar.NSLOCATION_NETHER);
                        compassmeta.setLodestoneTracked(false);
                        event.getPlayer().getInventory().getItem(i).setItemMeta(compassmeta);
                        return;
                    }
                    if(NetherStar.NSPLAYER.getWorld().getEnvironment() == Environment.NORMAL) {
                        compassmeta.setLodestone(NetherStar.NSLOCATION_NETHER);
                        compassmeta.setLodestoneTracked(false);
                        event.getPlayer().getInventory().getItem(i).setItemMeta(compassmeta);
                        return;
                    }
                    compassmeta.setLodestone(NetherStar.NSPLAYER.getLocation());
                    compassmeta.setLodestoneTracked(false);
                    event.getPlayer().getInventory().getItem(i).setItemMeta(compassmeta);
                }
                i++;
            }
        }

        //so when the nether star player moves the sanity check returns to false
        plugin.sanityCheckDisable = false;

        if (event.getPlayer().getWorld().getEnvironment() != Environment.NORMAL) {
            NetherStar.NSLOCATION_NETHER = event.getPlayer().getLocation();
            return;
        }
        //this should never be reached unless the move is the nether star player, moving in the overworld
        NetherStar.NSLOCATION = event.getPlayer().getLocation();
    } 
    
    //stops player from dropping nether star
    @EventHandler
    public static void onDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.NETHER_STAR) {
            event.setCancelled(true);
        }
    }


    //announces when the nether star is picked up by player, updates nether star player
    @EventHandler
    public static void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {return;}
        
        if (event.getItem().getItemStack().getType() == Material.NETHER_STAR) {
            NetherStar.NSPLAYER = (Player) event.getEntity();
            //NICK FIX LOGIC HERE   
            plugin.potionEffects();
            NetherStar.playSoundGlobal(NetherStar.ding);
            int x_coord = event.getEntity().getLocation().getBlockX();
            int y_coord = event.getEntity().getLocation().getBlockY();
            int z_coord = event.getEntity().getLocation().getBlockZ();
            String dimension = event.getEntity().getWorld().getEnvironment().name();
            Bukkit.broadcastMessage(ChatColor.of(Color.CYAN) + "" + ChatColor.BOLD + NetherStar.NSPLAYER.getName() + ChatColor.RESET + " has picked up the nether star at " + ChatColor.of(Color.RED) + "" + ChatColor.BOLD + x_coord + ", " + y_coord + ", " + z_coord + ChatColor.RESET + " in the " + ChatColor.of(Color.CYAN) + "" + ChatColor.BOLD + plugin.dimensionchange(dimension));
            NetherStar.bossBar.setTitle(NetherStar.NSPLAYER.getDisplayName() + " has the star");
            return;
        }
        if (event.getItem().getItemStack().getType() == Material.COMPASS) {
            if (((Player)event.getEntity()).getInventory().contains(Material.COMPASS)) {
                event.setCancelled(true);
            }
        }
    }
    
    //announces when NS player dies, sets ns player to null
    @EventHandler
    public static void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        ItemStack[] playerInventory = event.getEntity().getInventory().getContents();
        //ItemStack[] playerArmor = event.getEntity().getInventory().getArmorContents();

        for (int i = 0; i < playerInventory.length; i++) {
            if (playerInventory[i] == null) {continue;}
            //NetherStar.LOGGER.info("2" + playerInventory[i].getType().name() + " " + (playerInventory[i].getItemMeta() instanceof Damageable));
            
            if (!(plugin.toolChecker(playerInventory[i]))) {
                NetherStar.LOGGER.info(playerInventory[i].getType().name() + " possible to drop");
                if (Math.random() < 0.4 || playerInventory[i].getType() == Material.NETHER_STAR) {
                    NetherStar.LOGGER.info(playerInventory[i].getType().name() + " tried to drop");
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), playerInventory[i]);
                    playerInventory[i] = new ItemStack(Material.AIR);
                }
            }
        }
        event.getEntity().getInventory().setContents(playerInventory);
        if(event.getEntity() == NetherStar.NSPLAYER) {
            NetherStar.playSoundGlobal(NetherStar.witherDeath); 
            int x_coord = event.getEntity().getLocation().getBlockX();
            int y_coord = event.getEntity().getLocation().getBlockY();
            int z_coord = event.getEntity().getLocation().getBlockZ();
            String dimension = event.getEntity().getWorld().getEnvironment().name();
            Bukkit.broadcastMessage(ChatColor.of(Color.CYAN) + "" + ChatColor.BOLD + NetherStar.NSPLAYER.getName() + ChatColor.RESET + " has died with the nether star at " + ChatColor.of(Color.RED) + "" + ChatColor.BOLD + x_coord + ", " + y_coord + ", " + z_coord + ChatColor.RESET + " in the " + ChatColor.of(Color.CYAN) + "" + ChatColor.BOLD + plugin.dimensionchange(dimension));
            plugin.clearPotionEffects();     
            NetherStar.NSPLAYER = null;
        }
    }

    //if the player is not the NS player and enters the same dimension as NS player (not overworld), tell them where ns player is
    //if the player is ns player, and exits overworld, announce to the game
    @EventHandler
    public static void onPortalEntrance(PlayerPortalEvent event) {
        if(event.getTo().getWorld().getEnvironment() == Environment.NORMAL) {
            Player p = event.getPlayer();
            for(ItemStack item : p.getInventory().getContents()) {
                if(item == null) {continue;}
                if(item.getType() != Material.COMPASS) {continue;}
                if(item.getType() == Material.COMPASS) {
                    p.getInventory().remove(Material.COMPASS);
                    p.getInventory().addItem(new ItemStack(Material.COMPASS));
                }
            }
        }
        if(NetherStar.NSPLAYER == null) {return;}
        if (event.getPlayer() != NetherStar.NSPLAYER && event.getTo().getWorld().getEnvironment() != Environment.NORMAL && NetherStar.NSPLAYER.getWorld().getEnvironment() == event.getTo().getWorld().getEnvironment()) {
            int x_coord = NetherStar.NSPLAYER.getLocation().getBlockX();
            int y_coord = NetherStar.NSPLAYER.getLocation().getBlockY();
            int z_coord = NetherStar.NSPLAYER.getLocation().getBlockZ();
            NetherStar.playSoundPlayer(event.getPlayer(), NetherStar.ding);
            event.getPlayer().sendMessage(NetherStar.NSPLAYER.getName() + " is in this dimension with the nether star at " + ChatColor.of(Color.RED) + "" + ChatColor.BOLD + x_coord + ", " + y_coord +  ", " + z_coord);
            event.getPlayer().sendMessage("Everytime you move your compass will point to the location of " + ChatColor.of(Color.CYAN) + "" + ChatColor.BOLD + NetherStar.NSPLAYER.getDisplayName());
            return;
        }

        if (event.getPlayer() != NetherStar.NSPLAYER) {
            return;
        }
        
        if (event.getTo().getWorld().getEnvironment() != Environment.NORMAL) {
            NetherStar.playSoundGlobal(NetherStar.portal);
            String dimension = event.getTo().getWorld().getEnvironment().toString();
            Bukkit.broadcastMessage(NetherStar.NSPLAYER.getName() + " has used a portal at " + ChatColor.of(Color.RED) + "" + ChatColor.BOLD +
                                    event.getFrom().getBlockX() + " " + event.getFrom().getBlockY() +  " " + event.getFrom().getBlockZ() + ChatColor.RESET + 
                                    " to enter the " + ChatColor.of(Color.RED) + "" + ChatColor.BOLD + plugin.dimensionchange(dimension));
            Bukkit.broadcastMessage("Your compasses will now point towards the last used portal");
            NetherStar.NSLOCATION = event.getFrom();
            return;
        }

        if (event.getTo().getWorld().getEnvironment() == Environment.NORMAL) {
            NetherStar.NSLOCATION_NETHER = event.getFrom();
            return;
        }
    }


    //checks if nether star player clicks a chest or storage block in order to stop them from placing it in there
    /*@EventHandler
    public static void onChestClick(PlayerInteractEvent event) {   
        if(event.getPlayer().equals(NetherStar.NSPLAYER)) {
            if(event.getClickedBlock() == null) {
                return;
            }
            else if(storageitems.contains(event.getClickedBlock().getType())) {
                isStorage = true;
            }
            else {
                isStorage = false;
            }
        }
    }*/

    @EventHandler
    public static void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() == NetherStar.NSPLAYER) {
            
            
            Inventory opened_inv = event.getInventory();
            if (opened_inv.getType().toString().equals("PLAYER")) {return;}
            else {
                isStorage = true;
            }
        }
    }

    //checks if the ns player clicks on the ns and isStorage is true to stop them from moving it into the chest
    @EventHandler
    public static void onInventoryNetherStarClick(InventoryClickEvent event) {
        if (event.getWhoClicked() != NetherStar.NSPLAYER) {return;}
        // i dont know if using an if statement here is more optimized or not
        // if(event.getCurrentItem.getType.equals(Material.NETHER_STAR)) {plugin.sanityCheckDisable = true;}
        plugin.sanityCheckDisable = true;
        if (event.getHotbarButton() != -1 && isStorage) {
            if(event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) == null) {return;}
            ItemStack hotbaritem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
            if(hotbaritem.isSimilar(netherstaritemstack)) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage("You can't move the Nether Star into storage");
                return;
            }
        }

            /*ItemStack[] inventory = event.getWhoClicked().getInventory().getContents();
            for (int i = 0; i < 9; i++) {
                if (inventory[i].getType() == Material.NETHER_STAR && event.getHotbarButton() == i) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage("You can't move the Nether Star into storage");
                    return;
                }
            }*/

        if(event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.NETHER_STAR) && isStorage) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage("You can't move the Nether Star into storage");
        }


        /*if (event.getCurrentItem().getType() == null ) {
            return;
        }
        
        else if(event.getWhoClicked().equals(NetherStar.NSPLAYER) && event.getCurrentItem().getType().equals(Material.NETHER_STAR) && isStorage) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage("You can't move the Nether Star into storage");
        }*/
    }

    //resets the chest bool so that they can move the nether star after closing inventory
    @EventHandler
    public static void onInventoryNetherStarClose(InventoryCloseEvent event) {
        if(event.getPlayer().equals(NetherStar.NSPLAYER) && isStorage) {
            isStorage = false;
        }
    }

    //gives compass on player join
    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        if(NetherStar.NSPLAYER != null) {
            NetherStar.bossBar.addPlayer(event.getPlayer());
        }
        if (event.getPlayer().getInventory().contains(Material.COMPASS)) {return;}
        
        event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
    }

    //gives compass on player respawn
    @EventHandler
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getPlayer().getInventory().contains(Material.COMPASS)) {
            Player p = event.getPlayer();
            for(ItemStack item : p.getInventory().getContents()) {
                if(item == null) {continue;}
                if(item.getType() != Material.COMPASS) {continue;}
                if(item.getType() == Material.COMPASS) {
                    p.getInventory().remove(Material.COMPASS);
                    p.getInventory().addItem(new ItemStack(Material.COMPASS));
                }
            }
            return;
        }
        event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
    }

    //Stops players from crafting a beacon
    @EventHandler
    public static void onBeaconCraft(CraftItemEvent event) {
        if(event.getRecipe().getResult().getType() == Material.BEACON) {
            event.setCancelled(true);
        }
    }

    //prevents the nehther star from burning
    @EventHandler
    public static void onItemBurn(EntityCombustEvent event) {
        if (event.getEntityType() != EntityType.ITEM) {return;}
        if (((Item)event.getEntity()).getItemStack().getType() == Material.NETHER_STAR) {
            event.setCancelled(true);
            World overworld = Bukkit.getWorld("world");
            NetherStar.playSoundGlobal(NetherStar.witherDeath);
            event.getEntity().teleport(new Location(overworld, 0, overworld.getHighestBlockYAt(0, 0) +1, 0));
            int x_world_spawn = event.getEntity().getWorld().getSpawnLocation().getBlockX();
            int y_world_spawn = event.getEntity().getWorld().getSpawnLocation().getBlockY();
            int z_world_spawn = event.getEntity().getWorld().getSpawnLocation().getBlockZ();
            event.getEntity().teleport(new Location(overworld, x_world_spawn, y_world_spawn +1, z_world_spawn));
            Bukkit.broadcastMessage("Moved the Nether Star to world spawn at " + ChatColor.of(Color.RED) + "" + ChatColor.BOLD + x_world_spawn + ", " + y_world_spawn + ", " + z_world_spawn + ChatColor.RESET + " due to item burn. Good luck :)");
            NetherStar.NSLOCATION = event.getEntity().getWorld().getSpawnLocation();
        }
    }

    @EventHandler
    public static void onItemLavaBurn(EntityRemoveEvent event) {
        if (event.getEntityType() != EntityType.ITEM) {return;}
        if (((Item)event.getEntity()).getItemStack().getType() == Material.NETHER_STAR) {
            World overworld = Bukkit.getWorld("world");
            NetherStar.playSoundGlobal(NetherStar.witherDeath);
            int x_world_spawn = overworld.getSpawnLocation().getBlockX();
            int y_world_spawn = overworld.getSpawnLocation().getBlockY();
            int z_world_spawn = overworld.getSpawnLocation().getBlockZ();
            overworld.dropItemNaturally(overworld.getSpawnLocation(), new ItemStack(Material.NETHER_STAR));
            Bukkit.broadcastMessage("Moved the Nether Star to world spawn at " + ChatColor.of(Color.RED) + "" + ChatColor.BOLD + x_world_spawn + ", " + y_world_spawn + ", " + z_world_spawn + ChatColor.RESET + " due to item burn. Good luck :)");
            NetherStar.NSLOCATION = overworld.getSpawnLocation();
        }
    }

    //spawns at spawn, 
    @EventHandler
    public static void onItemDespawn(ItemDespawnEvent event) {
        if (event.getEntity().getItemStack().getType() != Material.NETHER_STAR) { 
            return;
        }
            
        //this is dumb and there is definitley a better way to do it
        //tries default name for overworld, if that doesnt work, goes through all players and checks to see if one is in overworld, if that doesnt work, just give up
        World overworld = Bukkit.getWorld("world");
        boolean found = true;
        if (overworld == null || overworld.getEnvironment() != Environment.NORMAL) {
            found = false;
            for (Player p : Bukkit.getOnlinePlayers()) {
                overworld = p.getWorld();
                if (overworld != null && overworld.getEnvironment() == Environment.NORMAL) {
                    found = true;
                    break;
                }
            }
        }

        //will try again in 5 minutes
        if (!found) {
            NetherStar.LOGGER.info("Tried to spawn netherstar at spawn and failed");
            event.setCancelled(true);
            return;
        }
        
        NetherStar.playSoundGlobal(NetherStar.witherDeath);
        event.getEntity().teleport(new Location(overworld, 0, overworld.getHighestBlockYAt(0, 0) +1, 0));
        int x_world_spawn = event.getEntity().getWorld().getSpawnLocation().getBlockX();
        int y_world_spawn = event.getEntity().getWorld().getSpawnLocation().getBlockY();
        int z_world_spawn = event.getEntity().getWorld().getSpawnLocation().getBlockZ();
        event.getEntity().teleport(new Location(overworld, x_world_spawn, y_world_spawn +1, z_world_spawn));
        Bukkit.broadcastMessage("Moved the Nether Star to world spawn at " + ChatColor.of(Color.RED) + "" + ChatColor.BOLD + x_world_spawn + ", " + y_world_spawn + ", " + z_world_spawn + ChatColor.RESET + " due to despawn. Good luck :)");
        event.getEntity().setUnlimitedLifetime(true);
        NetherStar.NSLOCATION = event.getEntity().getWorld().getSpawnLocation();
        event.setCancelled(true);
    }

    @EventHandler
    public static void netherCompassClick(PlayerInteractEvent event) {
        if(event.getClickedBlock() != null) {
            if(event.getClickedBlock().getBlockData().getMaterial().equals(Material.LODESTONE)) {
                event.setCancelled(true);
            }
        }
        if(event.getPlayer().equals(NetherStar.NSPLAYER)) {return;}
        if(event.getPlayer().getWorld().getEnvironment() != Environment.NETHER) {return;}
        if(event.getItem() == null) {return;}
        if((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && event.getItem().getType().equals(Material.COMPASS)) {
            CompassMeta compassmeta = (CompassMeta) event.getItem().getItemMeta();
            if(NetherStar.NSPLAYER.getWorld().getEnvironment() == Environment.NORMAL) {
                event.getPlayer().sendMessage(NetherStar.NSPLAYER.getDisplayName() + "is not in the nether");
                compassmeta.setLodestone(NetherStar.NSLOCATION_NETHER);
                event.getItem().setItemMeta(compassmeta);
                return;
            }
            compassmeta.setLodestone(NetherStar.NSPLAYER.getLocation());
            compassmeta.setLodestoneTracked(false);
            event.getItem().setItemMeta(compassmeta);
        }
    }

    @EventHandler
    public static void damageNotDuringGame(EntityDamageEvent event) {
        if(plugin.stopMove && event.getEntity().getType().equals(EntityType.PLAYER)) {
            event.setCancelled(true);
        }
    }

}
