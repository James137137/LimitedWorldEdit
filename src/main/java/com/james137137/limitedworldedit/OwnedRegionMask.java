package com.james137137.limitedworldedit;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

final class OwnedRegionMask {

    private static final OwnedRegionMask EMPTY = new OwnedRegionMask(List.of());

    private final List<ProtectedRegion> regions;

    private OwnedRegionMask(List<ProtectedRegion> regions) {
        this.regions = regions;
    }

    static OwnedRegionMask empty() {
        return EMPTY;
    }

    static OwnedRegionMask forOwner(Collection<ProtectedRegion> regions, UUID ownerId) {
        Objects.requireNonNull(regions, "regions");
        Objects.requireNonNull(ownerId, "ownerId");

        List<ProtectedRegion> ownedRegions = regions.stream()
                .filter(ProtectedRegion::isPhysicalArea)
                .filter(region -> region.getOwners().contains(ownerId))
                .toList();
        return ownedRegions.isEmpty() ? EMPTY : new OwnedRegionMask(ownedRegions);
    }

    boolean contains(BlockVector3 position) {
        Objects.requireNonNull(position, "position");
        return regions.stream().anyMatch(region -> region.contains(position));
    }

    int size() {
        return regions.size();
    }
}
