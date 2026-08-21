package org.grey.sable_sublevel_detection;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Path;


// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = SableSublevelDetection.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {



    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<String> CONFIG_VERSION = BUILDER.define("config-version", SableSublevelDetection.MOD_VERSION);
    private static final ModConfigSpec.IntValue OCCUPANCY_QUERY_TICK_FREQUENCY = BUILDER.defineInRange("occupancy-sensor-query-tick-frequency", 5,1,100);
    private static final ModConfigSpec.IntValue OCCUPANCY_GRACE_TICK_DURATION = BUILDER.defineInRange("occupancy-sensor-grace-tick-duration", 20,1,100);
    static final ModConfigSpec SPEC = BUILDER.build();

    public static String configVersion;
    public static int occupancyQueryTickFrequency;
    public static int occupancyGraceTickDuration;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        configVersion = CONFIG_VERSION.get();
        occupancyQueryTickFrequency = OCCUPANCY_QUERY_TICK_FREQUENCY.get();
        occupancyGraceTickDuration = OCCUPANCY_GRACE_TICK_DURATION.get();
    }


}
