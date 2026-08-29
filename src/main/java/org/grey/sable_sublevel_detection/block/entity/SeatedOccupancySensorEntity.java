package org.grey.sable_sublevel_detection.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * OccupancySensorEntity contains the static field registry of all currently loaded sensors belonging to sublevels, to limit query later.
 * the sublevel ID is captured on level change to allow for sensors placed globally to operate normally when assembled into sublevels.
 */
public class SeatedOccupancySensorEntity extends AbstractSensorEntity {


    public SeatedOccupancySensorEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SEATED_OCCUPANCY_SENSOR_BE.get(), pos, blockState);
    }


}
