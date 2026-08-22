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
    private static final Map<UUID, HashSet<UUID>> sublevelSensors = new HashMap<>();
    public static final Set<UUID> occupied = new HashSet<>();
    public static final Map<UUID, HashSet<UUID>> occupants = new HashMap<>();
    //End Occupants
    private static final Map<UUID, HashSet<UUID>> newOccupants = new HashMap<>();
    private static final Set<UUID> newOccupied = new HashSet<>();

    //Delta Occupants
    private static final Set<UUID> removedSubevels = new HashSet<>();
    private static final Set<UUID> addedSubevels = new HashSet<>();

    //Grace
    private static final Map<UUID, Integer> gracePeriods = new HashMap<>();

    private static int tickCount = 0;


    /**
     * Tracks the occupancy of sublevels that contain loaded Occupancy sensors on an interval defined within
     * the config.
     *
     * Sable does not provide tracking events, so occupancy must be determined by polling the server's player list.
     * Occupied sublevels are stashed in a set, as well as a map which hold each player occupying a sublevel.
     * These values are cached and used in the next event as a comparison to determine delta occupants.
     * Newly occupied sublevels get updated to be POWERED, while newly unoccupied ones are allowed a
     * grace period before being UNPOWERED
     * */
    @SubscribeEvent
    public static void tick(ServerTickEvent.Pre event) {
        var server = event.getServer();

        //Grace Period Decrement and Reentries
        if(!gracePeriods.isEmpty()) {
            var iterator = gracePeriods.entrySet().iterator();
            while(iterator.hasNext()) {
                var entry = iterator.next();
                var key = entry.getKey();

                //Re-Entered sublevels exit grace period state.
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



        //Gate heavy operations behind config frequency, and only when sensors are loaded
        if(--tickCount > 0 || OccupancySensorEntity.loadedSensors.isEmpty()) return;

        newOccupants.clear();
        newOccupied.clear();
        tickCount = Config.occupancyQueryTickFrequency;

        var playerList = server.getPlayerList().getPlayers();

        /*Sable API is unable to provide a PlayerTrackingSublevelStart event or anything of the kind. Mod author
        stated it was unfeasible as it would be constantly firing.

        The SableCompanion check is relatively heavy, contributing to 1/3 of the entire footprint of the
        mod tick event, per testing with Spark in a single-player world with ~70 loaded sublevels each
        containing sensors.*/
        for(ServerPlayer player : playerList) {
            var sublevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel((Entity) player);
            if(sublevel==null) continue;
            var uuid = sublevel.getUniqueId();
            newOccupied.add(uuid);
            newOccupants.computeIfAbsent(uuid, k -> new HashSet<>()).add(player.getUUID());

        }

        //Gate hash operations behind a requisite that a change in worldstate has occurred
        if(!newOccupants.equals(sublevelSensors)) {
            removedSubevels.clear();
            addedSubevels.clear();

            removedSubevels.addAll(occupied);
            addedSubevels.addAll(newOccupied);

            removedSubevels.removeAll(newOccupied);
            addedSubevels.removeAll(occupied);

            //Only update affected blockstates
            if(!addedSubevels.isEmpty()) changeOccupiedState(true, addedSubevels, server);
            if(!removedSubevels.isEmpty()) {
                for(UUID uuid : removedSubevels) {
                    gracePeriods.put(uuid, Config.occupancyGraceTickDuration);
                }
            }

            //Rebuild caches
            sublevelSensors.clear();
            for (var entry : newOccupants.entrySet()) {
                sublevelSensors.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }

            occupants.clear();
            for (var entry : newOccupants.entrySet()) {
                var key = entry.getKey();
                for(var player : entry.getValue()) {
                    occupants.computeIfAbsent(key, k -> new HashSet<>()).add(player);
                }
            }

            occupied.clear();
            occupied.addAll(newOccupied);



        }




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
            Set<UUID> set = new HashSet<>();
            set.add(occupied);
            changeOccupiedState(status, set, server);
    }




}
