package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
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
public class TallNut extends WallNut{
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.wall_nut.wall_nut_first_aid", PVZItems.LUX_ESSENCE, 4, 4, 0, 0),
            new Skill("skill.pvz.wall_nut.iron_armor", PVZItems.TERRA_ESSENCE, 4, 8, 75, 0),
            new Skill("skill.pvz.tall_nut.projectile_protection", PVZItems.VENTUS_ESSENCE, 8, 8, 50, 0)
    );
    public TallNut(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 50D)
                .add(Attributes.ARMOR, 60D)
                .add(Attributes.ARMOR_TOUGHNESS, 30D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    public void setupPresentationAnim() {
    }
    public boolean canBowling() {return false;}

    public float getMaxIronArmor() {
        return 300;
    }

    @SubscribeEvent
    public static void reboundProjectiles(ProjectileImpactEvent ev) {
        if (ev.getRayTraceResult() != null) {
            HitResult result = ev.getRayTraceResult();
            if (result.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) result).getEntity();
                Projectile projectile = ev.getProjectile();
                if (entity instanceof TallNut nut && nut.random.nextBoolean() && nut.hasSkill("skill.pvz.tall_nut.projectile_protection") && EntityUtil.checkCanEntityBeAttack(nut, projectile.getOwner())) {
                    projectile.setDeltaMovement(projectile.getDeltaMovement().multiply(-0.5, 1, -0.5));
                    projectile.setOwner(nut);
                    ev.setCanceled(true);
                }
            }
        }
    }
}
