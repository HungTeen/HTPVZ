package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.creatures.Anger;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.EnumSet;

public class FireImp extends Imp {
    protected Goal fireImpSummonGoal;
    public FireImp(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }
    public void tick() {
        super.tick();
        if (this.level.isClientSide && this.getPose() == Pose.CROAKING) {
            level.addParticle(this.getTicksFrozen() <= 0 ? ParticleTypes.LAVA : ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.3F, getZ(),
                    random.nextFloat() * 0.15 - 0.075, random.nextFloat() * 0.15, random.nextFloat() * 0.15 - 0.075);
        }
    }
    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        fireImpSummonGoal = new FireImpSummonGoal(this);
        this.goalSelector.addGoal(1, fireImpSummonGoal);
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, LivingEntity.class, (entity) -> entity instanceof IPlant,
                10, 1, 1.2D, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test));
    }

    public boolean doHurtTarget(Entity p_34276_) {
        boolean flag = super.doHurtTarget(p_34276_);
        if (flag) {
            float f = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            p_34276_.setSecondsOnFire((int)f);
        }
        return flag;
    }

    public boolean canStandOnFluid(FluidState p_204067_) {
        return p_204067_.is(FluidTags.LAVA);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("skill_cooldown", this.tickCount % 300);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        this.tickCount = tag.getInt("skill_cooldown");
    }
    public static class FireImpSummonGoal extends Goal {
        public final Mob zombie;
        public int angerLife = 90;
        public int spellInterval = 300;
        public int summonInterval = 60;
        public int summonTimes = 2;
        public FireImpSummonGoal(Mob zombie) {
            this.zombie = zombie;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }
        @Override
        public boolean canUse() {
            return zombie.tickCount % spellInterval <= 1
                    && zombie.getPose() == Pose.STANDING
                    && EntityUtil.isEntityValid(zombie.getTarget())
                    && zombie.level.getEntities(this.zombie,
                            zombie.getBoundingBox().inflate(4, 2, 4),
                            entity -> EntityUtil.checkCanEntityBeAttack(zombie, entity)).isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            return zombie.tickCount % spellInterval < summonInterval * (summonTimes + 0.5)
                    && zombie.getPose() == Pose.CROAKING
                    && EntityUtil.isEntityValid(zombie.getTarget())
                    && zombie.level.getEntities(this.zombie,
                    zombie.getBoundingBox().inflate(4, 2, 4),
                    entity -> EntityUtil.checkCanEntityBeAttack(zombie, entity)).isEmpty();
        }

        @Override
        public void stop() {
            super.stop();
            if (zombie.getPose() == Pose.CROAKING) {
                zombie.setPose(Pose.STANDING);
            }
        }

        @Override
        public void start() {
            if (zombie.getPose() != Pose.CROAKING) {
                zombie.setPose(Pose.CROAKING);
            }
        }

        @Override
        public void tick() {
            if (zombie.getTicksFrozen() <= 0 && zombie.getPose() == Pose.CROAKING) {
                zombie.getNavigation().stop();
                if (! EntityUtil.isEntityValid(zombie.getTarget())) return;
                zombie.lookAt(zombie.getTarget(), 10, 10);
                if (zombie.tickCount % summonInterval <= 1 && zombie.tickCount % spellInterval > summonInterval - 10) {
                    Anger anger = new Anger(zombie.level);
                    anger.setPos(this.zombie.position().add(0, 1.6F, 0));
                    anger.setTarget(zombie.getTarget());
                    anger.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this.zombie));
                    anger.setDeltaMovement(this.zombie.getDeltaMovement());
                    anger.yRot = this.zombie.yRot;
                    anger.xRot = this.zombie.xRot;
                    anger.maxLife = angerLife;
                    zombie.level.addFreshEntity(anger);
                    anger.getAttribute(Attributes.FLYING_SPEED).setBaseValue(0.6F);
                    anger.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(zombie.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    Scoreboard scoreboard = zombie.level.getScoreboard();
                    PlayerTeam team = scoreboard.getPlayersTeam(zombie.getScoreboardName());
                    PlayerTeam team1 = scoreboard.getPlayerTeam(PVZMod.ENEMY_TEAM);
                    if (team1 != null) {
                        scoreboard.addPlayerToTeam(anger.getScoreboardName(), team == null ? team1 : team);
                    } else if (team != null) {
                        scoreboard.addPlayerToTeam(anger.getScoreboardName(), team);
                    }
                }
            }
        }
    }
}
