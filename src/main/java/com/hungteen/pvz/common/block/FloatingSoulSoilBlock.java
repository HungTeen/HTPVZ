package com.hungteen.pvz.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class FloatingSoulSoilBlock extends Block {
    public FloatingSoulSoilBlock(Properties p_49795_) {
        super(p_49795_);
    }
    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion)
    {
        level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        this.wasExploded(level, pos, explosion);
    }
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid)
    {
        this.playerWillDestroy(level, pos, state, player);
        int silkTouchLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, player.getMainHandItem());
        return level.setBlock(pos, fluid.getType() == Fluids.EMPTY && silkTouchLevel == 0 ? Blocks.LAVA.defaultBlockState() : fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
    }
}
