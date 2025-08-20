package com.nicholas;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class EventListener implements Listener {


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
            Bukkit.broadcastMessage(NetherStar.NSPLAYER.getName() + " has picked up the nether star at " + NetherStar.NSPLAYER.getLocation().getX() + " " + NetherStar.NSPLAYER.getLocation().getY() +  " " + NetherStar.NSPLAYER.getLocation().getZ() + " in the " + NetherStar.NSPLAYER.getWorld().getEnvironment().name());
        }
    }
    
    //announces when NS player dies, sets ns player to null
    @EventHandler
    public static void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getEntity() == NetherStar.NSPLAYER) {
            //NICK FIX LOGIC HERE  
            Bukkit.broadcastMessage(NetherStar.NSPLAYER.getName() + " has died with the nether star at " + event.getEntity().getLocation().toString() + " in the " + event.getEntity().getWorld().getEnvironment().name());
            NetherStar.NSPLAYER = null;
        }
    }

    //if the player is not the NS player and enters the same dimension as NS player (not overworld), tell them where ns player is
    //if the player is ns player, and exits overworld, announce to the game
    @EventHandler
    public static void onPortalEntrance(PlayerPortalEvent event) {
        if (event.getPlayer() != NetherStar.NSPLAYER && event.getTo().getWorld().getEnvironment() != Environment.NORMAL && NetherStar.NSPLAYER.getWorld().getEnvironment() == event.getTo().getWorld().getEnvironment()) {
            event.getPlayer().sendMessage(NetherStar.NSPLAYER.getName() + " is in this dimension with the nether star at " + NetherStar.NSPLAYER.getLocation().getX() + " " + NetherStar.NSPLAYER.getLocation().getY() +  " " + NetherStar.NSPLAYER.getLocation().getZ());
            event.getPlayer().sendMessage("Your compass will not work in this dimension");
            return;
        }

        if (event.getPlayer() != NetherStar.NSPLAYER) {
            return;
        }
        
        if (event.getTo().getWorld().getEnvironment() != Environment.NORMAL) {
            Bukkit.broadcastMessage(NetherStar.NSPLAYER.getName() + " has used a portal at " + 
                                    event.getFrom().getX() + " " + event.getFrom().getY() +  " " + event.getFrom().getZ() + 
                                    " to enter the " + event.getTo().getWorld().getEnvironment().name());
            Bukkit.broadcastMessage("Your compasses will now point towards the last used portal");
            NetherStar.NSLOCATION = event.getFrom();
            return;
        }


    }

}
