package com.nicholas;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class Commands implements CommandExecutor, TabExecutor {


    private NetherStar plugin;

    public Commands(NetherStar p) {
        this.plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (label.equals("top")) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (NetherStar.NSPLAYER == null || p.getWorld().getEnvironment() != Environment.NORMAL || (NetherStar.NSPLAYER.getWorld().getEnvironment() == Environment.NORMAL) && p.getLocation().distance(NetherStar.NSPLAYER.getLocation()) < 100) {
                p.sendMessage("Too Close to Nether Star Player, cannot teleport (or you are in the nether)");
                return true;
            }
            p.teleport(new Location(p.getWorld(), p.getLocation().getX(), p.getWorld().getHighestBlockYAt(p.getLocation()) + 1, p.getLocation().getZ()));
            return true;
        }
    }

    if(args.length == 1 && args[0].equalsIgnoreCase("start")) {
        plugin.startPlugin();
        return true;
       }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop");
        }
        return Collections.emptyList();
    }

}
