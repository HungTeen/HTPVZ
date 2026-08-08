package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.bullet.ModifiedFireBall;
import com.hungteen.pvz.common.entity.zombies.GhastRiderBoss;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.UUID;

public class LavaGhastling extends Ghast {
    public static UUID RIDEN_BY_BOSS_MODIFIER_UUID = UUID.fromString("33ffd765-acbb-d867-840b-2726daa6c655");
    public LavaGhastling(EntityType<? extends Ghast> p_32725_, Level p_32726_) {
        super(p_32725_, p_32726_);
    }
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RidenByBossGoal(this));
        this.goalSelector.addGoal(5, new EvilGhastlingRandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new GhastLookGoal(this));
        this.goalSelector.addGoal(7, new EvilGhastlingShootFireballGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                (entity) -> ! EntityUtil.isTeammate(this, entity) && Math.abs(entity.getY() - this.getY()) <= 4.0D));
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(PVZAttributes.PLANT_HURT_RESISTANCE.get(), 0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    protected float getStandingEyeHeight(Pose p_32741_, EntityDimensions p_32742_) {
        return 0.6F;
    }

    public boolean canStandOnFluid(FluidState p_204067_) {
        return p_204067_.is(FluidTags.LAVA);
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
        if (this.getFirstPassenger() instanceof GhastRiderBoss boss && boss.ghastlings.size() > 1) {
            return false;
        }
        return super.hurt(damageSource, amount);
    }

    static class GhastLookGoal extends Goal {
        private final Ghast ghast;

        public GhastLookGoal(Ghast p_32762_) {
            this.ghast = p_32762_;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return true;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            if (this.ghast.getTarget() == null) {
                Vec3 vec3 = this.ghast.getDeltaMovement();
                this.ghast.setYRot(-((float) Mth.atan2(vec3.x, vec3.z)) * (180F / (float)Math.PI));
                this.ghast.yBodyRot = this.ghast.getYRot();
            } else {
                LivingEntity livingentity = this.ghast.getTarget();
                if (livingentity.distanceToSqr(this.ghast) < 4096.0D) {
                    double d1 = livingentity.getX() - this.ghast.getX();
                    double d2 = livingentity.getZ() - this.ghast.getZ();
                    this.ghast.setYRot(-((float)Mth.atan2(d1, d2)) * (180F / (float)Math.PI));
                    this.ghast.yBodyRot = this.ghast.getYRot();
                }
            }

        }
    }
    static class EvilGhastlingShootFireballGoal extends Goal {
        private final Ghast ghast;
        public int chargeTime;

        public EvilGhastlingShootFireballGoal(Ghast p_32776_) {
            this.ghast = p_32776_;
        }

        public boolean canUse() {
            boolean enemyTooClose = false;
            if (! ghast.level.getEntities(EntityTypeTest.forClass(Entity.class),
                    this.ghast.getBoundingBox().inflate(4, 4, 4),
                    entity -> ! EntityUtil.isTeammate(ghast, entity)).isEmpty()) {
                enemyTooClose = true;
            }
            return this.ghast.getTarget() != null && ! enemyTooClose;
        }

        public void start() {
            this.chargeTime = 0;
        }

        public void stop() {
            this.ghast.setCharging(false);
            this.chargeTime = -80;
        }

        public void tick() {
            LivingEntity livingentity = this.ghast.getTarget();
            if (livingentity != null) {
                if (livingentity.distanceToSqr(this.ghast) < 4096.0D && this.ghast.hasLineOfSight(livingentity)) {
                    Level level = this.ghast.level;
                    ++this.chargeTime;
                    if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                        level.levelEvent(null, 1015, this.ghast.blockPosition(), 0);
                    }

                    if (this.chargeTime == 20) {
                        Vec3 vec3 = this.ghast.getViewVector(1.0F);
                        double d2 = livingentity.getX() - (this.ghast.getX() + vec3.x * 4.0D);
                        double d3 = livingentity.getY(0.5D) - this.ghast.getY(0.5D);
                        double d4 = livingentity.getZ() - (this.ghast.getZ() + vec3.z * 4.0D);
                        if (!this.ghast.isSilent()) {
                            level.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
                        }

                        Fireball fireball = new ModifiedFireBall(level, this.ghast, d2, d3, d4, this.ghast.getExplosionPower(), null, true);
                        fireball.setPos(this.ghast.getX() + vec3.x, this.ghast.getY(0.5D) + 0.5D, fireball.getZ() + vec3.z);
                        level.addFreshEntity(fireball);
                        this.chargeTime = -80;
                    }
                } else if (this.chargeTime > 0) {
                    --this.chargeTime;
                }

                this.ghast.setCharging(this.chargeTime > 10);
            }
        }
    }
    static class RidenByBossGoal extends Goal {

        private final Ghast ghast;
        public RidenByBossGoal(Ghast p_32783_) {
            this.ghast = p_32783_;
        }
        @Override
        public boolean canUse() {
            if (ghast.getFirstPassenger() instanceof GhastRiderBoss boss) {
                if (boss.getTarget() != null) ghast.setTarget(boss.getTarget());
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            EntityUtil.addModifierToAttribute(this.ghast, Attributes.ARMOR,
                    new AttributeModifier(RIDEN_BY_BOSS_MODIFIER_UUID, "riden_by_boss_bonus", 20, AttributeModifier.Operation.ADDITION));
            EntityUtil.addModifierToAttribute(this.ghast, Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(RIDEN_BY_BOSS_MODIFIER_UUID, "riden_by_boss_bonus", 20, AttributeModifier.Operation.ADDITION));
        }

        @Override
        public void stop() {
            EntityUtil.removeModifierFromAttribute(this.ghast, Attributes.ARMOR, RIDEN_BY_BOSS_MODIFIER_UUID);
            EntityUtil.removeModifierFromAttribute(this.ghast, Attributes.ARMOR_TOUGHNESS, RIDEN_BY_BOSS_MODIFIER_UUID);
        }
    }
    static class EvilGhastlingRandomFloatAroundGoal extends Goal {
        private final Ghast ghast;

        public EvilGhastlingRandomFloatAroundGoal(Ghast p_32783_) {
            this.ghast = p_32783_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            if (this.ghast.isVehicle()) return false;
            MoveControl movecontrol = this.ghast.getMoveControl();
            if (! movecontrol.hasWanted()) {
                return true;
            } else {
                if (ghast.tickCount % 100 > 1) {
                    return false;
                }
                double d0 = movecontrol.getWantedX() - this.ghast.getX();
                double d1 = movecontrol.getWantedY() - this.ghast.getY();
                double d2 = movecontrol.getWantedZ() - this.ghast.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                return d3 < 1.0D || d3 > 3600D;
            }
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            RandomSource randomsource = this.ghast.getRandom();
            GhastRiderBoss ghastRider = null;
            if (PVZEntityCapability.getOwner(this.ghast) instanceof GhastRiderBoss g) {
                ghastRider = g;
            }
            double dx, dy, dz;
            if (ghastRider != null && ghastRider.homePos != null) {
                Vec3 pos = Vec3.atCenterOf(ghastRider.homePos);
                float angle = randomsource.nextFloat() * 6.28f;
                dy = pos.y + 4 + randomsource.nextFloat() * 8F;
                dx = pos.x + 8 * Math.sin(angle);
                dz = pos.z + 8 * Math.cos(angle);
            } else {
                Vec3 pos = this.ghast.position();
                dx = pos.x + ((randomsource.nextFloat() - 0.5) * 24F);
                dy = pos.y + ((randomsource.nextFloat() - 0.5) * 8F);
                dz = pos.z + ((randomsource.nextFloat() - 0.5) * 24F);
            }
            BlockPos tmp = new BlockPos(dx, dy, dz);
            for (int i = 0; i < 5; i ++) {
                if (ghast.level.getBlockState(tmp).isSuffocating(ghast.level, tmp)) {
                    dy --;
                }
            }
            this.ghast.getMoveControl().setWantedPosition(dx, dy, dz, 1.0D);
        }
    }
}
