package com.nicholas;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.Server;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;


/*
 * netherstarplugin java plugin
 * TODO
 * 5 tick sanity checker doesn't work in off hand
 * Loot pool and timed item drops, figure out what to do
 * Loot drop revamp? Loottable class
 * /top command (because mining up sucks and wastes time) (maybe make it so you cant use /top if you are nearby other players?)
 * World border
 * Win condition + sudden death
 * burning star is a little bit broken, jumping in the lava in the nether causes 2 stars to be produced
 * keep inventory working but need to figure out what to keep
 * figure out what to do if nether star player leaves the game
 * 
 * UPDATED TODO
 * fix duping nether stars (update sanity checker?) 
 * off hand glitch DONE 
 * improve loot drops to make people wanna hold the star
 * add world border to stop people from running so much, make it shrink to sudden death DONE
 * change keep inventory to 40% of everything except for pickaxe, axe, shovel, sword, and food DONE
 * change nether compass to always update wwhile moving DONE
 * prestart adventure mode DONE
 * 
 */
public class NetherStar extends JavaPlugin
{
  //sends message to console
  public static final Logger LOGGER = Logger.getLogger("NetherStarPlugin");
  
  //sounds
  public static final Sound firework = Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST;
  public static final Sound witherDeath = Sound.ENTITY_WITHER_DEATH;
  public static final Sound dragongrowl = Sound.ENTITY_ENDER_DRAGON_GROWL;
  public static final Sound ding = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
  public static final Sound portal = Sound.BLOCK_PORTAL_TRIGGER;
  public static final Sound enderdragdeath = Sound.ENTITY_ENDER_DRAGON_DEATH;
  public static final Sound musiccat = Sound.MUSIC_DISC_CAT;
  public static final Sound musicstal = Sound.MUSIC_DISC_STAL;

  //the player currently holding the nether star. should only be null if no one is holding nether star
  public static Player NSPLAYER = null;

  //the location compass points to. NOTE: NOT ALWAYS LOCATION OF NS PLAYER due to different dimensions, sometimes last portal used
  public static Location NSLOCATION = null;

  public static Location NSLOCATION_NETHER = null;

  public static int countdown = 10;

  public static int ID_Countdown;

  public static int ID_Start;

  public static int ID_timer;

  public static int timer = 10800;
  //public static int timer = 80;

  public boolean startcode = false;

  public boolean winCondition = false;

  public WorldBorder worldBorder;

  public static BossBar bossBar;

  public Scoreboard scoreboard;

  public Objective timerDisplay;

  public ArrayList<Player> realPlayers = new ArrayList<>();
  

  //used to run methods in singletonlogic class
  private SingletonLogic plugin = SingletonLogic.getInstance();

