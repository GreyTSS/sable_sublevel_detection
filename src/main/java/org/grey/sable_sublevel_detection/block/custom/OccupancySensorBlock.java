package org.grey.sable_sublevel_detection.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import org.grey.sable_sublevel_detection.block.entity.OccupancySensorEntity;
import org.jetbrains.annotations.Nullable;

/**
 * An occupancy sensor is used to determine a sublevel's occupancy, using redstone output and *when present*,
 * ComputerCraft's peripheral API as feedback for players to automate protocols with regard to their presence.
 */
public class OccupancySensorBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;;
    public static final MapCodec<OccupancySensorBlock> CODEC = simpleCodec(OccupancySensorBlock::new);

    //Configuration
    public OccupancySensorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(POWERED, false)
                .setValue(INVERTED, false));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {return new OccupancySensorEntity(blockPos, blockState);}

     @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {return CODEC;}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        builder.add(INVERTED);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        //Ensure a sublevel capture is attempted with the block entity on creation.
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof OccupancySensorEntity sensor) {
            sensor.captureSubLevelId(level);
        }
    }



    //Redstone
    @Override
    protected boolean isSignalSource(BlockState state) {return true;}

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {return state.getValue(POWERED) ^ state.getValue(INVERTED) ? 15 : 0;}

    //Inversion
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getItemInHand(player.getUsedItemHand()).isEmpty()) {
            return InteractionResult.PASS;
        }
        if(!level.isClientSide) {
            boolean current = state.getValue(INVERTED);
            level.setBlockAndUpdate(pos, state.setValue(INVERTED, !current));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    //Rendering
    @Override
    protected RenderShape getRenderShape(BlockState state) {return RenderShape.MODEL;}
}
