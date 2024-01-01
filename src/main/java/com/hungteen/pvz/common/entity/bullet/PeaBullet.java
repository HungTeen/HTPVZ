package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.entity.plants.PeaShooter;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PeaBullet extends AbstractBulletEntity{


    public PeaBullet(EntityType<? extends AbstractBulletEntity> entityIn, Level level) {
        super(entityIn,level);
        this.noPhysics = true;
    }

    public PeaBullet(Level worldIn, PeaShooter peaShooter) {
        super(PVZEntities.PEA.get(), worldIn, peaShooter);
    }
    @Override
    protected void dealDamageTo(Entity target) {
        this.canExist = false;
        final float damage = this.getAttackDamage();

        //default normal pea damage.
        DamageSource source = DamageSource.MAGIC;

        target.hurt(source, damage);
    }


    @Override
    protected int getMaxLiveTick() {
        return 120;
    }


}
