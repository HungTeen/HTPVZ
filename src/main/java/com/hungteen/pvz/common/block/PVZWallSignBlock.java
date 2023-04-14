package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.block.entity.PVZSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

public class PVZWallSignBlock extends WallSignBlock {
    public PVZWallSignBlock(Properties p_56273_, WoodType p_56274_) {
        super(p_56273_, p_56274_);
    }

    public BlockEntity newBlockEntity(BlockPos p_154556_, BlockState p_154557_) {
        return new PVZSignBlockEntity(p_154556_, p_154557_);
    }

}
