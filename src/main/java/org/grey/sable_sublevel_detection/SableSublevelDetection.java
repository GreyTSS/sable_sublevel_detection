package org.grey.sable_sublevel_detection;

import com.mojang.logging.LogUtils;
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
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.grey.sable_sublevel_detection.block.ModBlocks;
import org.grey.sable_sublevel_detection.block.entity.ModBlockEntities;
import org.grey.sable_sublevel_detection.compat.cct.CompatCCTweaked;
import org.grey.sable_sublevel_detection.item.ModCreativeModeTabs;
import org.grey.sable_sublevel_detection.item.ModItems;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SableSublevelDetection.MODID)
public class SableSublevelDetection {
    public static final String MODID = "sable_sublevel_detection";
    public static final String MOD_VERSION = "0.1";

    private static final Logger LOGGER = LogUtils.getLogger();

    public SableSublevelDetection(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        NeoForge.EVENT_BUS.register(this);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        ModBlockEntities.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);


        //Optional Compat:
        if (ModList.get().isLoaded("computercraft")) {
            CompatCCTweaked.register();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
