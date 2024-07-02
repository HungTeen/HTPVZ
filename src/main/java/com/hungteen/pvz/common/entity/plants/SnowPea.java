package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

public class SnowPea extends PeaShooter{
    public int timeOverheat;
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.snow_pea.never_melt_ice", PVZItems.GELUM_ESSENCE, 6, 6, 75, 0)
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
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    protected PeaBullet createBullet() {
        PeaBullet bullet = new PeaBullet(this.level, this, PeaBullet.PeaType.Common);
        bullet.setPeaType(PeaBullet.PeaType.Ice);
        if (this.hasSkill("skill.pvz.snow_pea.never_melt_ice")) {
            bullet.neverMelt = true;
        }
        return bullet;
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

        if (this.timeOverheat > 300 && ForgeEventFactory.canLivingConvert(this, PVZEntities.PEA_SHOOTER.get(), (timer) -> this.timeOverheat = timer)) {
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
        return hasSkill("skill.pvz.snow_pea.never_melt_ice") || super.fireImmune();
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
}
