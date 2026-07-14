package com.james137137.limitedworldedit;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.List;
import java.util.Objects;

final class LimitedExtent extends AbstractDelegateExtent {

    private final OwnedRegionMask mask;
    private final BlockState outsideBlockState;
    private final BaseBlock outsideFullBlock;

    LimitedExtent(OwnedRegionMask mask, Extent extent) {
        this(mask, extent, airState());
    }

    private LimitedExtent(OwnedRegionMask mask, Extent extent, BlockState outsideBlockState) {
        this(mask, extent, outsideBlockState, outsideBlockState.toBaseBlock());
    }

    LimitedExtent(OwnedRegionMask mask, Extent extent, BlockState outsideBlockState, BaseBlock outsideFullBlock) {
        super(extent);
        this.mask = Objects.requireNonNull(mask, "mask");
        this.outsideBlockState = Objects.requireNonNull(outsideBlockState, "outsideBlockState");
        this.outsideFullBlock = Objects.requireNonNull(outsideFullBlock, "outsideFullBlock");
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block)
            throws WorldEditException {
        return mask.contains(location) && super.setBlock(location, block);
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        if (!mask.contains(location.toVector().toBlockPoint())) {
            return null;
        }
        return super.createEntity(location, entity);
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        return mask.contains(position) && super.setBiome(position, biome);
    }

    @Override
    public BlockState getBlock(BlockVector3 location) {
        return mask.contains(location) ? super.getBlock(location) : outsideBlockState;
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 location) {
        return mask.contains(location) ? super.getFullBlock(location) : outsideFullBlock;
    }

    @Override
    public List<? extends Entity> getEntities() {
        return filterEntities(super.getEntities());
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return filterEntities(super.getEntities(region));
    }

    private List<Entity> filterEntities(List<? extends Entity> entities) {
        return entities.stream()
                .filter(entity -> mask.contains(entity.getLocation().toVector().toBlockPoint()))
                .map(Entity.class::cast)
                .toList();
    }

    private static BlockState airState() {
        return Objects.requireNonNull(BlockTypes.AIR).getDefaultState();
    }
}
