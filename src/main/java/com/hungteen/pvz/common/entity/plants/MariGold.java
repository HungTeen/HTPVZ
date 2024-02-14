package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.plants.base.ProducerPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class MariGold extends ProducerPlant {
    public MariGold(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void genSomething() {
        ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getEyeY(), this.getZ(), this.getRandomIngot().getDefaultInstance());
//        EntityUtil.onEntityRandomPosSpawn(level, itementity, blockPosition().offset(0.5, -0.5, 0.5), 0);
        BlockPos pos = blockPosition();
        itementity.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        level.addFreshEntity(itementity);
    }

    private Item getRandomIngot() {
        final int num = this.getRandom().nextInt(100);
         if (num < getIronChance()) {
            return Items.IRON_INGOT;
        }
        return Items.GOLD_INGOT;
    }

    public int getIronChance() {
        return 75;
    }

    @Override
    public int getGenCD() {
        final int time = 500;
        return time;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 10D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
}
