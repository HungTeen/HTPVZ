package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.common.register.PVZBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EssenceAltarBlockEntity extends BlockEntity implements Nameable {
    private Component name;


    public EssenceAltarBlockEntity(BlockPos pos, BlockState blockState) {
        super(PVZBlockEntities.ESSENCE_ALTAR.get(), pos, blockState);
    }

    @Override
    public Component getName() {
        return this.getCustomName() == null ? Component.translatable("block.pvz.essence_altar") : this.getCustomName();
    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.hasCustomName()) {
            tag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    public void setCustomName(@javax.annotation.Nullable Component component) {
        this.name = component;
    }

    @javax.annotation.Nullable
    public Component getCustomName() {
        return this.name;
    }
}
