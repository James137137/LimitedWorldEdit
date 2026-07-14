package com.james137137.limitedworldedit;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.util.UUID;

final class WorldGuardRegionProvider {

    OwnedRegionMask forPlayer(World world, UUID playerId) {
        if (world == null || playerId == null) {
            return OwnedRegionMask.empty();
        }

        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(world);
        if (regionManager == null) {
            return OwnedRegionMask.empty();
        }

        return OwnedRegionMask.forOwner(regionManager.getRegions().values(), playerId);
    }
}
