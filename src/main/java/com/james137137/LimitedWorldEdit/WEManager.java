/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit;

import com.james137137.LimitedWorldEdit.hooks.WorldGaurdAPI;
import com.sk89q.worldedit.blocks.BaseBlock;

import java.util.HashSet;
import org.bukkit.entity.Player;

public class WEManager {

    public static BaseBlock AIR = new BaseBlock(0, 0);

    public static boolean maskContains(HashSet<RegionWrapper> mask, int x, int y, int z) {
        for (RegionWrapper region : mask) {
            if (region.isIn(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public static boolean maskContains(HashSet<RegionWrapper> mask, int x, int z) {
        for (RegionWrapper region : mask) {
            if (region.isIn(x, z)) {
                return true;
            }
        }
        return false;
    }

    public static HashSet<RegionWrapper> getMask(Player player) {
        HashSet<RegionWrapper> regions = new HashSet<>();
        if (player == null)
        {
            System.out.println("player is null");
            return null;
        }
        if (LimitedWorldEdit.api == null)
        {
            System.err.println("LimitedWorldEdit.api is null");
        }
        regions.addAll(LimitedWorldEdit.api.getRegions(player));
        return regions;
    }

    public static boolean intersects(RegionWrapper region1, RegionWrapper region2) {
        return (region1.minX <= region2.maxX) && (region1.maxX >= region2.minX) && (region1.minZ <= region2.maxZ) && (region1.maxZ >= region2.minZ);
    }

    public static boolean regionContains(RegionWrapper selection, HashSet<RegionWrapper> mask) {
        for (RegionWrapper region : mask) {
            if (intersects(region, selection)) {
                return true;
            }
        }
        return false;
    }
}
