/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit;

import com.james137137.mcstats.Metrics;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author James
 */
public class LimitedWorldEdit extends JavaPlugin {

    public static List<String> playerNames = new ArrayList<>();
    public static List<Double> lastRun = new ArrayList<>();
    public boolean delayOn;
    static final Logger log = Logger.getLogger("Minecraft");
    public double delay; //in secounds
    Calendar mytime = Calendar.getInstance();
    public int blockLimit;

    public static WorldGuardPlugin myWorldGuardPlugin;
    public static WorldEditPlugin worldEdit;

    @Override
    public void onEnable() {

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        getServer().getPluginManager().registerEvents(new LimitedWorldEditListener(this), this);

        String version = Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDescription().getVersion();
        log.log(Level.INFO, this.getName() + ":Version {0} enabled", version);

        myWorldGuardPlugin = getWorldGuard();
        worldEdit = getWorldEdit();

        blockLimit = worldEdit.getLocalConfiguration().maxChangeLimit;

        FileConfiguration config = getConfig();
        config.addDefault("AutoUpdate", false);
        config.addDefault("delayOn", true);
        config.addDefault("delayTimeInSecounds", 10);
        config.addDefault("DelayBasedOnLastWorldEdit", true);
        config.addDefault("NumberOfBlocksForEverySecoundOfDelay", 10000); // this has not yet been implemented

        delayOn = config.getBoolean("delayOn");
        delay = (double) config.getInt("delayTimeInSecounds");
        config.options().copyDefaults(true);
        saveConfig();

        if (config.getBoolean("AutoUpdate")) {
            UpdatePlugin();
        }
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

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    private WorldEditPlugin getWorldEdit() {
        try {
            return myWorldGuardPlugin.getWorldEdit();
        } catch (CommandException ex) {
            Logger.getLogger(LimitedWorldEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
        //String[] trimmedArgs = args;

        if (commandName.equalsIgnoreCase("DonatorsWorldEdit")) {
            try {
                sender.sendMessage("" + CanWorldEdit(sender));

            } catch (CommandException ex) {
                Logger.getLogger(LimitedWorldEdit.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }

    public boolean CanWorldEdit(CommandSender sender) throws CommandException {

        if (delayOn) {
            if (Math.abs(getTime() - getLastRun((Player) sender) + 0.00001) >= (double) delay / 3600.0) {
                lastRun.set(getPlayerID((Player) sender), getTime());
            } else {
                sender.sendMessage(ChatColor.RED + "cooldown enable of (" + delay + " Secs): please wait");
                return false;
            }
        }
        Player player = myWorldGuardPlugin.checkPlayer(sender);
        Selection sel = worldEdit.getSelection(player);
        if (sel == null) {
            sender.sendMessage("Select a region with WorldEdit first.");
            return false;
        }

        BlockVector pos1 = sel.getNativeMinimumPoint().toBlockVector();
        BlockVector pos2 = sel.getNativeMaximumPoint().toBlockVector();
        RegionManager mgr = myWorldGuardPlugin.getGlobalRegionManager().get(sel.getWorld());
        Vector pos1pt = new Vector(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ());
        Vector pos2pt = new Vector(pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
        ApplicableRegionSet pos1set = mgr.getApplicableRegions(pos1pt);
        ApplicableRegionSet pos2set = mgr.getApplicableRegions(pos2pt);

        if (sender.hasPermission("LimitedWorldEdit.unguarded")) {
            if (pos1set.size() == 0) {
                if (pos2set.size() == 0) {
                    return true;
                } else {
                    sender.sendMessage("Pos2 must outside a WorldGaurd region");
                    return false;
                }

            } else {
                if (pos2set.size() == 0) {
                    sender.sendMessage("Pos1 must outside a WorldGaurd region");
                    return true;
                } else {
                    sender.sendMessage("Both Pos1 and Pos2 must outside a WorldGaurd region");
                    return false;
                }

            }
        }

        if (pos1set.size() == 0) {
            sender.sendMessage("pos1 is not in a WorldGuard region");
            return false;
        }
        if (pos2set.size() == 0) {
            sender.sendMessage("pos2 is not in a WorldGuard region");
            return false;
        }
        String pos1Id;
        String pos2Id;
        pos1Id = ((ProtectedRegion) pos1set.iterator().next()).getId();
        pos2Id = ((ProtectedRegion) pos1set.iterator().next()).getId();

        if (pos1Id.equalsIgnoreCase(pos2Id)) {
            ProtectedRegion region = mgr.getRegion(pos1Id);
            if (region.getOwners().contains(sender.getName())) {
                return true;
            } else {
                sender.sendMessage("You are not owner of this region");
            }
        } else {
            sender.sendMessage("pos1 and pos2 are not in the same region");
            return false;
        }

        return false;

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

}
