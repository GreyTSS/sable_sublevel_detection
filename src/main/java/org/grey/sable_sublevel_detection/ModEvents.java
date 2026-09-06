package org.grey.sable_sublevel_detection;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.grey.sable_sublevel_detection.block.entity.custom.OccupancySensorEntity;
import org.grey.sable_sublevel_detection.block.entity.custom.SeatedOccupancySensorEntity;

import java.util.*;

import static org.grey.sable_sublevel_detection.block.custom.AbstractSensorBlock.POWERED;


@EventBusSubscriber(modid = SableSublevelDetection.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {

    //Sublevel Registry
    private static final Set<UUID> sublevelsOccupiedOld = new HashSet<>();
    private static final Set<UUID> sublevelsOccupiedNew = new HashSet<>();
    private static final Map<UUID, HashSet<UUID>> sublevelsOccupantsOld = new HashMap<>();
    private static final Map<UUID, HashSet<UUID>> sublevelsOccupantsNew = new HashMap<>();

    //Delta Occupants
    private static final Set<UUID> sublevelOccupiedRemoved = new HashSet<>();
    private static final Set<UUID> sublevelOccupiedAdded   = new HashSet<>();

    //Occupancy Sensor
    private static final HashMap<UUID, HashSet<UUID>> occupancySensorOccupantsNew = new HashMap<>();
    private static final HashMap<UUID, HashSet<UUID>> occupancySensorOccupantsOld = new HashMap<>();

    //Seated Occupancy Sensor
    private static final HashSet<UUID> seatedPlayersNew = new HashSet<>();

    public static final HashSet<UUID> seatedSublevelsNew  = new HashSet<>();
    public static final HashSet<UUID> seatedSublevelsOld  = new HashSet<>();

    private static final HashMap<UUID, HashSet<UUID>> seatedOccupancySensorOccupantsNew = new HashMap<>();
    private static final HashMap<UUID, HashSet<UUID>> seatedOccupancySensorOccupantsOld = new HashMap<>();


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
        //Sensors to be checked
        var time = System.nanoTime();
        boolean occupancySensorActive       = !OccupancySensorEntity.loadedSensors.isEmpty();
        boolean seatedOccupancySensorActive = !SeatedOccupancySensorEntity.loadedSensors.isEmpty();

        var server = event.getServer();

        if(!gracePeriods.isEmpty()) {
            var iterator = gracePeriods.entrySet().iterator();
            while(iterator.hasNext()) {
                var entry = iterator.next();
                var key = entry.getKey();

                //Re-Entered sublevels exit grace period state.
                if(sublevelsOccupiedNew.contains(key)) {
                    iterator.remove();
                    continue;
                }


                int newDuration = entry.getValue()-1;

                if(newDuration <= 0) {
                    if(occupancySensorActive) changeOccupiedState(false, key, OccupancySensorEntity.loadedSensors, server);
                    if(seatedOccupancySensorActive) changeOccupiedState(false, key, SeatedOccupancySensorEntity.loadedSensors, server);
                    iterator.remove();
                } else {
                    entry.setValue(newDuration);
                }
            }
        }




        boolean sensorsExist = (occupancySensorActive || seatedOccupancySensorActive);

        //Future sensors may not require polling.
        boolean pollingRequired = sensorsExist;

        //Sensor timer defined in config to reduce polling frequency.
        if(--tickCount > 0 || !sensorsExist) return;
        tickCount = Config.occupancyQueryTickFrequency;

        //Clear Pooled Collections
        sublevelsOccupiedNew.clear();
        sublevelsOccupantsNew.clear();
        seatedPlayersNew.clear();
        seatedSublevelsNew.clear();
        occupancySensorOccupantsNew.clear();
        seatedOccupancySensorOccupantsNew.clear();

        //Poll Players
        if(pollingRequired) {
            for(Player player : server.getPlayerList().getPlayers()) {
                SubLevelAccess sublevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel((Entity) player);

                if(sublevel == null) continue;
                //Update Sublevel Registry
                UUID uuid = sublevel.getUniqueId();
                sublevelsOccupiedNew.add(uuid);
                sublevelsOccupantsNew.computeIfAbsent(uuid, k -> new HashSet<>()).add(player.getUUID());

                //Seated Sensor state update
                if(seatedOccupancySensorActive && player.isPassenger() && !(player.getVehicle() instanceof LivingEntity)) {
                    seatedPlayersNew.add(player.getUUID());
                    seatedSublevelsNew.add(uuid);
                    seatedOccupancySensorOccupantsNew.computeIfAbsent(uuid, k -> new HashSet<>()).add(player.getUUID());

                }
            }
        }


        //Continue if unchanged
        if(!sublevelsOccupiedNew.equals(sublevelsOccupiedOld) || !seatedSublevelsNew.equals(seatedSublevelsOld)) {
            //Clear
            sublevelOccupiedAdded.clear();
            sublevelOccupiedRemoved.clear();

            //Calculate Deltas
            sublevelOccupiedRemoved.addAll(sublevelsOccupiedOld);
            sublevelOccupiedAdded.addAll(sublevelsOccupiedNew);

            sublevelOccupiedRemoved.removeAll(sublevelsOccupiedNew);
            sublevelOccupiedAdded.removeAll(sublevelsOccupiedOld);
            Set<UUID> seatedAdded = new HashSet<>();
            Set<UUID> seatedRemoved = new HashSet<>();

            if(seatedOccupancySensorActive) {
                seatedAdded.addAll(seatedSublevelsNew);
                seatedRemoved.addAll(seatedSublevelsOld);

                seatedAdded.removeAll(seatedSublevelsOld);
                seatedRemoved.removeAll(seatedSublevelsNew);
            }



            //Occupancy Sensor
            if(occupancySensorActive && !sublevelOccupiedAdded.isEmpty()) {
                Set<UUID> occupancyUpdates = new HashSet<>();
                changeOccupiedState(true, sublevelOccupiedAdded, OccupancySensorEntity.loadedSensors, server);
            }

            if(!sublevelOccupiedRemoved.isEmpty() && occupancySensorActive) {
                for (UUID uuid : sublevelOccupiedRemoved) {
                    if (OccupancySensorEntity.loadedSensors.containsKey(uuid)) {
                        gracePeriods.put(uuid, Config.occupancyGraceTickDuration);
                    }
                }
            }


            //Seated Occupancy Sensor
            if(seatedOccupancySensorActive && !seatedAdded.isEmpty()) {

                changeOccupiedState(true, seatedAdded, SeatedOccupancySensorEntity.loadedSensors, server);
            }
            if(seatedOccupancySensorActive && !seatedRemoved.isEmpty()) {
                changeOccupiedState(false, seatedRemoved, SeatedOccupancySensorEntity.loadedSensors, server);
            }

        }


        //Clear collections
        sublevelsOccupiedOld.clear();
        occupancySensorOccupantsOld.clear();
        seatedOccupancySensorOccupantsOld.clear();
        seatedSublevelsOld.clear();

        //Update Cache
        sublevelsOccupiedOld.addAll(sublevelsOccupiedNew);
        occupancySensorOccupantsOld.putAll(occupancySensorOccupantsNew);
        seatedOccupancySensorOccupantsOld.putAll(seatedOccupancySensorOccupantsNew);
        seatedSublevelsOld.addAll(seatedSublevelsNew);
        }

    private static void changeOccupiedState(boolean status, Set<UUID> occupied, Map<UUID, HashSet<PositionData>> registry, MinecraftServer server) {
        for(UUID uuid : occupied) {
            var sublevel = registry.get(uuid);
            if(sublevel == null) continue;

            for(PositionData block : sublevel) {
                var level = server.getLevel(block.globalPos().dimension());
                if (level==null) continue;
                var pos = block.globalPos().pos();
                var state = level.getBlockState(pos);
                if(state.hasProperty(POWERED)) level.setBlockAndUpdate(pos,state.setValue(POWERED, status));
            }
        }
    }

    private static void changeOccupiedState(boolean status, UUID occupied, Map<UUID,HashSet<PositionData>> registry, MinecraftServer server) {
            Set<UUID> set = new HashSet<>();
            set.add(occupied);
            changeOccupiedState(status, set, registry, server);
    }


    public static Set<UUID> getOccupants(UUID sublevel) {
        return(new HashSet<>(sublevelsOccupantsNew.get(sublevel)));
    }

    public static HashMap<UUID, HashSet<UUID>> getOccupantsMap() {
        return(new HashMap<>(sublevelsOccupantsNew));
    }
    public static Set<UUID> getSeatedOccupants(UUID sublevel) {
        return(new HashSet<>(seatedOccupancySensorOccupantsNew.get(sublevel)));
    }

    public static Map<UUID, HashSet<UUID>> getSeatedOccupantsMap() {
        return(new HashMap<>(seatedOccupancySensorOccupantsNew));
    }





}
