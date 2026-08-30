package org.grey.sable_sublevel_detection.compat.cct;

import dan200.computercraft.api.ComputerCraftAPI;
import net.neoforged.fml.ModList;

public class CompatCCTweaked {
    public static void register() {
        if(ModList.get().isLoaded("computercraft")) {
            ComputerCraftAPI.registerGenericSource(new OccupancySensorPeripheral());
            ComputerCraftAPI.registerGenericSource(new SeatedOccupancySensorPeripheral());
        }
    }
}
