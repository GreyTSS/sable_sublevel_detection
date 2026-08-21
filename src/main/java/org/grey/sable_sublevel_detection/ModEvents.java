package org.grey.sable_sublevel_detection;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.grey.sable_sublevel_detection.block.entity.OccupancySensorEntity;

import java.util.*;

import static org.grey.sable_sublevel_detection.block.custom.OccupancySensorBlock.POWERED;


@EventBusSubscriber(modid = SableSublevelDetection.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {

    //Current Occupants
    private static final Map<UUID, HashSet<UUID>> occupants = new HashMap<>();
    public static final Set<UUID> occupied = new HashSet<>();

    //End Occupants
    private static final Map<UUID, HashSet<UUID>> newOccupants = new HashMap<>();
    private static final Set<UUID> newOccupied = new HashSet<>();

    //Delta Occupants
    private static final Set<UUID> removedSubevels = new HashSet<>();
    private static final Set<UUID> addedSubevels = new HashSet<>();

    //Grace
    private static final Map<UUID, Integer> gracePeriods = new HashMap<>();


    private static int tickCount = 0;


    /*

     */
    @SubscribeEvent
    public static void tick(ServerTickEvent.Pre event) {
        var server = event.getServer();
        if(!gracePeriods.isEmpty()) {
            var iterator = gracePeriods.entrySet().iterator();
            while(iterator.hasNext()) {
                var entry = iterator.next();
                var key = entry.getKey();

                if(newOccupied.contains(key)) {
                    iterator.remove();
                    continue;
                }


                int newDuration = entry.getValue()-1;

                if(newDuration <= 0) {
                    changeOccupiedState(false, key, server);
                    iterator.remove();
                } else {
                    entry.setValue(newDuration);
                }
            }
        }




        if(--tickCount > 0 || OccupancySensorEntity.loadedSensors.isEmpty()) return;
        var start = System.nanoTime();
        newOccupants.clear();
        newOccupied.clear();
        tickCount = Config.occupancyQueryTickFrequency;

        var playerList = server.getPlayerList().getPlayers();
        for(ServerPlayer player : playerList) {
            var sublevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel((Entity) player);
            if(sublevel==null) continue;
            var uuid = sublevel.getUniqueId();
            newOccupied.add(uuid);
            newOccupants.computeIfAbsent(uuid, k -> new HashSet<>()).add(player.getUUID());
        }

        if(!newOccupants.equals(occupants)) {
            removedSubevels.clear();
            addedSubevels.clear();

            removedSubevels.addAll(occupied);
            addedSubevels.addAll(newOccupied);

            removedSubevels.removeAll(newOccupied);
            addedSubevels.removeAll(occupied);


            if(!addedSubevels.isEmpty()) changeOccupiedState(true, addedSubevels, server);

            if(!removedSubevels.isEmpty()) {
                System.out.println("CONFIG GRACE: " + Config.occupancyGraceTickDuration);
                System.out.println("CACHED GRACE: " + Config.occupancyGraceTickDuration);
                for(UUID uuid : removedSubevels) {

                    gracePeriods.put(uuid, Config.occupancyGraceTickDuration);
                }
            }

            occupants.clear();
            for (var entry : newOccupants.entrySet()) {
                occupants.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }

            occupied.clear();
            occupied.addAll(newOccupied);



        }
        System.out.println("Sensors Loaded: "+OccupancySensorEntity.loadedSensors.size());
        System.out.println("Sensor Tick Event Time: "+ (System.nanoTime() - start)+"ns!" );
        System.out.println("Grace Periods: " + gracePeriods.size());



    }

    private static void changeOccupiedState(boolean status, Set<UUID> occupied, MinecraftServer server) {
        for(UUID uuid : occupied) {
            var sublevel = OccupancySensorEntity.loadedSensors.get(uuid);
            if(sublevel == null) continue;

            for(OccupancySensorEntity.PositionData block : sublevel) {
                var level = server.getLevel(block.globalPos().dimension());
                if (level==null) continue;
                var pos = block.globalPos().pos();
                var state = level.getBlockState(pos);
                if(state.hasProperty(POWERED)) level.setBlockAndUpdate(pos,state.setValue(POWERED, status));
            }
        }
    }

    private static void changeOccupiedState(boolean status, UUID occupied, MinecraftServer server) {

            var sublevel = OccupancySensorEntity.loadedSensors.get(occupied);
            if(sublevel == null) return;

            for(OccupancySensorEntity.PositionData block : sublevel) {
                var level = server.getLevel(block.globalPos().dimension());
                if (level==null) continue;
                var pos = block.globalPos().pos();
                var state = level.getBlockState(pos);
                if(state.hasProperty(POWERED)) level.setBlockAndUpdate(pos,state.setValue(POWERED, status));

        }
    }




}
