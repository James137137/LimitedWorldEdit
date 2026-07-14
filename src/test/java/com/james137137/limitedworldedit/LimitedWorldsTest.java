package com.james137137.limitedworldedit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LimitedWorldsTest {

    private static final UUID OWNER = UUID.fromString("0caef9ae-27da-4cf2-9b47-b0b62775d27a");
    private static final BlockVector3 INSIDE = BlockVector3.at(2, 5, 2);
    private static final BlockVector3 OUTSIDE = BlockVector3.at(20, 5, 20);

    @Test
    void masksTheDirectWorldReadsUsedByEditSessionClipboardCopies() {
        World delegate = mock(World.class);
        BlockState insideState = mock(BlockState.class);
        BaseBlock insideBlock = mock(BaseBlock.class);
        BlockState outsideState = mock(BlockState.class);
        BaseBlock outsideBlock = mock(BaseBlock.class);
        BiomeType outsideBiome = mock(BiomeType.class);
        when(delegate.getBlock(INSIDE)).thenReturn(insideState);
        when(delegate.getFullBlock(INSIDE)).thenReturn(insideBlock);

        World limitedWorld = LimitedWorlds.wrap(
                delegate, mask(), false, outsideState, outsideBlock, outsideBiome);

        assertSame(insideState, limitedWorld.getBlock(INSIDE));
        assertSame(insideBlock, limitedWorld.getFullBlock(INSIDE));
        assertSame(outsideState, limitedWorld.getBlock(OUTSIDE));
        assertSame(outsideBlock, limitedWorld.getFullBlock(OUTSIDE));
        assertSame(outsideBiome, limitedWorld.getBiome(OUTSIDE));
    }

    @Test
    void unwrapsTheDelegateAndRestoresTheOriginalOverrideState() {
        World delegate = mock(World.class);
        BlockState outsideState = mock(BlockState.class);
        BaseBlock outsideBlock = mock(BaseBlock.class);
        BiomeType outsideBiome = mock(BiomeType.class);

        World defaultWorld = LimitedWorlds.wrap(
                delegate, mask(), false, outsideState, outsideBlock, outsideBiome);
        World explicitOverride = LimitedWorlds.wrap(
                delegate, mask(), true, outsideState, outsideBlock, outsideBiome);

        assertSame(delegate, LimitedWorlds.unwrap(defaultWorld));
        assertNull(LimitedWorlds.restoreValue(defaultWorld));
        assertSame(delegate, LimitedWorlds.restoreValue(explicitOverride));
    }

    private static OwnedRegionMask mask() {
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                "plot",
                BlockVector3.at(0, 0, 0),
                BlockVector3.at(10, 10, 10));
        region.getOwners().addPlayer(OWNER);
        return OwnedRegionMask.forOwner(List.of(region), OWNER);
    }
}
