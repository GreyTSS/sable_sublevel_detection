package org.grey.sable_sublevel_detection.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.grey.sable_sublevel_detection.SableSublevelDetection;
import org.grey.sable_sublevel_detection.block.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SableSublevelDetection.MODID, exFileHelper);
    }




    @Override
    protected void registerStatesAndModels() {
        allSidesBlockState(ModBlocks.OCCUPANCY_SENSOR);


    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void allSidesBlockState(DeferredBlock<?> deferredBlock) {
        String name = deferredBlock.getId().getPath();

        ResourceLocation bottomPowered = ResourceLocation.fromNamespaceAndPath(SableSublevelDetection.MODID, "block/"+name+"_bottom_powered");
        ResourceLocation bottomUnpowered = ResourceLocation.fromNamespaceAndPath(SableSublevelDetection.MODID, "block/"+name+"_bottom_unpowered");
        ResourceLocation topPowered = ResourceLocation.fromNamespaceAndPath(SableSublevelDetection.MODID, "block/"+name+"_top_powered");
        ResourceLocation topUnpowered = ResourceLocation.fromNamespaceAndPath(SableSublevelDetection.MODID, "block/"+name+"_top_unpowered");

        ResourceLocation sidePowered = ResourceLocation.fromNamespaceAndPath(SableSublevelDetection.MODID, "block/"+name+"_side_powered");
        ResourceLocation sideUnpowered = ResourceLocation.fromNamespaceAndPath(SableSublevelDetection.MODID, "block/"+name+"_side_unpowered");

        ModelFile modelPowered = models().cubeBottomTop(name+"_powered", sidePowered, bottomPowered, topPowered);
        ModelFile modelUnPowered = models().cubeBottomTop(name, sideUnpowered, bottomUnpowered, topUnpowered);

        getVariantBuilder(deferredBlock.get()).forAllStates(state -> {
            boolean isPowered = state.getValue(BlockStateProperties.POWERED);
            return ConfiguredModel.builder()
                    .modelFile(isPowered ? modelPowered : modelUnPowered)
                    .build();
        });
        simpleBlockItem(deferredBlock.get(), modelUnPowered);
    }


}
