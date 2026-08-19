package org.grey.sable_sublevel_detection;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.impl.SableCompanionUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.grey.sable_sublevel_detection.block.entity.OccupancySensorEntity;

import java.util.*;

import static org.grey.sable_sublevel_detection.block.custom.OccupancySensorBlock.POWERED;


@EventBusSubscriber(modid = SableSublevelDetection.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {

    private static Map<UUID, HashSet<UUID>> occupants = new HashMap<>();
    private static Set<UUID> occupied = new HashSet<>();


    private static final int queryTickFrequency = Config.occupancyQueryTickFrequency;
    private static int tickCount = queryTickFrequency;

    /*

     */
    @SubscribeEvent
    public static void tick(ServerTickEvent.Pre event) {
        if(--tickCount > 0) return;
        tickCount = queryTickFrequency;
        var playerList = event.getServer().getPlayerList().getPlayers();
        Map<UUID, HashSet<UUID>> newOccupants = new HashMap<>();
        Set<UUID> newOccupied = new HashSet<>();
        for(ServerPlayer player : playerList) {
            var sublevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel((Entity) player);
            if(sublevel==null) continue;
            var uuid = sublevel.getUniqueId();
            newOccupied.add(uuid);
            newOccupants.computeIfAbsent(uuid, k -> new HashSet<>()).add(player.getUUID());
        }

        if(!newOccupants.equals(occupants)) {
            changeOccupiedState(false, occupied);

            occupants = newOccupants;
            occupied = newOccupied;
            changeOccupiedState(true, occupied);



        }





    }

    private static void changeOccupiedState(boolean status, Set<UUID> occupied) {
        for(UUID uuid : occupied) {
            var sublevel = OccupancySensorEntity.loadedSensors.get(uuid);
            if(sublevel == null) continue;
            var blocks = sublevel.stream().toList();
            for(OccupancySensorEntity.PositionData block : blocks) {
                var level = block.level();
                var pos = block.globalPos().pos();
                var state = level.getBlockState(block.globalPos().pos());

                level.setBlockAndUpdate(pos,state.setValue(POWERED, status));
                level.updateNeighborsAt(pos,state.getBlock());

            }
        }
    }




}
