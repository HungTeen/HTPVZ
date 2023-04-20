package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.common.register.PVZBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PVZSignBlockEntity extends SignBlockEntity {
    public PVZSignBlockEntity(BlockPos pos, BlockState state){
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType(){
        return PVZBlockEntities.SIGN.get();
    }
}
