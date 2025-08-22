package com.nicholas;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.t;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareInventoryResultEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;


public class EventListener implements Listener {




    //used to run methods from singletonlogic class
    private static SingletonLogic plugin = SingletonLogic.getInstance();

    //used to check if ns player opens chest or storage
    public static boolean isStorage = false;

    //used to store items for what the nether star can be stored in
    private static List<Material> storageitems = new ArrayList<>(Arrays.asList(
                                                                Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.HOPPER, 
                                                                Material.CHEST_MINECART, Material.HOPPER_MINECART, Material.SHULKER_BOX, 
                                                                Material.DISPENSER, Material.DROPPER, Material.CRAFTING_TABLE, Material.FURNACE, 
                                                                Material.BLAST_FURNACE, Material.SMOKER, Material.BARREL, Material.BREWING_STAND));

    
    





    /*
     * For non holders: Updates compass location for everyone on every player move not including nether star player
     *          Redundant but makes it a bit smoother, can remove if performance issues
     * 
     * For Holder: If in not overworld, do nothing
     * otherwise update nether star location to current location
     */
    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent event) {
        if (NetherStar.NSLOCATION == null) {return;}
        
        if (event.getPlayer() != NetherStar.NSPLAYER) {
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                p.setCompassTarget(NetherStar.NSLOCATION);
            }
            return;
        }
        if (event.getPlayer().getWorld().getEnvironment() != Environment.NORMAL) {
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
            Bukkit.broadcastMessage(NetherStar.NSPLAYER.getName() + " has picked up the nether star at " + NetherStar.NSPLAYER.getLocation().getX() + " " + NetherStar.NSPLAYER.getLocation().getY() +  " " + NetherStar.NSPLAYER.getLocation().getZ() + " in the " + NetherStar.NSPLAYER.getWorld().getEnvironment().name());
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
        if(event.getEntity() == NetherStar.NSPLAYER) {
            //NICK FIX LOGIC HERE 
            NetherStar.playSoundGlobal(NetherStar.witherDeath); 
            Bukkit.broadcastMessage(NetherStar.NSPLAYER.getName() + " has died with the nether star at " + event.getEntity().getLocation().toString() + " in the " + event.getEntity().getWorld().getEnvironment().name());
            plugin.clearPotionEffects();
            event.getEntity().getWorld().setChunkForceLoaded(event.getEntity().getLocation().getBlockX(),event.getEntity().getLocation().getBlockZ() , true);
            NetherStar.NSPLAYER = null;
        }
    }

    //if the player is not the NS player and enters the same dimension as NS player (not overworld), tell them where ns player is
    //if the player is ns player, and exits overworld, announce to the game
    @EventHandler
    public static void onPortalEntrance(PlayerPortalEvent event) {
        if (event.getPlayer() != NetherStar.NSPLAYER && event.getTo().getWorld().getEnvironment() != Environment.NORMAL && NetherStar.NSPLAYER.getWorld().getEnvironment() == event.getTo().getWorld().getEnvironment()) {
            NetherStar.playSoundPlayer(event.getPlayer(), NetherStar.ding);
            event.getPlayer().sendMessage(NetherStar.NSPLAYER.getName() + " is in this dimension with the nether star at " + NetherStar.NSPLAYER.getLocation().getX() + " " + NetherStar.NSPLAYER.getLocation().getY() +  " " + NetherStar.NSPLAYER.getLocation().getZ());
            event.getPlayer().sendMessage("Your compass will not work in this dimension");
            return;
        }

        if (event.getPlayer() != NetherStar.NSPLAYER) {
            return;
        }
        
        if (event.getTo().getWorld().getEnvironment() != Environment.NORMAL) {
            NetherStar.playSoundGlobal(NetherStar.portal);
            Bukkit.broadcastMessage(NetherStar.NSPLAYER.getName() + " has used a portal at " + 
                                    event.getFrom().getX() + " " + event.getFrom().getY() +  " " + event.getFrom().getZ() + 
                                    " to enter the " + event.getTo().getWorld().getEnvironment().name());
            Bukkit.broadcastMessage("Your compasses will now point towards the last used portal");
            NetherStar.NSLOCATION = event.getFrom();
            return;
        }
    }


    @EventHandler
    public static void onPrepareInventoryResult(PrepareInventoryResultEvent event) {
        if (!event.getViewers().contains(NetherStar.NSPLAYER)) {return;}
        if (event.getView().getTopInventory().getType() != InventoryType.PLAYER || event.getResult().getType() == Material.NETHER_STAR) {
            event.setResult(new ItemStack(Material.AIR));
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.NETHER_STAR));
        }
    }


    /* 
    //checks if nether star player clicks a chest or storage block in order to stop them from placing it in there
    @EventHandler
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
    }

    

    //checks if the ns player clicks on the ns and isStorage is true to stop them from moving it into the chest
    @EventHandler
    public static void onInventoryNetherStarClick(InventoryClickEvent event) {
        if (event.getWhoClicked() != NetherStar.NSPLAYER) {return;}
        
        if (event.getHotbarButton() != -1) {
            ItemStack[] inventory = event.getWhoClicked().getInventory().getContents();
            for (int i = 0; i < 9; i++) {
                if (inventory[i].getType() == Material.NETHER_STAR && event.getHotbarButton() == i) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage("You can't move the Nether Star in a chest");
                    return;
                }
            }
        }

        if (event.getCurrentItem().getType() == null ) {
            return;
        }
        
        else if(event.getWhoClicked().equals(NetherStar.NSPLAYER) && event.getCurrentItem().getType().equals(Material.NETHER_STAR) && isStorage) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage("You can't move the Nether Star in a chest");
        }
    }

    //resets the chest bool so that they can move the nether star after closing inventory
    @EventHandler
    public static void onInventoryNetherStarClose(InventoryCloseEvent event) {
        if(event.getPlayer().equals(NetherStar.NSPLAYER) && isStorage) {
            isStorage = false;
        }
    }

    */


    //gives compass on player join
    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getInventory().contains(Material.COMPASS)) {return;}
        
        event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
    }

    //gives compass on player respawn
    @EventHandler
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getPlayer().getInventory().contains(Material.COMPASS)) {return;}

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
        boolean found = false;
        if (overworld == null || overworld.getEnvironment() != Environment.NORMAL) {
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
        event.getEntity().setUnlimitedLifetime(true);
        Bukkit.broadcastMessage("Moved the Nether Star to 0 0 due to despawn. Good luck :)");
        event.setCancelled(true);
    }

}
