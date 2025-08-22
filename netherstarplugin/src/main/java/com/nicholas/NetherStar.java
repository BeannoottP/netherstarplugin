package com.nicholas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;


/*
 * netherstarplugin java plugin
 * TODO
 * 5 tick sanity checker, check if nether star is inventory every 5 ticks 
 * Timed item drops - DONE DONE DONE
 * Sound effects
 * Fix broadcast messages of coordinates
 * Stylize broadcast messages
 * Prevent nether star despawning, teleport to world spawn or give to random person
 * change chest logic to check if inventory is not player inventory
 * donkey chest
 * prevent hotbar
 * prevent compass stacking - DONE
 * Loot drop revamp? Loottable class
 * /top command (because mining up sucks and wastes time) (maybe make it so you cant use /top if you are nearby other players?)
 */
public class NetherStar extends JavaPlugin
{
  //sends message to console
  public static final Logger LOGGER = Logger.getLogger("NetherStarPlugin");
  
  //sounds
  public static final Sound firework = Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST;
  public static final Sound witherDeath = Sound.ENTITY_WITHER_DEATH;
  public static final Sound ding = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
  public static final Sound portal = Sound.BLOCK_PORTAL_TRIGGER;

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
    

    RegisteredListener registeredListener = new RegisteredListener(new com.nicholas.EventListener(), (listener, event) -> com.nicholas.EventListener.onEvent(event), EventPriority.NORMAL, this, false);
    for (HandlerList handler : HandlerList.getHandlerLists()){
        handler.register(registeredListener);
    }

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
    playSoundGlobal(firework);
    Player firstReciever = realPlayers.get((int) (Math.random() * (realPlayers.size())));
    firstReciever.getInventory().addItem(new ItemStack(Material.NETHER_STAR));
    NSPLAYER = firstReciever;
    NSLOCATION = firstReciever.getLocation();
    plugin.potionEffects();
  }

  // if this is broken then god save us all
  private void loadListeners(Listener... listeners) {
    for (Listener listener : listeners) {
      Bukkit.getPluginManager().registerEvents(listener, this);
    }
  }

  //plays sound for one player
  public static void playSoundGlobal(Sound sound) {
    Bukkit.getServer().getOnlinePlayers().forEach(p -> playSoundPlayer(p, sound));
  }

  //plays sound for all players
  public static void playSoundPlayer(Player p, Sound sound) {
    p.playSound(p, sound, 1, 0);
  }





}
