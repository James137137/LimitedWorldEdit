/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit.hooks;

import com.james137137.LimitedWorldEdit.LimitedWorldEdit;
import com.james137137.LimitedWorldEdit.RegionWrapper;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author James
 */
public class WorldGaurdAPI implements API {

    private static WorldGuardPlugin myWorldGuardPlugin;

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

    @Override
    public List<RegionWrapper> getRegions(Player player) {
        List<RegionWrapper> output = new ArrayList<>();
        RegionManager mgr = myWorldGuardPlugin.getRegionManager(player.getWorld());
        Collection<ProtectedRegion> values = mgr.getRegions().values();
        for (ProtectedRegion value : values) {
            if (value.getOwners().contains(player.getUniqueId())) {
                BlockVector minimumPoint = value.getMinimumPoint();
                BlockVector maximumPoint = value.getMaximumPoint();
                RegionWrapper regionWrapper = new RegionWrapper(minimumPoint.getBlockX(), maximumPoint.getBlockX(), minimumPoint.getBlockY(), maximumPoint.getBlockY(),
                        minimumPoint.getBlockZ(), maximumPoint.getBlockZ());
                output.add(regionWrapper);
            }
        }

        return output;
    }
}
