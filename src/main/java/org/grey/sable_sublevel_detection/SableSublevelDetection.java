package org.grey.sable_sublevel_detection;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.grey.sable_sublevel_detection.block.ModBlocks;
import org.grey.sable_sublevel_detection.block.custom.AbstractSensorBlock;
import org.grey.sable_sublevel_detection.block.entity.ModBlockEntities;
import org.grey.sable_sublevel_detection.compat.cct.CompatCCTweaked;
import org.grey.sable_sublevel_detection.item.ModCreativeModeTabs;
import org.grey.sable_sublevel_detection.item.ModItems;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SableSublevelDetection.MODID)
public class SableSublevelDetection {
    public static final String MODID = "sable_sublevel_detection";
    public static final String MOD_VERSION = "1.1b1";
    public static final String CONFIG_VERSION = "1.0";

    private static final Logger LOGGER = LogUtils.getLogger();

    public SableSublevelDetection(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        NeoForge.EVENT_BUS.register(this);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);




        //Optional Compat:
        if (ModList.get().isLoaded("computercraft")) {
            CompatCCTweaked.register();
        }
    }




    private void commonSetup(final FMLCommonSetupEvent event) {

    }


    private static void registerSwapper(AbstractSensorBlock.Swapper swapper) {
        AbstractSensorBlock.SWAPPER.put(swapper.block(), swapper.item());
        AbstractSensorBlock.REVERSE_SWAPPER.put(swapper.item(), swapper.block());
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        registerSwapper(new AbstractSensorBlock.Swapper(ModBlocks.OCCUPANCY_SENSOR.get(), Items.ENDER_EYE));
        registerSwapper(new AbstractSensorBlock.Swapper(ModBlocks.SEATED_OCCUPANCY_SENSOR.get(), Items.MINECART));
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
