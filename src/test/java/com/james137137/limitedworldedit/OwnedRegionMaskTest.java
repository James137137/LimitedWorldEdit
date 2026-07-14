package com.james137137.limitedworldedit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OwnedRegionMaskTest {

    private static final UUID OWNER = UUID.fromString("0caef9ae-27da-4cf2-9b47-b0b62775d27a");

    @Test
    void includesOwnedCuboidWithinItsVerticalBounds() {
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                "plot",
                BlockVector3.at(0, -20, 0),
                BlockVector3.at(10, 30, 10));
        region.getOwners().addPlayer(OWNER);

        OwnedRegionMask mask = OwnedRegionMask.forOwner(List.of(region), OWNER);

        assertEquals(1, mask.size());
        assertTrue(mask.contains(BlockVector3.at(0, -20, 0)));
        assertTrue(mask.contains(BlockVector3.at(10, 30, 10)));
        assertFalse(mask.contains(BlockVector3.at(5, -21, 5)));
        assertFalse(mask.contains(BlockVector3.at(11, 10, 5)));
    }

    @Test
    void preservesPolygonShapeInsteadOfUsingItsBoundingBox() {
        ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(
                "triangle",
                List.of(BlockVector2.at(0, 0), BlockVector2.at(10, 0), BlockVector2.at(0, 10)),
                0,
                20);
        region.getOwners().addPlayer(OWNER);

        OwnedRegionMask mask = OwnedRegionMask.forOwner(List.of(region), OWNER);

        assertTrue(mask.contains(BlockVector3.at(2, 10, 2)));
        assertFalse(mask.contains(BlockVector3.at(9, 10, 9)), "point is inside the bounding box but outside the triangle");
        assertFalse(mask.contains(BlockVector3.at(2, 21, 2)));
    }

    @Test
    void excludesUnownedAndNonPhysicalRegions() {
        ProtectedCuboidRegion unowned = new ProtectedCuboidRegion(
                "other-plot",
                BlockVector3.at(0, 0, 0),
                BlockVector3.at(10, 10, 10));
        unowned.getOwners().addPlayer(UUID.randomUUID());

        GlobalProtectedRegion global = new GlobalProtectedRegion("__global__");
        global.getOwners().addPlayer(OWNER);

        OwnedRegionMask mask = OwnedRegionMask.forOwner(List.of(unowned, global), OWNER);

        assertEquals(0, mask.size());
        assertFalse(mask.contains(BlockVector3.at(5, 5, 5)));
    }
}
