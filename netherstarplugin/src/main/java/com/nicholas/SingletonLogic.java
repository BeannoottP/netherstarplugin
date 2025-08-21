package com.nicholas;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import static org.bukkit.potion.PotionEffect.INFINITE_DURATION;

import org.bukkit.Material;
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

    //any potion effects not running through scheduler can be placed here
    public void potionEffects() {
        if(NetherStar.NSPLAYER != null) {
            NetherStar.NSPLAYER.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, INFINITE_DURATION, 0, true));
            NetherStar.NSPLAYER.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, INFINITE_DURATION, 0, true));
            NetherStar.NSPLAYER.setMaxHealth(24);
        }
    }

    //resets all potion effects after ns player dies
    public void clearPotionEffects() {
        if(NetherStar.NSPLAYER != null) {
            NetherStar.NSPLAYER.removePotionEffect(PotionEffectType.RESISTANCE);
            NetherStar.NSPLAYER.removePotionEffect(PotionEffectType.HASTE);
            NetherStar.NSPLAYER.setMaxHealth(20);
        }
    }

    //buffs for stage 1
    public void itemDropsFirstStage() {
        if(NetherStar.NSPLAYER != null) {
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.IRON_INGOT));
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.COOKED_BEEF));
        }
    }

    //buffs for stage 2
    public void itemDropsSecondStage() {
        if(NetherStar.NSPLAYER != null) {
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.DIAMOND));
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
        }
    }
    
    //buffs for stage 3
    public void itemDropsThirdStage() {
        if(NetherStar.NSPLAYER != null) {
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.NETHERITE_INGOT));
            NetherStar.NSPLAYER.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
        }
    }
}
