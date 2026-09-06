package org.grey.sable_sublevel_detection.block.entity.custom;


import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.grey.sable_sublevel_detection.ModEvents;
import org.grey.sable_sublevel_detection.PositionData;
import org.grey.sable_sublevel_detection.block.entity.ModBlockEntities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;


/**
 * OccupancySensorEntity contains the static field registry of all currently loaded sensors belonging to sublevels, to limit query later.
 * the sublevel ID is captured on level change to allow for sensors placed globally to operate normally when assembled into sublevels.
 */
public class OccupancySensorEntity extends AbstractSensorEntity {

    public static Map<UUID, HashSet<PositionData>> loadedSensors = new HashMap<>();
    public OccupancySensorEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.OCCUPANCY_SENSOR_BE.get(), pos, blockState);
    }

    @Override
    public void checkStateOnLoad() {
        var occupied = ModEvents.getOccupantsMap().containsKey(getSubLevelId());
        this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(BlockStateProperties.POWERED, occupied));
    }

    @Override
    public Map<UUID,HashSet<PositionData>> getRegistry() {return OccupancySensorEntity.loadedSensors;}

}
