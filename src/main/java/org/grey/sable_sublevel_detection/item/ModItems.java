package org.grey.sable_sublevel_detection.item;

import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.grey.sable_sublevel_detection.SableSublevelDetection;

public class ModItems {
    public static DeferredRegister.Items ITEMS = DeferredRegister.createItems(SableSublevelDetection.MODID);




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }


}
