package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;

public class PVZBoat extends Boat {
    private WoodType woodType = PVZBlocks.woodTypeList.get(0);


    public PVZBoat(EntityType<? extends Boat> p_38290_, Level p_38291_) {
        super(p_38290_, p_38291_);
    }

    public PVZBoat(Level p_38293_, double p_38294_, double p_38295_, double p_38296_) {
        this(PVZEntities.BOAT.get(), p_38293_);
        this.setPos(p_38294_, p_38295_, p_38296_);
        this.xo = p_38294_;
        this.yo = p_38295_;
        this.zo = p_38296_;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag p_38359_){
        p_38359_.putString("WoodType", this.getWoodType().name());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag p_38338_){
        if (p_38338_.contains("WoodType", 8)) {
            this.setWoodType(p_38338_.getString("WoodType"));
        }
    }
    @Override
    public Item getDropItem(){
        return PVZItems.boatItemMap.get(Pair.of(this.woodType, false)).get();
    }

    public void setWoodType(WoodType woodType){
        this.woodType = woodType;
    }
    public void setWoodType(String woodType){
        PVZBlocks.woodTypeList.forEach((t) -> {
            if (t.name().equals(woodType)){
                this.woodType = t;
            }
        });
    }
    public WoodType getWoodType(){
        return woodType;
    }
}
