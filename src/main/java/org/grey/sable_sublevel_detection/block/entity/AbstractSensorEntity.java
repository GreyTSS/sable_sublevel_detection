package org.grey.sable_sublevel_detection.block.entity;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.grey.sable_sublevel_detection.ModEvents;
import org.grey.sable_sublevel_detection.PositionData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * OccupancySensorEntity contains the static field registry of all currently loaded sensors belonging to sublevels, to limit query later.
 * the sublevel ID is captured on level change to allow for sensors placed globally to operate normally when assembled into sublevels.
 */
public class AbstractSensorEntity extends BlockEntity {

    private UUID subLevelId;
    public static Map<UUID, HashSet<PositionData>> loadedSensors = new HashMap<>();

    public void captureSubLevelId(Level level) {
        if (level.isClientSide || this.subLevelId != null) return;

        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, this.worldPosition);

        this.subLevelId = subLevel != null ? subLevel.getUniqueId() : null;
        this.setChanged();

        if(!this.isRemoved()) {
            registerSensor();
        }
    }

    private void registerSensor() {
        if(this.getLevel() == null || this.subLevelId == null) return;
        GlobalPos globalPos = GlobalPos.of(this.getLevel().dimension(), this.getBlockPos());
        loadedSensors.computeIfAbsent(subLevelId, k -> new HashSet<>()).add(new PositionData(globalPos));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerSensor();
        if(subLevelId==null || this.getLevel() == null) return;
        var occupied = ModEvents.getOccupantsMap().containsKey(subLevelId);
        this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(BlockStateProperties.POWERED, occupied));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(this.getLevel() == null || this.subLevelId == null) return;
        var sublevelSensors = loadedSensors.get(subLevelId);
        if(sublevelSensors!=null) {
            sublevelSensors.remove(new PositionData(GlobalPos.of(this.getLevel().dimension(), this.getBlockPos())));
            if(loadedSensors.get(subLevelId).isEmpty()) loadedSensors.remove(subLevelId);
        }
    }



    public UUID getSubLevelId() {
        return subLevelId;
    }


    public AbstractSensorEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
       super(type, pos, blockState);

    }




    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if(this.subLevelId!=null) tag.putUUID("SublevelUUID", this.subLevelId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if(tag.hasUUID("SublevelUUID")) this.subLevelId = tag.getUUID("SublevelUUID");

    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if(this.getLevel()!=null) captureSubLevelId(this.getLevel());
    }
}
