package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;

public class PVZChestBoat extends ChestBoat {
    private WoodType woodType = PVZBlocks.woodTypeList.get(0);


    public PVZChestBoat(EntityType<? extends Boat> p_38290_, Level p_38291_) {
        super(p_38290_, p_38291_);
    }

    public PVZChestBoat(Level p_38293_, double p_38294_, double p_38295_, double p_38296_) {
        this(PVZEntities.CHEST_BOAT.get(), p_38293_);
        this.setPos(p_38294_, p_38295_, p_38296_);
        this.xo = p_38294_;
        this.yo = p_38295_;
        this.zo = p_38296_;
    }
    @Override
    public Item getDropItem(){
        return PVZItems.boatItemList.get(Pair.of(this.woodType, true)).get();
    }
    @Override
    public EntityDimensions getDimensions(Pose poseIn){
        return EntityDimensions.scalable(1.375F, 0.5625F);
    }

    public void setWoodType(WoodType woodType){
        this.woodType = woodType;
    }
    public WoodType getWoodType(){
        return woodType;
    }
}
