package org.grey.sable_sublevel_detection.compat.cct;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.grey.sable_sublevel_detection.ModEvents;
import org.grey.sable_sublevel_detection.SableSublevelDetection;
import org.grey.sable_sublevel_detection.block.entity.OccupancySensorEntity;
import java.util.UUID;

import static org.grey.sable_sublevel_detection.block.custom.AbstractSensorBlock.INVERTED;

/**
 * Exposes methods to CC:Tweaked computers when installed. Allows them to query a sensor for occupancy count, names and uuids
 * of occupying players, and set/get inversion state. Redstone can already be received through CraftOS's Redstone API.
 */
public class OccupancySensorPeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return SableSublevelDetection.MODID+":occupany_sensor";
    }


    @LuaFunction
    public int getOccupancyCount(OccupancySensorEntity sensor) {
        if(sensor.getSubLevelId() == null || ModEvents.getOccupantsMap().get(sensor.getSubLevelId()) == null) return 0;
        return ModEvents.getOccupants(sensor.getSubLevelId()).size();
    }

    @LuaFunction
    public String[] getOccupantsNames (OccupancySensorEntity sensor) {
        var level = sensor.getLevel();

        if(sensor.getSubLevelId() == null || level == null) return new String[0];

        if(ModEvents.getOccupantsMap().get(sensor.getSubLevelId()) != null && !ModEvents.getOccupantsMap().get(sensor.getSubLevelId()).isEmpty()) {
            UUID[] uuids = ModEvents.getOccupantsMap().get(sensor.getSubLevelId()).toArray(UUID[]::new);
            var len = uuids.length;
            String[] playerNames = new String[len];
            for (var i = 0; i < len; i++) {
                var player = level.getPlayerByUUID(uuids[i]);
                if (player != null) playerNames[i] = player.getName().getString();
            }
            return playerNames;
        }
        return new String[0];
    }

    @LuaFunction
    public String[] getOccupantsUUIDs (OccupancySensorEntity sensor) {
        var level = sensor.getLevel();
        if(sensor.getSubLevelId() == null || level == null) return new String[0];
        if(ModEvents.getOccupantsMap().get(sensor.getSubLevelId()) != null && !ModEvents.getOccupantsMap().get(sensor.getSubLevelId()).isEmpty()) {
            UUID[] uuids = ModEvents.getOccupantsMap().get(sensor.getSubLevelId()).toArray(UUID[]::new);
            var len = uuids.length;
            String[] uuidStrings = new String[len];
            for(var i = 0; i < len; i++) {
                uuidStrings[i] = uuids[i].toString();
            }
            return uuidStrings;
        }
        return new String[0];
    }


    @LuaFunction
    public boolean getInversionState(OccupancySensorEntity sensor) {
        var level = sensor.getLevel();
        if(sensor.getSubLevelId() == null || level == null) return false;
        var pos = sensor.getBlockPos();
        var block = level.getBlockState(pos);
        return block.getValue(BlockStateProperties.INVERTED);
    }

    @LuaFunction
    public void setInversionState(OccupancySensorEntity sensor, boolean state) {
        var level = sensor.getLevel();
        if(sensor.getSubLevelId() == null || level == null) return;
        var pos = sensor.getBlockPos();
        var block = level.getBlockState(pos);
        level.setBlockAndUpdate(pos,block.setValue(INVERTED, state));

    }


}
