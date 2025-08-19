package com.nicholas;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

@SuppressWarnings("unused")
public class Commands implements CommandExecutor, TabExecutor {


    private NetherStar plugin;

    public Commands(NetherStar p) {
        this.plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if(args.length == 1 && args[1].equalsIgnoreCase("start")) {
        plugin.startPlugin();
        return true;
       }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onTabComplete'");
    }

}
