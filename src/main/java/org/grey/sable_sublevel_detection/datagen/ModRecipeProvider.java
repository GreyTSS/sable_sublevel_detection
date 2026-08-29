package org.grey.sable_sublevel_detection.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.grey.sable_sublevel_detection.block.ModBlocks;

import java.util.concurrent.CompletableFuture;


public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.OCCUPANCY_SENSOR.get())
                .pattern("IEI")
                .pattern("COC")
                .pattern("CRC")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COBBLESTONE)
                .define('E', Items.ENDER_EYE)
                .define('O', Items.OBSERVER)
                .define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_observer", has(Items.OBSERVER)).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SEATED_OCCUPANCY_SENSOR.get())
                .pattern("IVI")
                .pattern("COC")
                .pattern("CRC")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COBBLESTONE)
                .define('V', Items.MINECART)
                .define('O', Items.OBSERVER)
                .define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_observer", has(Items.OBSERVER)).save(recipeOutput);

    }
}
