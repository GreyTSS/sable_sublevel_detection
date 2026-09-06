package org.grey.sable_sublevel_detection.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


/**
 * An abstract sensor is used to determine a sublevel's occupancy, using redstone output and *when present*,
 * ComputerCraft's peripheral API as feedback for players to automate protocols with regard to their presence. This
 * abstract sensor allows specialized subtypes like the seated occupancy sensor.
 */
public abstract class AbstractSensorBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    public static final Map<Block, Item> SWAPPER = new HashMap<>();
    public static final Map<Item, Block> REVERSE_SWAPPER = new HashMap<>();

    //Configuration
    public AbstractSensorBlock(Properties properties) {
        super(properties);


        this.registerDefaultState(this.defaultBlockState()
                .setValue(POWERED, false)
                .setValue(INVERTED, false));
    }

    @Override
    public abstract @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState);

    @Override
    protected abstract MapCodec<? extends BaseEntityBlock> codec();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        builder.add(INVERTED);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    ;


    //Redstone
    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ^ state.getValue(INVERTED) ? 15 : 0;
    }


    @Override
    protected void neighborChanged(BlockState p_60509_, Level p_60510_, BlockPos p_60511_, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {


    }

    //Inversion
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            System.out.println(level.getBlockState(pos).getValue(POWERED));
            var entity = level.getBlockEntity(pos);
            ItemStack usedItem = player.getItemInHand(player.getUsedItemHand());
            Block currentBlock = level.getBlockState(pos).getBlock();
            if (!usedItem.isEmpty()) {

                //If swap can occur
                if (SWAPPER.containsValue(usedItem.getItem()) && REVERSE_SWAPPER.containsValue(currentBlock) && REVERSE_SWAPPER.get(usedItem.getItem()) != currentBlock) {


                    var newState = REVERSE_SWAPPER.get(usedItem.getItem()).defaultBlockState()
                            .setValue(POWERED, false)
                            .setValue(INVERTED, state.getValue(INVERTED));

                    //Switch Sensors
                    level.setBlock(pos, newState, UPDATE_NEIGHBORS);

                    //Drop Old Signature
                    ItemStack drop = new ItemStack(SWAPPER.get(currentBlock));
                    ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), drop);
                    level.addFreshEntity(itemEntity);

                    usedItem.consume(1, player);
                    return InteractionResult.SUCCESS;
                }

                //Fail if non-swappable hand item
                return InteractionResult.PASS;



            }

            //Invert if empty-handed
            boolean current = state.getValue(INVERTED);
            level.setBlockAndUpdate(pos, state.setValue(INVERTED, !current));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    //Rendering
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }


    public record Swapper(Block block, Item item) {
    }

    ;

}
