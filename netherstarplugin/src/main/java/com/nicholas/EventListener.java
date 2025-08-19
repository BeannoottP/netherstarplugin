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

public class EventListener implements Listener {

    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent event) {
        if (NetherStar.NSPLAYER == null || NetherStar.NSLOCATION == null) {return;}
        
        if (event.getPlayer() != NetherStar.NSPLAYER) {
            event.getPlayer().setCompassTarget(NetherStar.NSLOCATION);
            return;
        }
        if (event.getPlayer().getWorld().getEnvironment() == Environment.NETHER) {
            return;
        }
        NetherStar.NSLOCATION = event.getPlayer().getLocation();
    } 
    
    @EventHandler
    public static void onDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.NETHER_STAR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {return;}
        
        if (event.getItem().getItemStack().getType() == Material.NETHER_STAR) {
            NetherStar.NSPLAYER = (Player) event.getEntity();   
            Bukkit.broadcastMessage(NetherStar.NSPLAYER.getAsString() + " has picked up the nether star at " + NetherStar.NSPLAYER.getLocation().toString() + " in the " + NetherStar.NSPLAYER.getWorld().getEnvironment().name());
        }
    }
    
    public static void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getEntity() == NetherStar.NSPLAYER) {
            Bukkit.broadcastMessage(NetherStar.NSPLAYER.getAsString() + " has died with the nether star at " + event.getEntity().getLocation().toString() + " in the " + event.getEntity().getWorld().getEnvironment().name());
            NetherStar.NSPLAYER = null;
        }
    }

}
