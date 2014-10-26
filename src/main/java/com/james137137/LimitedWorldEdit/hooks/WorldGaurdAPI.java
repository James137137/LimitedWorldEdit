/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit.hooks;

import com.james137137.LimitedWorldEdit.LimitedWorldEdit;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author James
 */
public class WorldGaurdAPI {

    private final WorldGuardPlugin myWorldGuardPlugin;

    public WorldGaurdAPI(LimitedWorldEdit aThis) {
        myWorldGuardPlugin = getWorldGuard(aThis);
    }

    private WorldGuardPlugin getWorldGuard(LimitedWorldEdit aThis) {
        Plugin plugin = aThis.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    public boolean CanBuildHere(Player sender, Selection sel,BlockVector pos1,BlockVector pos2) {
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
            if (region.getOwners() != null && region.getOwners().contains(sender.getUniqueId())) {
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
}