  public void onEnable()
  {
    LOGGER.info("NetherStarPlugin enabled");
    
    Commands c = new Commands(this);
    getCommand("netherstar").setExecutor(c);
    getCommand("top").setExecutor(c);

    loadListeners(new com.nicholas.EventListener());
    
    //tps counter that google ai spit out
    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
        long lastTickTime = System.currentTimeMillis();
        int ticks = 0;
        double tps = 20.0; // Initialize with perfect TPS

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long timeElapsed = currentTime - lastTickTime;
            

            ticks++;
            if (timeElapsed >= 1000) { // Check every second
                tps = (double) ticks / (timeElapsed / 1000.0);
                ticks = 0;
                // You can then log this 'tps' value to the console
                LOGGER.info("Current TPS: " + String.format("%.2f", tps));
                lastTickTime = currentTime;
            }
        }
    }, 0L, 1L); // Run every tick

    //pregame start state
    plugin.stopMove = true;
    worldBorder = Bukkit.getWorld("world").getWorldBorder();
    Location spawnlocation = Bukkit.getServer().getWorld("world").getSpawnLocation();
    worldBorder.setCenter(spawnlocation);
    worldBorder.setSize(50);


  }

  public void onDisable()
  {
    Bukkit.getServer().getScheduler().cancelTasks(this);
    LOGGER.info("NetherStarPlugin disabled");
  }
  
  //for start of event, called via command
  public void startPlugin() {
    plugin.stopMove = true;
    startcode = true;
    Bukkit.broadcastMessage(ChatColor.BOLD + "The game is starting in...");
    ID_Countdown = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        if(countdown > 0) {
          Bukkit.broadcastMessage(ChatColor.of(Color.RED) + "" + ChatColor.BOLD + countdown);
          countdown--;
        }
        else {
          plugin.stopMove = false;
          Bukkit.getScheduler().cancelTask(ID_Countdown);
          return;
        }
      }
    }, 0, 20);

    ID_Start = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        if(startcode) {
          Collection players = Bukkit.getServer().getOnlinePlayers();
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
          worldBorder.setSize(3000);
          worldBorder.setSize(400, 10800);
          bossBar = (BossBar) Bukkit.createBossBar(NSPLAYER.getDisplayName() + " has the star", BarColor.BLUE, BarStyle.SOLID);
          for(Object p : players) {
            bossBar.addPlayer((Player)p);
          }
          scoreBoard();
          Bukkit.broadcastMessage("The Nether Star Game has started! " + ChatColor.of(Color.CYAN) +  "" + ChatColor.BOLD + NSPLAYER.getName() + ChatColor.RESET + " has been given the star! Use your compass to track them");

          startcode = false;
        }
        else {
          Bukkit.getScheduler().cancelTask(ID_Start);
          return;
        }
      }
    }, 201, 20);

  ID_timer = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        if(timer == 7200) {
          Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "There are 2 HOURS remaining");
          playSoundGlobal(dragongrowl);
        }
        if(timer == 3600) {
          Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "There is 1 HOUR remaining");
          playSoundGlobal(dragongrowl);
        }
        if(timer == 1800) {
          Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "There are 30 MINUTES remaining");
          playSoundGlobal(dragongrowl);
        }
        if(timer == 300) {
          Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "There are 5 MINUTES remaining");
          playSoundGlobal(dragongrowl);
        }
        if(timer == 60) {
          Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "There is 1 MINUTE remaining");
          playSoundGlobal(dragongrowl);
        }
        if(timer < 11 && timer > 0) {
          Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + timer);
          playSoundGlobal(ding);
          
        }
        if(timer == 0) {
          winCondition = true;
          plugin.stopMove = true;
          if(NSPLAYER == null) {
            Bukkit.getWorld("world").playSound(NSLOCATION, musicstal, 100, 1);
            Bukkit.broadcastMessage("Wow, no one won, you guys suck. You are all tonights biggest loser.");
          }
          playSoundGlobal(enderdragdeath);
          Bukkit.getWorld("world").playSound(NSLOCATION, musiccat, 100, 1);
          Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + NSPLAYER.getDisplayName() + " IS THE WINNER!!!");
          Bukkit.getScheduler().cancelTask(ID_timer);
        }
          updateScoreboard();
          timer--;
      }
    }, 201, 20);

    //sanity checker
    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        plugin.sanityChecker();
      }
    }, 0, 20);

    //these 3 schedulers drop items and cancel the previous schedule when the new one is running
    //figure out better way to implement cancelation so it doesn't try to cancel multiple times in upcoming schedulers
    int ID_1 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        plugin.itemDropsFirstStage();
        plugin.playerFoodDrops();
      }
    }, 0, 3600);

    int ID_2 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        Bukkit.getScheduler().cancelTask(ID_1);
        plugin.itemDropsSecondStage();
        plugin.playerFoodDrops();
      }
    }, 24000, 3600);

    int ID_3 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        Bukkit.getScheduler().cancelTask(ID_2);
        plugin.itemDropsThirdStage();
        plugin.playerFoodDrops();
      }
    }, 48000, 3600);



  }

  public void scoreBoard() {
    scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
    timerDisplay = scoreboard.registerNewObjective("timer", Criteria.DUMMY , ChatColor.RED + "Nether Star");
    timerDisplay.setDisplaySlot(DisplaySlot.SIDEBAR);
    Score timerScore = timerDisplay.getScore(ChatColor.GREEN + "Time Left:" + ChatColor.RESET + timer);
    timerScore.setScore(2);
    LOGGER.info("Scoreboard created!");
  }

  public void updateScoreboard() {
    for (String entry : scoreboard.getEntries()) {
        scoreboard.resetScores(entry);
    }

    Score timerScore = timerDisplay.getScore(/*ChatColor.GREEN + "Time Left: " + ChatColor.RESET + */ ((timer/60)/60) + "hrs " + (timer/60%60) + "mins " + (timer%60) + "s");
    timerScore.setScore(0);

    for (Player p : Bukkit.getOnlinePlayers()) {
      p.setScoreboard(scoreboard);
    }
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
