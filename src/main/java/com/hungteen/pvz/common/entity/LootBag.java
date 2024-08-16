package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.item.LootBagItem;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LootBag extends ItemEntity {

    public LootBag(EntityType<? extends ItemEntity> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    public static LootBag drop(Level level, BlockPos pos, ResourceLocation loot) {
        return drop(level, pos, loot, 5);
    }

    public static LootBag drop(Level level, BlockPos pos, ResourceLocation lootTable, int size) {
        LootBag bag = PVZEntities.LOOT_BAG.get().create(level);
        bag.setItem(LootBagItem.modify(PVZItems.LOOT_BAG.get().getDefaultInstance(), lootTable, size));
        bag.moveTo(Vec3.atCenterOf(pos));
        level.addFreshEntity(bag);
        bag.setDeltaMovement(bag.random.nextFloat() / 3, bag.random.nextFloat() / 3, bag.random.nextFloat() / 3);
        return bag;
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return true;
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }
}
