package com.james137137.LimitedWorldEdit;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 *
 * @author James
 */
public class RegionWrapper {
    public final int minX;
    public final int maxX;
    public final int minY;
    public final int maxY;
    public final int minZ;
    public final int maxZ;

    public RegionWrapper(int minX, int maxX, int minZ, int maxZ) {
        this.maxX = maxX;
        this.minX = minX;
        this.maxZ = maxZ;
        this.minZ = minZ;
        this.minY = 0;
        this.maxY = 256;
    }

    public RegionWrapper(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.maxX = maxX;
        this.minX = minX;
        this.maxZ = maxZ;
        this.minZ = minZ;
        this.minY = minY;
        this.maxY = maxY;
    }

    public boolean isIn(int x, int y, int z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ && y >= this.minY && y <= this.maxY;
    }

    public boolean isIn(int x, int z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
    }
    
    
}
