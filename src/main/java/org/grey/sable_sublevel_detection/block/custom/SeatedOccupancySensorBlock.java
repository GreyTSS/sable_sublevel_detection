package org.grey.sable_sublevel_detection.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.grey.sable_sublevel_detection.SableSublevelDetection;
import org.grey.sable_sublevel_detection.block.entity.OccupancySensorEntity;
import org.grey.sable_sublevel_detection.block.entity.SeatedOccupancySensorEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An occupancy sensor is used to determine a sublevel's occupancy, using redstone output and *when present*,
 * ComputerCraft's peripheral API as feedback for players to automate protocols with regard to their presence.
 */
public class SeatedOccupancySensorBlock extends AbstractSensorBlock {

    public static final MapCodec<SeatedOccupancySensorBlock> CODEC = simpleCodec(SeatedOccupancySensorBlock::new);

    //Configuration
    public SeatedOccupancySensorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SeatedOccupancySensorEntity(blockPos, blockState);
    }


    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {return CODEC;}

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {

        super.onPlace(state, level, pos, oldState, movedByPiston);
        //Ensure a sublevel capture is attempted with the block entity on creation.
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SeatedOccupancySensorEntity sensor) {
            sensor.captureSubLevelId(level);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if(Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip."+ SableSublevelDetection.MODID+".seated_occupancy_sensor.tooltip2"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip."+ SableSublevelDetection.MODID+".seated_occupancy_sensor.tooltip1"));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

    }
}
