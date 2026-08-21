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

    //Current
    private static final Map<UUID, HashSet<UUID>> occupants = new HashMap<>();
    private static final Set<UUID> occupied = new HashSet<>();

    //Delta
    private static final Map<UUID, HashSet<UUID>> newOccupants = new HashMap<>();
    private static final Set<UUID> newOccupied = new HashSet<>();



    private static final int queryTickFrequency = Config.occupancyQueryTickFrequency;
    private static int tickCount = queryTickFrequency;


    /*

     */
    @SubscribeEvent
    public static void tick(ServerTickEvent.Pre event) {
        if(--tickCount > 0 || OccupancySensorEntity.loadedSensors.isEmpty()) return;
        var start = System.nanoTime();
        newOccupants.clear();
        newOccupied.clear();
        tickCount = queryTickFrequency;
        var playerList = event.getServer().getPlayerList().getPlayers();
        for(ServerPlayer player : playerList) {
            var sublevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel((Entity) player);
            if(sublevel==null) continue;
            var uuid = sublevel.getUniqueId();
            newOccupied.add(uuid);
            newOccupants.computeIfAbsent(uuid, k -> new HashSet<>()).add(player.getUUID());
        }

        if(!newOccupants.equals(occupants)) {

            Set<UUID> removedSubevels = new HashSet<>(occupied);
            Set<UUID> addedSubevels = new HashSet<>(newOccupied);


            removedSubevels.removeAll(newOccupied);
            addedSubevels.removeAll(occupied);

            if(!removedSubevels.isEmpty()) changeOccupiedState(false, removedSubevels, event.getServer());
            if(!addedSubevels.isEmpty()) changeOccupiedState(true, addedSubevels, event.getServer());

            occupants.clear();
            for (var entry : newOccupants.entrySet()) {
                occupants.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }

            occupied.clear();
            occupied.addAll(newOccupied);
        }
        System.out.println("Sensors Loaded: "+OccupancySensorEntity.loadedSensors.size());
        System.out.println("Sensor Tick Event Time: "+ (System.nanoTime() - start)+"ns!" );



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




}
