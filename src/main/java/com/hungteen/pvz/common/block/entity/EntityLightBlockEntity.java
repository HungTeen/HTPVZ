package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.common.block.EntityLightBlock;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class EntityLightBlockEntity extends BlockEntity {
    public EntityLightBlockEntity(BlockPos pos, BlockState blockState) {
        super(PVZBlockEntities.ENTITY_LIGHT.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, EntityLightBlockEntity blockEntity) {
        if (! level.isClientSide() && blockState.is(PVZBlocks.ENTITY_LIGHT.get())) {
            if (! blockState.getValue(EntityLightBlock.HAS_SOURCE)) {
                level.setBlock(pos, (blockState.getValue(EntityLightBlock.WATERLOGGED) ? Blocks.WATER : Blocks.AIR).defaultBlockState(), 2);
            } else {
                level.setBlock(pos, blockState.setValue(EntityLightBlock.HAS_SOURCE, false), 3);
            }
        }
    }

}
