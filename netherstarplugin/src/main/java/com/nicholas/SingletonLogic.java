package com.nicholas;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import static org.bukkit.potion.PotionEffect.INFINITE_DURATION;

import java.awt.Event;
import java.security.SignedObject;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

//THIS CLASS WAS CREATED TO RUN ANY LOGIC THAT NEEDS TO GO BETWEEN CLASSES

public class SingletonLogic {

    //this code is to make a singleton class
    static SingletonLogic obj = new SingletonLogic();
    private SingletonLogic() {

    }

    public static SingletonLogic getInstance() {
      return obj;
    }

    private static ItemStack netherstaritemstack = new ItemStack(Material.NETHER_STAR);

    public boolean sanityCheckDisable = false;

    public boolean stopMove = false;

    public int slotIndex;

    //any potion effects not running through scheduler can be placed here
    public void potionEffects() {
        if(NetherStar.NSPLAYER != null) {
            //NetherStar.NSPLAYER.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, INFINITE_DURATION, 0, true));
            NetherStar.NSPLAYER.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, INFINITE_DURATION, 0, true));
            NetherStar.NSPLAYER.setMaxHealth(24);
        }
    }

    //resets all potion effects after ns player dies
    public void clearPotionEffects() {
        if(NetherStar.NSPLAYER != null) {
            //NetherStar.NSPLAYER.removePotionEffect(PotionEffectType.RESISTANCE);
            NetherStar.NSPLAYER.removePotionEffect(PotionEffectType.HASTE);
            NetherStar.NSPLAYER.setMaxHealth(20);
        }
    }

    public void playerFoodDrops() {
        Collection players = Bukkit.getServer().getOnlinePlayers();
        ArrayList<Player> realPlayers = new ArrayList<>();
        for (Object p : players) {
            if (p instanceof Player) {
                realPlayers.add((Player) p);
            }
        }
        for (Player q : realPlayers) {
            q.getInventory().addItem(new ItemStack(Material.COOKED_BEEF));
        }
    }


    //buffs for stage 1
    public void itemDropsFirstStage() {
        if(NetherStar.NSPLAYER != null) {
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.IRON_INGOT));
        }
    }

    //buffs for stage 2
    public void itemDropsSecondStage() {
        if(NetherStar.NSPLAYER != null) {
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.DIAMOND));
        }
    }
    
    //buffs for stage 3
    public void itemDropsThirdStage() {
        if(NetherStar.NSPLAYER != null) {
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.NETHERITE_INGOT));
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
        }
    }

    public void sanityChecker() {
        if(sanityCheckDisable) {return;}
        if(NetherStar.NSPLAYER == null) {return;}
        if(NetherStar.NSPLAYER.getInventory().getItem(EquipmentSlot.OFF_HAND) != null) {
            if(NetherStar.NSPLAYER.getInventory().getItem(EquipmentSlot.OFF_HAND).getType() == Material.NETHER_STAR) {
                NetherStar.NSPLAYER.sendMessage("You cannot move the nether star to your off hand");
                NetherStar.NSPLAYER.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            }
        }
        if(NetherStar.NSPLAYER.getInventory().containsAtLeast(netherstaritemstack, 2)) {
            //might be a little slow but prevents more thaan 1 netherstar
            NetherStar.NSPLAYER.sendMessage("You have at least 2 stars");
            slotIndex = NetherStar.NSPLAYER.getInventory().first(Material.NETHER_STAR);
            if (slotIndex != -1) {
                ItemStack netherStarInInventory = NetherStar.NSPLAYER.getInventory().getItem(slotIndex);
                netherStarInInventory.setAmount(1);
                NetherStar.NSPLAYER.getInventory().setItem(slotIndex, netherStarInInventory);
            }
        }
        if(NetherStar.NSPLAYER.getInventory().contains(netherstaritemstack.getType())) {return;}
        NetherStar.NSPLAYER.getInventory().addItem(netherstaritemstack);
    }

    public String dimensionchange(String d) {
        switch(d) {
            case "NORMAL":
            return "Overworld";
            case "NETHER":
            return "Nether";
            case "THE_END":
            return "The End";
            default:
            return d;
        } 
    }

    public boolean toolChecker(ItemStack item) {
        if(item == null) {return false;}
        if(item.toString().toLowerCase().contains("axe")) {return true;}
        if(item.toString().toLowerCase().contains("pickaxe")) {return true;}
        if(item.toString().toLowerCase().contains("shovel")) {return true;}
        if(item.toString().toLowerCase().contains("hoe")) {return true;}
        //if(item.toString().toLowerCase().contains("ore")) {return true;}
        //if(item.toString().toLowerCase().contains("ingot")) {return true;}
        //if(item.toString().toLowerCase().contains("raw")) {return true;}
        if(item.toString().toLowerCase().contains("cooked")) {return true;}
        if(item.toString().toLowerCase().contains("bread")) {return true;}
        if(item.toString().toLowerCase().contains("sword")) {return true;}
        //if(item.toString().toLowerCase().contains("chestplate")) {return true;}
        //if(item.toString().toLowerCase().contains("leggings")) {return true;}
        //if(item.toString().toLowerCase().contains("helmet")) {return true;}
        //if(item.toString().toLowerCase().contains("boots")) {return true;}
        return false;
    }

}
