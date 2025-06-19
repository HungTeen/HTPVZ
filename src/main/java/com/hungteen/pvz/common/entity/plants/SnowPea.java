package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.ai.goal.ShooterTargetGoal;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;
import java.util.function.Predicate;

public class SnowPea extends PeaShooter {
    public int timeOverheat;
    public static String NEWVER_MELT_ICE_SKILL_NAME = "skill.pvz.snow_pea.never_melt_ice";
    public static List<Skill> staticSkillList = List.of(
            new Skill(PeaShooter.PUNCH_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 8, 4, 50, 0),
            new Skill(NEWVER_MELT_ICE_SKILL_NAME, PVZItems.GELUM_ESSENCE, 6, 6, 75, 0)
    );
    public SnowPea(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
        timeOverheat = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PeaShooter.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 4D);
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }

    @Override
    protected PeaBullet createBullet() {
        PeaBullet bullet = new PeaBullet(this.level, this, PeaBullet.PeaType.Common);
        bullet.setPeaType(PeaBullet.PeaType.Ice);
        if (this.hasSkill(NEWVER_MELT_ICE_SKILL_NAME)) {
            bullet.neverMelt = true;
        }
        return bullet;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.removeGoal(targetGoal);
        this.targetGoal = new SnowPeaTargetGoal(this);
        this.targetSelector.addGoal(1, targetGoal);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }
    }

    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isConverting()) {
            ++this.timeOverheat;
        } else {
            this.timeOverheat = 0;
        }

        if (this.timeOverheat > 1200 && ForgeEventFactory.canLivingConvert(this, PVZEntities.PEA_SHOOTER.get(), (timer) -> this.timeOverheat = timer)) {
//            this.playConvertedSound();TODO add sound.
            PeaShooter peaShooter = convertTo(PVZEntities.PEA_SHOOTER.get(), true);
            if (peaShooter != null) {
                if (this.hasCustomName()) {
                    peaShooter.setCustomName(this.getCustomName());
                }
                this.getCapability(PVZEntityCapability.CAP).ifPresent((cap) ->
                        peaShooter.getCapability(PVZEntityCapability.CAP).ifPresent(cap1 -> cap1.setOwner(cap.getOwner())));
                peaShooter.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, peaShooter);
            }
        }

    }

    public boolean isConverting() {
        return this.level.dimensionType().ultraWarm() && !this.fireImmune() && !this.isNoAi();
    }
    @Override
    public boolean fireImmune() {
        return hasSkill(NEWVER_MELT_ICE_SKILL_NAME) || super.fireImmune();
    }
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source == DamageSource.FREEZE || super.isInvulnerableTo(source);
    }
    @Override
    public boolean canFreeze() {
        return false;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TimeOverheat", timeOverheat);

    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TimeOverheat")) {
            timeOverheat = tag.getInt("TimeOverheat");
        }
    }

    public static class SnowPeaTargetGoal extends ShooterTargetGoal {

        public SnowPeaTargetGoal(ShooterPlant mobIn) {
            super(mobIn);
        }

        @Override
        protected void findTarget() {
            Predicate<Entity> storedPredicate = this.predicate;
            this.predicate = this.predicate.and(target -> target.getTicksFrozen() <= 0);
            super.findTarget();
            this.predicate = storedPredicate;
            if (this.target == null) {
                super.findTarget();
            }
        }
    }
}
