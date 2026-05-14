package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class TallNut extends WallNut {
    public static final String PROJ_PROTECTION_SKILL_NAME = "skill.pvz.tall_nut.projectile_protection";
    public static final String VINE_SKILL_NAME = "skill.pvz.tall_nut.vine_nut";
    public static List<Skill> staticSkillList = List.of(
            new Skill(FIRST_AID_SKILL_NAME, PVZItems.ORIGIN_ESSENCE, 4, 1, 0, 0),
            new Skill(WallNut.ARMOR_SKILL_NAME, PVZItems.TERRA_ESSENCE, 4, 6, 75, 0),
            new Skill(VINE_SKILL_NAME, PVZItems.TERRA_ESSENCE, 8, 6, 100, 0).avoidSkills(WallNut.ARMOR_SKILL_NAME),
            new Skill(PROJ_PROTECTION_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 8, 8, 50, 0).avoidSkills(VINE_SKILL_NAME)
    );
    public TallNut(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 120D)
                .add(Attributes.ARMOR, 80D)
                .add(Attributes.ARMOR_TOUGHNESS, 40D)
                .add(Attributes.FOLLOW_RANGE, 3D);
    }
    public boolean canBowling() {
        return false;
    }

    public float getMaxIronArmor() {
        return 400;
    }

    @Override
    public boolean hurt(DamageSource source, float dmgNum) {
        if (this.hasSkill(VINE_SKILL_NAME) && ! EntityUtil.isTeammate(this, source.getEntity()) && ! (source instanceof IndirectEntityDamageSource)) {
            if (source.getEntity() != null) source.getEntity().hurt(PVZDamageSource.tallNutThornsDamage(this, source.getEntity()), Math.min(dmgNum, 2));
        }
        return super.hurt(source, dmgNum);
    }

    @SubscribeEvent
    public static void reboundProjectiles(ProjectileImpactEvent ev) {
        if (ev.getRayTraceResult() != null) {
            HitResult result = ev.getRayTraceResult();
            if (result.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) result).getEntity();
                Projectile projectile = ev.getProjectile();
                if (entity instanceof TallNut nut && nut.random.nextBoolean() && nut.hasSkill(PROJ_PROTECTION_SKILL_NAME) && EntityUtil.checkCanEntityBeAttack(nut, projectile.getOwner())) {
                    projectile.setDeltaMovement(projectile.getDeltaMovement().multiply(-0.5, 1, -0.5));
                    projectile.setOwner(nut);
                    ev.setCanceled(true);
                }
            }
        }
    }
}
