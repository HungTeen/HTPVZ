package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.world.PVZFog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.UUID;

public class ThrownFogInBottle extends ThrowableItemProjectile {
    public ThrownFogInBottle(EntityType<? extends ThrownFogInBottle> p_37510_, Level p_37511_) {
        super(p_37510_, p_37511_);
    }

    public ThrownFogInBottle(Level p_37518_, LivingEntity p_37519_) {
        super(PVZEntities.FOG_IN_BOTTLE.get(), p_37519_, p_37518_);
    }

    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    protected float getGravity() {
        return 0.07F;
    }

    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level instanceof ServerLevel) {
            PVZFog.addFog(level.dimension().location(), this.position(), 30, 10, 15, UUID.randomUUID());
            this.discard();
        }

    }
}