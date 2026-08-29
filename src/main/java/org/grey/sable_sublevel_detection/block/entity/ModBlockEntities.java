package org.grey.sable_sublevel_detection.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.grey.sable_sublevel_detection.SableSublevelDetection;
import org.grey.sable_sublevel_detection.block.ModBlocks;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            SableSublevelDetection.MODID);


    //Occupancy Sensor Block Entity
    public static final Supplier<BlockEntityType<OccupancySensorEntity>> OCCUPANCY_SENSOR_BE = BLOCK_ENTITIES.register(
        "occupancy_sensor_be",
        () -> BlockEntityType.Builder.of(
                OccupancySensorEntity::new,
                ModBlocks.OCCUPANCY_SENSOR.get()).build(null));
    public static final Supplier<BlockEntityType<SeatedOccupancySensorEntity>> SEATED_OCCUPANCY_SENSOR_BE = BLOCK_ENTITIES.register(
            "seated_occupancy_sensor_be",
            () -> BlockEntityType.Builder.of(
                    SeatedOccupancySensorEntity::new,
                    ModBlocks.SEATED_OCCUPANCY_SENSOR.get()).build(null));



    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
