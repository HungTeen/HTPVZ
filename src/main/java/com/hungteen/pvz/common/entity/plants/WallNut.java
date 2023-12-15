package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.PVZPlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;
import java.util.function.Predicate;

import static com.hungteen.pvz.common.world.PVZDamageHandler.teamFilter;

public class WallNut extends PVZPlant {
    float storedHealth;
    public static final EntityDataAccessor<Integer> EXPLODE_COUNT = SynchedEntityData.defineId(WallNut.class, EntityDataSerializers.INT);

    static {
        staticSkillSet.add(
                new Skill("skill.pvz.wall_nut.explode", PVZItems.IGNIS_ESSENCE, 3, 8, 150, 250)
        );
    }
    public WallNut(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        storedHealth = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EXPLODE_COUNT, -1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PVZPlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.ARMOR, 25D)
                .add(Attributes.ARMOR_TOUGHNESS, 20D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    //deliberate.
    public Predicate<Entity> canPush(){
        return entity -> true;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (getHealth() < storedHealth && level.isClientSide()) {
            for (int i = 0; i < 3; i ++) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BIRCH_PLANKS.defaultBlockState()).setPos(this.getOnPos()),
                    getX()+random.nextFloat() - 0.5, getY() + 1.1, getZ()+random.nextFloat() - 0.5, 0, 0, 0);
            }
        }
        storedHealth = getHealth();
        if (this.hasSkill(this, 0) && this.getEntityData().get(EXPLODE_COUNT) > -1) {
            this.getEntityData().set(EXPLODE_COUNT, this.getEntityData().get(EXPLODE_COUNT) + 1);
            if (this.getEntityData().get(EXPLODE_COUNT) > 40) {
                this.explode();
            }
        }
    }

    private void explode() {
        if (!this.level.isClientSide) {
            this.dead = true;
            level.explode(this, teamFilter(DamageSource.explosion(this)), null, this.getX(), this.getY(), this.getZ(), 3F, false, Explosion.BlockInteraction.NONE);
            //TODO bug of hurting friendly mobs.
            this.discard();
        }
    }

    @Override
    public void actuallyHurt(DamageSource dmgSource, float dmg) {
        super.actuallyHurt(dmgSource, dmg);
        if (this.hasSkill(this, 0) && this.getHealth() <= 0) {
            this.setHealth(0.1F);
            this.getEntityData().set(EXPLODE_COUNT, this.getEntityData().get(EXPLODE_COUNT) == -1 ? 0 : this.getEntityData().get(EXPLODE_COUNT));
        }
    }

}
