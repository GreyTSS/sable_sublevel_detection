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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class OccupancySensorEntity extends BlockEntity {

    private UUID subLevelId;
    public static Map<UUID, HashSet<PositionData>> loadedSensors = new HashMap<>();
    public void captureSubLevelId(Level level) {
        if (level.isClientSide || this.subLevelId != null) {
            return;
        }

        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, this.worldPosition);
        this.subLevelId = subLevel != null ? subLevel.getUniqueId() : null;

        this.setChanged();
    }

    @Override
    public void onLoad() {
        if(this.getLevel() == null) return;
        loadedSensors.computeIfAbsent(subLevelId, k -> new HashSet<>()).add(new PositionData(GlobalPos.of(this.getLevel().dimension(), this.getBlockPos()),this.level));
    }

    @Override
    public void setRemoved() {
        if(this.getLevel() == null) return;
        loadedSensors.get(subLevelId).remove(new PositionData(GlobalPos.of(this.getLevel().dimension(), this.getBlockPos()),this.getLevel()));
        if(loadedSensors.get(subLevelId).isEmpty()) loadedSensors.remove(subLevelId);
    }



    public UUID getSubLevelId() {
        return subLevelId;
    }

    public OccupancySensorEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.OCCUPANCY_SENSOR_BE.get(), pos, blockState);
    }


    public record PositionData(GlobalPos globalPos, Level level){}

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if(this.subLevelId!=null) tag.putUUID("SublevelUUID", this.subLevelId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.subLevelId = tag.getUUID("SublevelUUID");
    }
}
