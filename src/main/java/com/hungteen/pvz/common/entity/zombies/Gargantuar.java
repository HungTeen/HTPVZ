package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.ai.goal.BlockWithShieldGoal;
import com.hungteen.pvz.common.entity.ai.goal.GroupShareEnemyGoal;
import com.hungteen.pvz.common.network.PlayerKnockBackPacket;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZItemTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class Gargantuar extends PVZZombie {


    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState throwAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public Gargantuar(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
        this.idleAnimationState.start(this.tickCount);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return PVZZombie.createAttributes()
                .add(Attributes.ARMOR, 120D)
                .add(Attributes.ARMOR_TOUGHNESS, 80D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.ATTACK_SPEED, 1D)
                .add(Attributes.ATTACK_DAMAGE, 4D)
                .add(Attributes.MAX_HEALTH, 60D);
    }
    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new BlockWithShieldGoal(this));
        this.goalSelector.addGoal(1, new GargantuarRiderGoal(this));
        this.goalSelector.addGoal(2, new GargantuarAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new GroupShareEnemyGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, true, (entity) -> entity instanceof IPlant && EntityUtil.checkCanEntityBeAttack(this, entity)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        if (DATA_POSE.equals(p_219422_)) {
            if (this.getPose() == Pose.USING_TONGUE) {
                this.throwAnimationState.start(this.tickCount);
            } else if (this.getPose() == Pose.SPIN_ATTACK) {
                this.attackAnimationState.start(this.tickCount);
            }
        }

        super.onSyncedDataUpdated(p_219422_);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, data, tag);
        this.setLeftHanded(false);
        this.setItemInHand(InteractionHand.MAIN_HAND, PVZItems.ANVIL_HAMMER.get().getDefaultInstance());
        this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        if (! this.isVehicle()) {
            Mob mob = this.getRider(level, difficulty, spawnType);
            if (mob != null) {
                mob.startRiding(this);
            }
        }
        return spawnGroupData;
    }

    public Mob getRider(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType) {
        Imp imp = PVZEntities.IMP.get().create(this.level);
        imp.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
//        imp.finalizeSpawn(level, difficulty, spawnType, null, null);
        imp.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this));
        return imp;
    }

    public boolean doHurtTarget(Entity p_21372_) {
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (p_21372_ instanceof LivingEntity) {
            f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)p_21372_).getMobType());
            f1 += (float)EnchantmentHelper.getKnockbackBonus(this);
        }

        int i = EnchantmentHelper.getFireAspect(this);
        if (i > 0) {
            p_21372_.setSecondsOnFire(i * 4);
        }

        boolean flag = p_21372_.hurt(PVZDamageSource.gargantuarCrash(this), f);
        if (flag) {
            if (f1 > 0.0F && p_21372_ instanceof LivingEntity) {
                ((LivingEntity)p_21372_).knockback(f1 * 0.5F, Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(this.getYRot() * ((float)Math.PI / 180F)));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
            }

            if (p_21372_ instanceof Player) {
                Player player = (Player)p_21372_;
                this.maybeDisableShield(player, this.getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
            }

            this.doEnchantDamageEffects(this, p_21372_);
            this.setLastHurtMob(p_21372_);
        }

        return flag;
    }

    private void maybeDisableShield(Player target, ItemStack itemStack, ItemStack targetHolding) {
        if (!itemStack.isEmpty() && !targetHolding.isEmpty() && (itemStack.getItem() instanceof AxeItem || itemStack.is(PVZItemTags.GIANT_HAMMER)) && targetHolding.getItem() instanceof ShieldItem item) {
            float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
            if (this.random.nextFloat() < f) {
                target.getCooldowns().addCooldown(item, 100);
                this.level.broadcastEntityEvent(target, (byte)30);
            }
        }
    }

    @Override
    public void positionRider(Entity entity) {
        entity.setPos(this.getPosition(0).add(0, this.getPassengersRidingOffset() + entity.getMyRidingOffset() ,0).add(this.getLookAngle().multiply(1, 0, 1).normalize().scale(this.isBaby() ? -0.5 : -0.7)));
    }

    public static class GargantuarRiderGoal extends Goal {
        private final Gargantuar gargantuar;
        private short animCount;
        private GargantuarRiderGoal(Gargantuar gargantuar) {
            this.gargantuar = gargantuar;
            this.animCount = 0;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }
        @Override
        public boolean canUse() {
            if (this.gargantuar.getTarget() != null) {
                this.gargantuar.getPassengers().forEach(passenger -> {
                    if (passenger instanceof Mob) {
                        ((Mob) passenger).setTarget(gargantuar.getTarget());
                    }
                });
            }
            return ((gargantuar.isVehicle() && EntityUtil.isEntityValid(gargantuar.getTarget()) && gargantuar.getPose() == Pose.STANDING) || gargantuar.getPose() == Pose.USING_TONGUE) && gargantuar.getHealth() < gargantuar.getMaxHealth() / 2;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
        public void tick() {
            gargantuar.getNavigation().stop();
            if (animCount == 0) {
                gargantuar.setPose(Pose.USING_TONGUE);
            } else if (animCount == 32) {
                Entity entity = gargantuar.getFirstPassenger();
                if (EntityUtil.isEntityValid(entity)) {
                    entity.stopRiding();
                    double dist = EntityUtil.isEntityValid(gargantuar.getTarget()) ? gargantuar.getTarget().position().subtract(gargantuar.position())
                            .multiply(new Vec3(1, 0, 1)).distanceTo(Vec3.ZERO) : 8;
                    entity.setDeltaMovement(gargantuar.getLookAngle().normalize().scale(Math.min(dist / 5, 3)).add(0, 0.15, 0));
                }
            } else if (animCount >= 59) {
                gargantuar.setPose(Pose.STANDING);
                animCount = 0;
                return;
            }
            animCount ++;
        }
    }

    public static class GargantuarAttackGoal extends ZombieAttackGoal {
        private short animCount;
        public GargantuarAttackGoal(Zombie p_26019_, double p_26020_, boolean p_26021_) {
            super(p_26019_, p_26020_, p_26021_);
            this.animCount = -1;
        }
        @Override
        public boolean canUse() {
            if (animCount < -1) {
                animCount += 1;
            }

            boolean vanillaResult;
            LivingEntity target = mob.getTarget();
            if (target == null) {
                vanillaResult = false;
            } else if (!target.isAlive()) {
                vanillaResult = false;
            } else {
                Path path = this.mob.getNavigation().createPath(target, 0);
                if (path != null) {
                    vanillaResult = true;
                } else {
                    vanillaResult = this.getAttackReachSqr(target) >= this.mob.distanceToSqr(target);
                }
            }

            boolean flag = vanillaResult && (mob.getPose() == Pose.STANDING || mob.getPose() == Pose.SPIN_ATTACK);
            if (! flag && animCount >= 0) {
                animCount += 2;
            }
            if (animCount >= 49) {
                mob.setPose(Pose.STANDING);
                animCount = -5;
            }
            return flag;
        }
        public boolean canContinueToUse() {
            if (animCount < -1) {
                animCount += 1;
            }
            boolean flag = super.canContinueToUse() && (mob.getPose() == Pose.STANDING || mob.getPose() == Pose.SPIN_ATTACK);
            if (animCount >= 49) {
                mob.setPose(Pose.STANDING);
                animCount = -5;
            }
            return flag;
        }
        @Override
        public void tick() {
            super.tick();
            if (animCount >= 0) {
                animCount ++;
                mob.getNavigation().stop();
            }
            if (animCount == 1) {
                mob.setPose(Pose.SPIN_ATTACK);
            } else if (animCount == 26) {
                LivingEntity target = this.mob.getTarget();
                if (EntityUtil.isEntityValid(target)) {
                    double distance = mob.distanceToSqr(target);
                    double dis = this.getAttackReachSqr(target);
                    if (distance <= dis) {
                        if (mob.getMainHandItem().is(PVZItemTags.GIANT_HAMMER)) {
                            target.hurt(PVZDamageSource.ignoreInvTime(PVZDamageSource.gargantuarCrash(mob).bypassArmor()), (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2F);
                            List<Entity> list = mob.level.getEntities((Entity) null,
                                    new AABB(target.position().add(-0.8, 0, -0.8), target.position().add(0.8, 1, 0.8)),
                                    (entity -> entity instanceof LivingEntity));
                            list.forEach((entity) -> {
                                double distSqr = mob.distanceToSqr(entity);
                                double horizontalMovement = 2.5 / distSqr > 0.3 ? 2.5 / distSqr : 0;
                                horizontalMovement = horizontalMovement > 1 ? 1 : horizontalMovement;
                                AttributeInstance attribute = target.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                                double knockBackModifier = 0;
                                if (attribute != null) {
                                    knockBackModifier = attribute.getValue();
                                }
                                Vec3 vec3 = entity.position().subtract(mob.position()).multiply(1, 0, 1).normalize()
                                        .multiply(horizontalMovement, 0, horizontalMovement).add(0, 0.5, 0)
                                        .multiply(1 - knockBackModifier, 1 - knockBackModifier * 0.5, 1 - knockBackModifier);
                                if (entity instanceof ServerPlayer player) {
                                    PlayerKnockBackPacket.knockBack(player, vec3, true);
                                } else {
                                    entity.setDeltaMovement(entity.getDeltaMovement().add(vec3));
                                }
                            });
                            ((ServerLevel) mob.level).sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY(0.5D), target.getZ(), 5, 1, 0.0D, 1, 0.0D);
                        }
                        this.mob.doHurtTarget(target);
                    }
                }
            }
        }
        @Override
        protected double getAttackReachSqr(LivingEntity entity) {
            return mob.getBbWidth() * mob.getBbWidth() * (mob.getMainHandItem().is(PVZItemTags.GIANT_HAMMER) ? 4 : 2) + entity.getBbWidth() * entity.getBbWidth();
        }
        @Override
        protected void checkAndPerformAttack(LivingEntity target, double distance) {
            double d0 = this.getAttackReachSqr(target);
            if (distance <= d0 * 0.75 && animCount == -1) {
                this.mob.swing(InteractionHand.MAIN_HAND);
                animCount = 0;
            }
        }
    }
}
