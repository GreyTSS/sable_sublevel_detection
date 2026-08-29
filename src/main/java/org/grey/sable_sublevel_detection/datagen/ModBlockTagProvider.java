package org.grey.sable_sublevel_detection.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.grey.sable_sublevel_detection.SableSublevelDetection;
import org.grey.sable_sublevel_detection.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, SableSublevelDetection.MODID, existingFileHelper);
    }

    //Add Create Wrench Compat
    private static final TagKey<Block> WRENCH_PICKUP = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("create", "wrench_pickup")
    );

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.OCCUPANCY_SENSOR.get())
                .add(ModBlocks.SEATED_OCCUPANCY_SENSOR.get());

        tag(WRENCH_PICKUP)
                .add(ModBlocks.OCCUPANCY_SENSOR.get())
                .add(ModBlocks.SEATED_OCCUPANCY_SENSOR.get());
    }
}
