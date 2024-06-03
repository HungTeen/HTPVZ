package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.interfaces.ISun;
import com.hungteen.pvz.api.interfaces.ISunAbsorber;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.network.SpawnParticlePacket;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZParticles;
import com.hungteen.pvz.util.EntityUtil;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Map;

public class Sun extends Entity implements ISunAbsorber, ISun {
    public static final float SUN_FALL_SPEED = 0.03F;
    public static final int DEFAULT_AMOUNT = 50;
    public static final int MAX_LIVE_TICK = 500;
    public LivingEntity controller = null;
    public Vec3 ColorBase = new Vec3(255,230,15);
    public Vec3 ColorChange = new Vec3(0,25,15);
    private ISunAbsorber attractedBy;
    private Player attractingPlayer = null;
    private static final EntityDataAccessor<Integer> AMOUNT = SynchedEntityData.defineId(Sun.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIVE_TICK = SynchedEntityData.defineId(Sun.class, EntityDataSerializers.INT);

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
    /** drop multiple suns with effects. if each > 0 drop in each, else drop with 50/25/15/5.**/
    public static void spawnSunsWithEffectsByAmount(Level level, BlockPos pos, int amount, int each, float speed) {
        for (int i = amount; i > 0; ) {
            int singleSunAmount = i;
            singleSunAmount = each > 0 ? each : singleSunAmount > 50 ? 50 : singleSunAmount > 25 ? 25 : singleSunAmount > 15 ? 15 : Math.min(singleSunAmount, 5);
            i -= singleSunAmount;
            spawnSunWithEffects(level, singleSunAmount, pos, speed);
        }
    }
    /**
     * spawn sun entity in range randomly with specific amount.
     */
    public static Sun spawnSunWithEffects(Level level, int amount, BlockPos pos, float maxSpeed) {
        Sun sun = spawnByAmount(level, amount, pos,
                new Vec3((level.getRandom().nextFloat() - 0.5) * maxSpeed,
                        level.getRandom().nextFloat() * 0.1 + 0.6 * maxSpeed,
                        (level.getRandom().nextFloat() - 0.5) * maxSpeed));
        for (int j = 10; j <= amount; j += 10){
            SpawnParticlePacket.particle(level, PVZParticles.SUN.get(), Vec3.atCenterOf(pos).add(0, 1, 0));
        }
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

    public int getLiveTick() {
        return this.entityData.get(LIVE_TICK);
    }
    public void setLiveTick(int value) {
        this.entityData.set(LIVE_TICK, value);
    }

    //ISun
    @Override
    public boolean canAttractThis(ISunAbsorber absorber) {
        if (attractedBy != null && attractingPlayer != null) {
            if (distanceToSqr(attractedBy.position()) > 16 && distanceToSqr(attractingPlayer) > 36) {
                return absorber.canAbsorb(this);
            }
        }
        return absorber.canAbsorb(this);
    }

    @Override
    public boolean canAttractThis(Player player) {
        final boolean[] tmp = new boolean[1];
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) ->
                tmp[0] = nbt.getValue(PVZPlayerCapNBT.SUN) < nbt.getValueLimit(PVZPlayerCapNBT.SUN).getSecond());
        return tmp[0];
    }

    @Override
    public Object getAttractor() {
        return this.attractingPlayer == null ? this.attractedBy : this.attractingPlayer;
    }

    @Override
    public boolean setAttractor(Object attractor) {
        if (attractor instanceof Player player) {
            this.attractingPlayer = player;
            return true;
        } else if (attractor instanceof ISunAbsorber absorber){
            this.attractedBy = absorber;
            return true;
        }
        return false;
    }

    @Override
    public void onAbsorbedBy(ISunAbsorber absorber) {
        absorber.onAbsorb(this);
    }

