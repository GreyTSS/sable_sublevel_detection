package org.grey.sable_sublevel_detection.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.grey.sable_sublevel_detection.SableSublevelDetection;
import org.grey.sable_sublevel_detection.block.entity.custom.OccupancySensorEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An occupancy sensor is used to determine a sublevel's occupancy, using redstone output and *when present*,
 * ComputerCraft's peripheral API as feedback for players to automate protocols with regard to their presence.
 */
public class OccupancySensorBlock extends AbstractSensorBlock {

    public static final MapCodec<OccupancySensorBlock> CODEC = simpleCodec(OccupancySensorBlock::new);

    //Configuration
    public OccupancySensorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new OccupancySensorEntity(blockPos, blockState);
    }


    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {return CODEC;}

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {

        super.onPlace(state, level, pos, oldState, movedByPiston);
        //Ensure a sublevel capture is attempted with the block entity on creation.
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof OccupancySensorEntity sensor) {
            sensor.captureSubLevelId(level);
        }
    }



    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if(Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip."+ SableSublevelDetection.MODID+".occupancy_sensor.tooltip"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip."+ SableSublevelDetection.MODID+".shift_tooltip"));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

    }
}
