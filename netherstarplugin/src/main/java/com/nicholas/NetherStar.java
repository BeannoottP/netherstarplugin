package com.nicholas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


/*
 * netherstarplugin java plugin
 * TODO
 * Compass on spawn, compass on respawn - nick
 *    Im going to have some stuff for point perminance in onPlayerJoin, will probably want to put some of this logic in there
 * POINT STORING
 * Perminance
 * Potion effects - nick
 * 5 tick sanity checker
 * Chest interaction with nether star? - nick
 */
public class NetherStar extends JavaPlugin
{
  //sends message to console
  private static final Logger LOGGER = Logger.getLogger("NetherStarPlugin");

  //point key
  public static final NamespacedKey POINT_KEY = new NamespacedKey(getPlugin(NetherStar.class), "Points");
  
  //the player currently holding the nether star. should only be null if no one is holding nether star
  public static Player NSPLAYER = null;

  //the location compass points to. NOTE: NOT ALWAYS LOCATION OF NS PLAYER due to different dimensions, sometimes last portal used
  public static Location NSLOCATION = null;

  public void onEnable()
  {
    LOGGER.info("NetherStarPlugin enabled");
    
    getCommand("netherstar").setExecutor(new Commands(this));
    
    loadListeners(new com.nicholas.EventListener());

    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        potionEffects();
      }
    }, 0, 30);


  }

  public void onDisable()
  {
    LOGGER.info("NetherStarPlugin disabled");
  }
  
  //for start of event, called via command
  public void startPlugin() {
    Collection players = Bukkit.getServer().getOnlinePlayers();
    ArrayList<Player> realPlayers = new ArrayList<>();
    for (Object p : players) {
      if (p instanceof Player) {
        realPlayers.add((Player) p);
      }
    }
    Player firstReciever = realPlayers.get((int) (Math.random() * (realPlayers.size())));
    firstReciever.getInventory().addItem(new ItemStack(Material.NETHER_STAR));
    NSPLAYER = firstReciever;
    NSLOCATION = firstReciever.getLocation();
  }

  private void potionEffects() {
//TODO EVERYTHING
  }


  // if this is broken then god save us all
  private void loadListeners(Listener... listeners) {
    for (Listener listener : listeners) {
      Bukkit.getPluginManager().registerEvents(listener, this);
    }
  }





}
