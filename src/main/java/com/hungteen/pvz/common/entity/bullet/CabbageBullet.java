package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CabbageBullet extends BaseBullet {
    public CabbageBullet(EntityType<? extends BaseBullet> entityIn, Level level) {
        super(entityIn,level);
        this.setNoGravity(false);
    }

    public CabbageBullet(Level worldIn, LivingEntity cabbagePult) {
        super(PVZEntities.CABBAGE.get(), worldIn, cabbagePult);
        setOwner(cabbagePult);
        this.setNoGravity(false);
    }

    protected void splashParticle() {
        Vec3 movement = getDeltaMovement();
        for (int i = 0; i < 5; i ++) {
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PVZItems.CABBAGE.get())),
                    getX(), getY(), getZ(),
                    - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.12,
                    - movement.y * 0.25 + random.nextFloat() * 0.25,
                    - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.12);
        }
    }

}
