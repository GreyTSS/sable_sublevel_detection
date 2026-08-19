package org.grey.sable_sublevel_detection.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.grey.sable_sublevel_detection.SableSublevelDetection;


public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SableSublevelDetection.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {


    }

}
