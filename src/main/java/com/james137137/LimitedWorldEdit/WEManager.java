/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit;

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
        regions.addAll(LimitedWorldEdit.api.getRegions(player));
        return regions;
    }

    
}
