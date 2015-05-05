/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit;

import com.james137137.LimitedWorldEdit.hooks.TownyAPI;
import com.james137137.LimitedWorldEdit.hooks.WorldGaurdAPI;
import com.james137137.mcstats.Metrics;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author James
 */
public class LimitedWorldEdit extends JavaPlugin {

    public static List<String> playerNames = new ArrayList<>();
    public static List<Double> lastRun;
    public boolean delayOn;
    static final Logger log = Logger.getLogger("Minecraft");
    public double delay; //in secounds
    Calendar mytime = Calendar.getInstance();
    public int blockLimit;
    TownyAPI townyAPI = null;
    WorldGaurdAPI worldGaurdAPI = null;
    List<String> worldEditCommands;
    List<String> worldGaurdCommands;
    String[] worldEditCommandst = {"set", "replace", "walls","undo","redo"};
    String[] worldGaurdCommandst = {"region define","region redefine"};

    public static WorldEditPlugin worldEdit;

    @Override
    public void onEnable() {
        worldEditCommands = Arrays.asList(worldEditCommandst);  
        worldGaurdCommands = Arrays.asList(worldGaurdCommandst);  
        lastRun = new ArrayList<>();
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        String version = Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDescription().getVersion();

        worldEdit = getWorldEdit();

        blockLimit = worldEdit.getLocalConfiguration().maxChangeLimit;

        FileConfiguration config = getConfig();
        config.addDefault("AutoUpdate", false);
        config.addDefault("UseWorldGaurdAPI", true);
        config.addDefault("UseTownyAPI", false);
        config.addDefault("delayOn", true);
        config.addDefault("delayTimeInSecounds", 10);
        config.addDefault("DefualtLimit", 100);
        config.addDefault("worldEditCommands", worldEditCommands);
        config.addDefault("worldGaurdCommands", worldGaurdCommands);
        
        //config.addDefault("DelayBasedOnLastWorldEdit", true);
        //config.addDefault("NumberOfBlocksForEverySecoundOfDelay", 10000); // this has not yet been implemented

        if (config.getBoolean("UseWorldGaurdAPI")) {
            worldGaurdAPI = new WorldGaurdAPI(this);
        }

        if (config.getBoolean("UseTownyAPI")) {
            townyAPI = new TownyAPI(this);
        }
        delayOn = config.getBoolean("delayOn");
        delay = (double) config.getInt("delayTimeInSecounds");
        config.options().copyDefaults(true);
        saveConfig();

        if (config.getBoolean("AutoUpdate")) {
            UpdatePlugin();
        }
        
        getServer().getPluginManager().registerEvents(new LimitedWorldEditListener(this), this);

        log.log(Level.INFO, this.getName() + ":Version {0} enabled", version);
    }

    public boolean UpdatePlugin() {
        Updater updater = new net.gravitydevelopment.updater.Updater(this, 54713, this.getFile(), net.gravitydevelopment.updater.Updater.UpdateType.DEFAULT, true);
        if (updater.getResult() == Updater.UpdateResult.SUCCESS) {
            this.getLogger().info("updated to " + updater.getLatestName());
            this.getLogger().info("Full plugin reload is required");
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        log.info("LimitedWorldEdit: disabled");
    }

    private WorldEditPlugin getWorldEdit() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldEdit");
        return (WorldEditPlugin) plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
        //String[] trimmedArgs = args;

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is for players only");
            return true;
        }

        if (commandName.equalsIgnoreCase("DonatorsWorldEdit")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("limit")) {
                    if (args.length == 1) {
                        int limit = getLimit((Player) sender);
                        sender.sendMessage(ChatColor.GREEN + "Your limit is " + ChatColor.GOLD + limit);
                        return true;
                    }
                }
            }
            try {
                sender.sendMessage("" + CanWorldEdit((Player) sender));

            } catch (CommandException ex) {
                Logger.getLogger(LimitedWorldEdit.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }

    public boolean CanWorldEdit(Player sender) throws CommandException {

        boolean result = true;
        if (delayOn) {
            if (Math.abs(getTime() - getLastRun(sender) + 0.00001) >= (double) delay / 3600.0) {
                lastRun.set(getPlayerID((Player) sender), getTime());
            } else {
                sender.sendMessage(ChatColor.RED + "cooldown enable of (" + delay + " Secs): please wait");
                return false;
            }
        }
        Selection sel = worldEdit.getSelection((Player) sender);
        if (sel == null) {
            sender.sendMessage("Select a region with WorldEdit first.");
            return false;
        }
        int limit = getLimit(sender);
        if (sel.getArea() * sel.getHeight() >= limit && limit > 0) {
            sender.sendMessage(ChatColor.RED + "The volume (number of blocks) exceeds your limit of " + limit);
            return false;
        }
        BlockVector pos1 = sel.getNativeMinimumPoint().toBlockVector();
        BlockVector pos2 = sel.getNativeMaximumPoint().toBlockVector();

        if (worldGaurdAPI != null) {
            result = worldGaurdAPI.CanBuildHere(sender, sel, pos1, pos2);
            if (result) {
                return result;
            }
        }

        if (townyAPI != null) {
            result = (townyAPI.CanBuildHere(sender, pos1, sel.getWorld()) && townyAPI.CanBuildHere(sender, pos2, sel.getWorld()));
            if (result) {
                return result;
            }
        }

        return result;

    }

    public double getTime() {
        mytime = Calendar.getInstance();
        return (double) mytime.get(Calendar.HOUR_OF_DAY) + (double) mytime.get(Calendar.MINUTE) / 60.0 + (double) mytime.get(Calendar.SECOND) / 3600.0;

    }

    public int getPlayerID(Player player) {
        String playerName = player.getName();
        for (int i = 0; i < playerNames.size(); i++) {
            if (playerNames.get(i).equalsIgnoreCase(playerName)) {
                return i;
            }
        }

        return -1;
    }

    public void SetPlayerID(Player player) {
        int playerid = getPlayerID(player);

        if (playerid == -1) {
            playerNames.add(player.getName());
            lastRun.add(getTime() - (delay / 3600.0));
        }
    }

    public double getLastRun(Player player) {
        int playerid = getPlayerID(player);
        if (playerid >= 0) {
            return lastRun.get(playerid);
        }
        return -1.0;
    }

    private int getLimit(Player player) {
        int limit = worldEdit.getSession(player).getBlockChangeLimit();
        if (limit < 0) {
            return limit;
        }
        Set<PermissionAttachmentInfo> effectivePermissions = player.getEffectivePermissions();
        for (PermissionAttachmentInfo permissionAttachmentInfo : effectivePermissions) {
            if (permissionAttachmentInfo.getPermission().startsWith("limitedworldedit.limit.")) {

                try {
                    int ammount = Integer.parseInt(permissionAttachmentInfo.getPermission().split(".limit.")[1]);
                    if (limit < ammount) {
                        limit = ammount;
                    }
                } catch (NumberFormatException e) {
                }

            }
        }
        if (this.getConfig().getInt("DefualtLimit") > limit)
        {
            limit = this.getConfig().getInt("DefualtLimit");
        }
        return limit;
    }

}
