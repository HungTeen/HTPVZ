package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.api.interfaces.ISunAbsorber;
import com.hungteen.pvz.api.interfaces.ISun;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.capability.pvzRules.PVZRulesCapability;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Map;

public class Sun extends Entity implements ISunAbsorber, ISun {
    public static final float SUN_FALL_SPEED = 0.03F;
    public static final int DEFAULT_AMOUNT = 50;
    public static final int MAX_LIVE_TICK = 500;
    public int sunLiveTick = 0;
    public LivingEntity controller = null;
    public Vec3 ColorBase = new Vec3(255,230,15);
    public Vec3 ColorChange = new Vec3(0,25,15);
    private Entity attractedBy;
    private static final EntityDataAccessor<Integer> AMOUNT = SynchedEntityData.defineId(Sun.class, EntityDataSerializers.INT);

    public Sun(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        setAmount(DEFAULT_AMOUNT);
        this.setNoGravity(true);
    }
    public static Sun spawnByAmount(Level level, int amount, BlockPos pos, Vec3 speed) {
        Sun sun = PVZEntities.SUN.get().create(level);
        sun.setAmount(amount);
        sun.moveTo(Vec3.atCenterOf(pos));
        sun.setDeltaMovement(speed);
        level.addFreshEntity(sun);
        return sun;
    }

    //ISun.
    @Override
    public int getAmount(){
        return this.entityData.get(AMOUNT);
    }

    @Override
    public void setAmount(int num){
        this.entityData.set(AMOUNT, num);
    }


    @Override
    public boolean canAttractThis(Entity entity) {
        if (entity instanceof Player) {
            final boolean[] tmp = new boolean[1];
            PVZPlayerCapability.getPlayerData((Player) entity).ifPresent((nbt) -> tmp[0] = nbt.getValue(PVZPlayerCapNBT.SUN) < nbt.getValueLimit(PVZPlayerCapNBT.SUN).getSecond());
            return tmp[0];
        } else if ((!(attractedBy instanceof Player) || distanceToSqr(attractedBy) > 16) && entity instanceof ISunAbsorber) {
            return ((ISunAbsorber) entity).canAbsorb(this);
        }
        return false;
    }

    @Override
    public void onAbsorbedBy(Entity entity) {
        if (entity instanceof Player) {
            //sun mending enchantment.
            if (getAmount() >= 50) {
                Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(PVZEnchantments.SUN_MENDING.get(), (Player) entity, ItemStack::isDamaged);
                if (entry != null) {
                    ItemStack itemStack = entry.getValue();
                    setAmount(getAmount() - 25);
                    itemStack.setDamageValue(itemStack.getDamageValue() - 1);
                }
            }
            //player absorb.
            PVZPlayerCapability.getPlayerData((Player) entity).ifPresent((nbt) -> {
                nbt.addValue(PVZPlayerCapNBT.SUN, getAmount());
                this.remove(Entity.RemovalReason.DISCARDED);
            });
        } else if (entity instanceof ISunAbsorber) {
            ((ISunAbsorber) entity).onAbsorb(this);
        }
    }

    //ISunAbsorber
    @Override
    public void onAbsorb(ISun sun) {
        if (sun instanceof Entity) {
            setAmount(getAmount() + sun.getAmount());
            ((Entity) sun).remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean canAbsorb(ISun sun){
        if (sun instanceof Entity) {
            return getAmount() < 150 && sun.getAmount() < 150 && ((Entity) sun).getId() < getId() && distanceToSqr(((Entity) sun)) < 4;
        } else {
            return false;
        }
    }
    @Override
    public int getContainingSun(){
        return getAmount();
    }


    @Override
    public void baseTick() {
        super.baseTick();

        //about sun disappear.
        if(! level.isClientSide) {
            if (PVZRulesCapability.get("sunDisappear") && ++ this.sunLiveTick >= this.getMaxLiveTick()) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        //natural fall.
        if(! this.onGround && ! this.isInWater()) {
            double speedY = this.getDeltaMovement().y;
            if(speedY > - SUN_FALL_SPEED){
                speedY -= SUN_FALL_SPEED / 2;
            } else{
                speedY = -SUN_FALL_SPEED;
            }
            this.setDeltaMovement(this.getDeltaMovement().x * 0.94, speedY, this.getDeltaMovement().z * 0.94);
        } else{
            this.setDeltaMovement(new Vec3(0, 0, 0));
        }
        //choose attractor.
        if ((this.tickCount+this.getId()) % ((this.attractedBy != null) ? 250 : 50) == 0 || (this.attractedBy != null && this.attractedBy.distanceToSqr(this) > 64.0D)) {
            this.attractedBy = null;
            level.getEntities(this, this.getBoundingBox().inflate(6)).forEach((targetEntity) -> {
                if ((this.attractedBy == null || distanceToSqr(targetEntity) < distanceToSqr(attractedBy)) && canAttractThis(targetEntity)) {
                    this.attractedBy = targetEntity;
                }
            });
        }
        //being attracted.
        if (this.attractedBy != null) {
            if (!level.isClientSide() && distanceToSqr(attractedBy) < 0.8F){
                onAbsorbedBy(attractedBy);
            }
            Vec3 vec3 = new Vec3(this.attractedBy.getX() - this.getX(), this.attractedBy.getY() + (double)this.attractedBy.getEyeHeight() / 2.0D - this.getY(), this.attractedBy.getZ() - this.getZ());
            double d0 = vec3.lengthSqr();
            if (d0 < 25.0D) {
                double d1 = 1.0D - Math.sqrt(d0) / 5.0D;
                this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * (attractedBy instanceof Sun ? 0.1D: 0.3D))));
            }
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        //about controlled color.
        if (this.controller != null && (this.controller.isSpectator() || (!this.controller.isAlive()))) {
            this.controller = null;
        }
        if (this.controller == null){
            ColorBase = new Vec3(255,230,15);
            ColorChange = new Vec3(0,25,15);
        }
    }

    public int getIcon() {
        final int value = this.getAmount();
        return value < 6 ? 0 : value < 16 ? 1 : value < 26 ? 2 : 3;
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        float w = this.getAmount() * 1f / 200 + 0.2f;
        return EntityDimensions.scalable(w, w);
    }


    public int getMaxLiveTick() {
        return PVZRulesCapability.get("sunDisappear") ? MAX_LIVE_TICK : -1;
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(AMOUNT, 1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Amount")) {
            setAmount(tag.getInt("amount"));
        }
        if (tag.contains("SunLiveTick")) {
            this.sunLiveTick = tag.getInt("SunLiveTick");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Amount", this.getAmount());
        tag.putInt("SunLiveTick", this.sunLiveTick);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