    @Override
    public void onAbsorbedBy(Player player) {
        //sun mending enchantment.
        if (getAmount() >= 50) {
            Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(PVZEnchantments.SUN_MENDING.get(), player, ItemStack::isDamaged);
            if (entry != null) {
                ItemStack itemStack = entry.getValue();
                setAmount(getAmount() - 25);
                itemStack.setDamageValue(itemStack.getDamageValue() - 1);
            }
        }
        //player absorb.
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> {
            nbt.addValue(PVZPlayerCapNBT.SUN, getAmount());
            this.remove(Entity.RemovalReason.DISCARDED);
        });
    }

    //ISunAbsorber
    @Override
    public void onAbsorb(ISun sun) {
        if (sun instanceof Entity) {
            setAmount(getAmount() + sun.getAmount());
            SpawnParticlePacket.particle(level, PVZParticles.SUN.get(), Vec3.atCenterOf(getOnPos()).add(0, 1, 0));
            SpawnParticlePacket.particle(level, PVZParticles.SUN.get(), Vec3.atCenterOf(getOnPos()).add(0, 1, 0));
            ((Entity) sun).remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean canAbsorb(ISun sun){
        if (sun instanceof Sun sun1) {
            return getAmount() < 250 && sun.getAmount() < 250 && sun1.getId() < getId() && distanceToSqr(sun1) < 4;
        } else {
            return false;
        }
    }
    @Override
    public int getContainingSun(){
        return getAmount();
    }
    @Override
    public boolean isSunContainer() {
        return true;
    }

    //basic
    @Override
    public void baseTick() {
        super.baseTick();

        //about sun disappear.
        if(! level.isClientSide) {
            if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.sunDisappear)) {
                this.setLiveTick(this.getLiveTick() + 1);
                if (this.getLiveTick() >= this.getMaxLiveTick()) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                }
            } else {
                this.setLiveTick(0);
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
        } else {
            this.setDeltaMovement(new Vec3(0, 0, 0));
        }
        //choose attractor.
        if (! level.isClientSide && (this.tickCount+this.getId()) % ((this.attractedBy != null && this.attractingPlayer != null) ? 250 : 50) == 0 ||
                ((this.attractedBy != null && this.distanceToSqr(this.attractedBy.position()) > 64.0D) ||
                        (this.attractingPlayer != null && this.distanceToSqr(this.attractingPlayer) > 64.0D)) ||
                (attractedBy instanceof Entity entity && ! EntityUtil.isEntityValid(entity)) ||
                (attractedBy instanceof BlockEntity bEntity && bEntity.isRemoved()) ||
                (attractedBy == null && ! EntityUtil.isEntityValid(attractingPlayer))) {
            this.attractedBy = null;
            this.attractingPlayer = level.getNearestPlayer(this.getX(), this.getY(), getZ(), 6,
                    EntitySelector.NO_SPECTATORS.and((player) -> player instanceof Player && canAttractThis((Player) player)));
            if (attractingPlayer == null) {
                level.getEntities(this, this.getBoundingBox().inflate(6)).forEach((targetEntity) -> {
                    if ((this.attractedBy == null || distanceToSqr(targetEntity) < distanceToSqr(attractedBy.position()))) {
                        if ((targetEntity != this && targetEntity instanceof ISunAbsorber absorber && canAttractThis(absorber))) {
                            this.attractedBy = absorber;
                        }
                    }
                });
                for (int x = -6; x < 6; x ++) {
                    for (int y = -6; y < 6; y ++) {
                        for (int z = -6; z < 6; z ++) {
                            BlockPos pos = this.getOnPos().offset(x, y, z);
                            if (level.getBlockEntity(pos) instanceof ISunAbsorber absorber) {
                                this.attractedBy = absorber;
                            }
                        }
                    }
                }
            }
        }
        //being attracted.
        if (! level.isClientSide) {
            if (this.attractingPlayer != null) {
                if (distanceToSqr(attractingPlayer) < 0.8F){
                    onAbsorbedBy(attractingPlayer);
                }
                Vec3 vec3 = this.attractingPlayer.position().subtract(this.position());
                double d0 = vec3.lengthSqr();
                if (d0 < 25.0D) {
                    double d1 = 1.0D - Math.sqrt(d0) / 5.0D;
                    this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.3D)));
                }
            } else if (this.attractedBy != null) {
                if (distanceToSqr(attractedBy.position()) < 0.8F){
                    onAbsorbedBy(attractedBy);
                }
                Vec3 vec3 = this.attractedBy.position().subtract(this.position());
                double d0 = vec3.lengthSqr();
                if (d0 < 25.0D) {
                    double d1 = 1.0D - Math.sqrt(d0) / 5.0D;
                    this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * (attractedBy instanceof Sun ? 0.1D: 0.3D))));
                }
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
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
        return value < 6 ? 0 : value < 26 ? 1 : value < 51 ? 2 : 3;
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        float w = this.getAmount() * 1f / 200 + 0.2f;
        return EntityDimensions.scalable(w, w);
    }


    public int getMaxLiveTick() {
        return MAX_LIVE_TICK;
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(AMOUNT, 1);
        this.entityData.define(LIVE_TICK, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Amount")) {
            setAmount(tag.getInt("Amount"));
        }
        if (tag.contains("SunLiveTick")) {
            this.setLiveTick(tag.getInt("SunLiveTick"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Amount", this.getAmount());
        tag.putInt("SunLiveTick", this.getLiveTick());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
