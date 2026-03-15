package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.zombies.GhastRiderBoss;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

public class LavaGhastling extends Ghast {
    static UUID RIDEN_BY_BOSS_MODIFIER_UUID = UUID.fromString("33ffd765-acbb-d867-840b-2726daa6c655");
    public LavaGhastling(EntityType<? extends Ghast> p_32725_, Level p_32726_) {
        super(p_32725_, p_32726_);
    }
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new EvilGhastlingRandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new GhastLookGoal(this));
        this.goalSelector.addGoal(7, new EvilGhastlingShootFireballGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (p_32755_) -> Math.abs(p_32755_.getY() - this.getY()) <= 4.0D));
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    protected float getStandingEyeHeight(Pose p_32741_, EntityDimensions p_32742_) {
        return 0.6F;
    }

    public boolean canStandOnFluid(FluidState p_204067_) {
        return p_204067_.is(FluidTags.LAVA);
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
            this.chargeTime = -120;
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
                        double d2 = livingentity.getX() - (this.ghast.getX() + vec3.x * 4.0D) + ghast.getRandom().nextFloat() * 10 - 5;
                        double d3 = livingentity.getY(0.5D) - (0.5D + this.ghast.getY(0.5D));
                        double d4 = livingentity.getZ() - (this.ghast.getZ() + vec3.z * 4.0D) + ghast.getRandom().nextFloat() * 10 - 5;
                        if (!this.ghast.isSilent()) {
                            level.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
                        }

                        LargeFireball largefireball = new LargeFireball(level, this.ghast, d2, d3, d4, this.ghast.getExplosionPower());
                        largefireball.setPos(this.ghast.getX() + vec3.x, this.ghast.getY(0.5D) + 0.5D, largefireball.getZ() + vec3.z);
                        level.addFreshEntity(largefireball);
                        this.chargeTime = -120;
                    }
                } else if (this.chargeTime > 0) {
                    --this.chargeTime;
                }

                this.ghast.setCharging(this.chargeTime > 10);
            }
        }
    }
    static class RidenByBossArmorGoal extends Goal {

        private final Ghast ghast;
        public RidenByBossArmorGoal(Ghast p_32783_) {
            this.ghast = p_32783_;
        }
        @Override
        public boolean canUse() {
            return ghast.getFirstPassenger() instanceof GhastRiderBoss;
        }

        @Override
        public void start() {
            EntityUtil.addModifierToAttribute(this.ghast, Attributes.ARMOR,
                    new AttributeModifier(RIDEN_BY_BOSS_MODIFIER_UUID, "riden_by_boss_bonus", 10, AttributeModifier.Operation.ADDITION));
        }

        @Override
        public void stop() {
            EntityUtil.removeModifierFromAttribute(this.ghast, Attributes.ARMOR, RIDEN_BY_BOSS_MODIFIER_UUID);
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
                double d0 = movecontrol.getWantedX() - this.ghast.getX();
                double d1 = movecontrol.getWantedY() - this.ghast.getY();
                double d2 = movecontrol.getWantedZ() - this.ghast.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                return d3 < 1.0D || d3 > 3600.0D;
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
            Vec3 pos = ghastRider != null && ghastRider.homePos != null ? Vec3.atCenterOf(ghastRider.homePos) : this.ghast.position();
            double d0 = pos.x + (double)((randomsource.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d1 = pos.y + (double)((randomsource.nextFloat() * 2.0F - 1F) * 16.0F); //goes up more.
            double d2 = pos.z + (double)((randomsource.nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.ghast.getMoveControl().setWantedPosition(d0, d1, d2, 1.0D);
        }
    }
}
