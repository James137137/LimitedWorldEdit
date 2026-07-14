package com.james137137.limitedworldedit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LimitedExtentTest {

    private static final UUID OWNER = UUID.fromString("0caef9ae-27da-4cf2-9b47-b0b62775d27a");
    private static final BlockVector3 INSIDE = BlockVector3.at(2, 5, 2);
    private static final BlockVector3 OUTSIDE = BlockVector3.at(20, 5, 20);

    @Test
    void permitsMutationsInsideAndRejectsThemOutside() throws WorldEditException {
        RecordingExtent delegate = new RecordingExtent();
        LimitedExtent extent = testExtent(delegate);
        BlockState stone = mock(BlockState.class);
        BiomeType plains = mock(BiomeType.class);
        BaseEntity pig = mock(BaseEntity.class);

        assertTrue(extent.setBlock(INSIDE, stone));
        assertFalse(extent.setBlock(OUTSIDE, stone));
        assertEquals(1, delegate.blockWrites);

        assertTrue(extent.setBiome(INSIDE, plains));
        assertFalse(extent.setBiome(OUTSIDE, plains));
        assertEquals(1, delegate.biomeWrites);

        extent.createEntity(new Location(delegate, INSIDE.x(), INSIDE.y(), INSIDE.z()), pig);
        extent.createEntity(new Location(delegate, OUTSIDE.x(), OUTSIDE.y(), OUTSIDE.z()), pig);
        assertEquals(1, delegate.entityCreates);
    }

    @Test
    void hidesBothBlockReadFormsOutsideTheMask() {
        RecordingExtent delegate = new RecordingExtent();
        BlockState outsideState = mock(BlockState.class);
        BaseBlock outsideBlock = mock(BaseBlock.class);
        BiomeType outsideBiome = mock(BiomeType.class);
        LimitedExtent extent = new LimitedExtent(mask(), delegate, outsideState, outsideBlock, outsideBiome);

        assertSame(delegate.blockState, extent.getBlock(INSIDE));
        assertSame(outsideState, extent.getBlock(OUTSIDE));
        assertSame(delegate.fullBlock, extent.getFullBlock(INSIDE));
        assertSame(outsideBlock, extent.getFullBlock(OUTSIDE));
        assertSame(outsideBiome, extent.getBiome(OUTSIDE));
    }

    @Test
    void exposesOnlyEntitiesInsideTheMask() {
        RecordingExtent delegate = new RecordingExtent();
        delegate.entities.add(new StubEntity(delegate, INSIDE));
        delegate.entities.add(new StubEntity(delegate, OUTSIDE));
        LimitedExtent extent = testExtent(delegate);

        assertEquals(1, extent.getEntities().size());
    }

    private static OwnedRegionMask mask() {
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                "plot",
                BlockVector3.at(0, 0, 0),
                BlockVector3.at(10, 10, 10));
        region.getOwners().addPlayer(OWNER);
        return OwnedRegionMask.forOwner(List.of(region), OWNER);
    }

    private static LimitedExtent testExtent(Extent delegate) {
        return new LimitedExtent(
                mask(), delegate, mock(BlockState.class), mock(BaseBlock.class), mock(BiomeType.class));
    }

    private static final class RecordingExtent extends NullExtent {

        private final List<Entity> entities = new ArrayList<>();
        private final BlockState blockState = mock(BlockState.class);
        private final BaseBlock fullBlock = mock(BaseBlock.class);
        private int blockWrites;
        private int biomeWrites;
        private int entityCreates;

        @Override
        public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block)
                throws WorldEditException {
            blockWrites++;
            return true;
        }

        @Override
        public boolean setBiome(BlockVector3 position, BiomeType biome) {
            biomeWrites++;
            return true;
        }

        @Override
        public Entity createEntity(Location location, BaseEntity entity) {
            entityCreates++;
            return null;
        }

        @Override
        public BlockState getBlock(BlockVector3 position) {
            return blockState;
        }

        @Override
        public BaseBlock getFullBlock(BlockVector3 position) {
            return fullBlock;
        }

        @Override
        public List<Entity> getEntities() {
            return entities;
        }

        @Override
        public List<Entity> getEntities(Region region) {
            return entities;
        }
    }

    private static final class StubEntity implements Entity {

        private final Extent extent;
        private Location location;

        private StubEntity(Extent extent, BlockVector3 position) {
            this.extent = extent;
            this.location = new Location(extent, position.x(), position.y(), position.z());
        }

        @Override
        public BaseEntity getState() {
            return null;
        }

        @Override
        public boolean remove() {
            return true;
        }

        @Override
        public <T> T getFacet(Class<? extends T> cls) {
            return null;
        }

        @Override
        public Location getLocation() {
            return location;
        }

        @Override
        public boolean setLocation(Location location) {
            this.location = location;
            return true;
        }

        @Override
        public Extent getExtent() {
            return extent;
        }
    }
}
