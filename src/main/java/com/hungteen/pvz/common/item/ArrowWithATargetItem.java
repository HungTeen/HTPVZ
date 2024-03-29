package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.entity.bullet.ArrowWithATarget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArrowWithATargetItem extends ArrowItem {
    public ArrowWithATargetItem(Properties p_40512_) {
        super(p_40512_);
    }
    public AbstractArrow createArrow(Level level, ItemStack p_40514_, LivingEntity entity) {
        return new ArrowWithATarget(level, entity);
    }
}
