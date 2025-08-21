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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


/*
 * netherstarplugin java plugin
 * TODO
 * Compass on spawn, compass on respawn - nick DONE DONE DONE
 *    Im going to have some stuff for point perminance in onPlayerJoin, will probably want to put some of this logic in there
 * POINT STORING
 * Perminance
 * Potion effects - nick DONE DONE DONE
 * 5 tick sanity checker, check if nether star is inventory every 5 ticks
 * Chest interaction with nether star? - nick DONE DONE DONE
 * Crafting Prevention - DONE DONE DONE
 * Timed item drops - DONE DONE DONE
 * Sound effects
 * Fix broadcast messages of coordinates
 * Stylize broadcast messages
 * FIX POINT KEY
 * Figure out how to use switch cases for storage items
 * Make it so people can only have 1 compass at a time
 */
public class NetherStar extends JavaPlugin
{
  //sends message to console
  private static final Logger LOGGER = Logger.getLogger("NetherStarPlugin");

  //point key
  //this line currently causes the plugin to fail, due to static implementatin of nether star class
  //public static final NamespacedKey POINT_KEY = new NamespacedKey(getPlugin(NetherStar.class), "Points");
  
  //the player currently holding the nether star. should only be null if no one is holding nether star
  public static Player NSPLAYER = null;

  //the location compass points to. NOTE: NOT ALWAYS LOCATION OF NS PLAYER due to different dimensions, sometimes last portal used
  public static Location NSLOCATION = null;

  //used to run methods in singletonlogic class
  private SingletonLogic plugin = SingletonLogic.getInstance();

  public void onEnable()
  {
    LOGGER.info("NetherStarPlugin enabled");
    
    getCommand("netherstar").setExecutor(new Commands(this));
    
    loadListeners(new com.nicholas.EventListener());

    //these 3 schedulers drop items and cancel the previous schedule when the new one is running
    //figure out better way to implement cancelation so it doesn't try to cancel multiple times in upcoming schedulers
    int ID_1 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        plugin.itemDropsFirstStage();
      }
    }, 0, 1200);

    int ID_2 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        Bukkit.getScheduler().cancelTask(ID_1);
        plugin.itemDropsSecondStage();
      }
    }, 2420, 1200);

    int ID_3 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        Bukkit.getScheduler().cancelTask(ID_2);
        plugin.itemDropsThirdStage();
      }
    }, 3620, 1200);



  }

  public void onDisable()
  {
    Bukkit.getServer().getScheduler().cancelTasks(this);
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
    plugin.potionEffects();
  }

  //MOVED TO SINGLETON LOGIC
  /*public void potionEffects() {
    if(NSPLAYER != null) {
      NSPLAYER.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 30, 1, true));
    }

  }*/


  // if this is broken then god save us all
  private void loadListeners(Listener... listeners) {
    for (Listener listener : listeners) {
      Bukkit.getPluginManager().registerEvents(listener, this);
    }
  }





}
