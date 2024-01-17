package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.api.interfaces.IDefenderPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.hungteen.pvz.common.world.PVZDamageSource.teamFilter;

public class WallNut extends SimplePlant implements IDefenderPlant {
    float storedHealth;
    public static final EntityDataAccessor<Integer> EXPLODE_COUNT = SynchedEntityData.defineId(WallNut.class, EntityDataSerializers.INT);
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.wall_nut.explode", PVZItems.IGNIS_ESSENCE, 3, 8, 150, 250),
            new Skill("skill.pvz.wall_nut.wall_nut_first_aid", PVZItems.ORIGIN_ESSENCE, 4, 4, 0, 0)
    );

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
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.ARMOR, 25D)
                .add(Attributes.ARMOR_TOUGHNESS, 20D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    public Predicate<Entity> canPush(){
        return entity -> true;
    }

    @Override
    public MutableComponent isVehicleSafe(Entity target, boolean actuallyPlant) {
        if (hasSkill(this, "skill.pvz.wall_nut.wall_nut_first_aid") && target != null && target.getClass() == this.getClass()) {
            if (PVZOwnedCapability.isTeammate(this, target)) {
                if (((WallNut) target).getHealth() > ((WallNut) target).getMaxHealth() * 0.67) {
                    return Component.translatable("hint.pvz.plant.wall_nut.not_broken");
                }
                if (actuallyPlant) {
                    moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                    yBodyRot = ((WallNut) target).yBodyRot;
                    if (target.hasCustomName()) {
                        setCustomName(target.getCustomName());
                        setCustomNameVisible(target.isCustomNameVisible());
                    }
                    setInvulnerable(target.isInvulnerable());
                    target.discard();
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        }
        return super.isVehicleSafe(target, actuallyPlant);
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
        if (this.hasSkill(this, "skill.pvz.wall_nut.explode") && this.getEntityData().get(EXPLODE_COUNT) > -1) {
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
            this.discard();
        }
    }

    @Override
    public void actuallyHurt(DamageSource dmgSource, float dmg) {
        super.actuallyHurt(dmgSource, dmg);
        if (this.hasSkill(this, "skill.pvz.wall_nut.explode") && this.getHealth() <= 0) {
            this.setHealth(0.1F);
            this.getEntityData().set(EXPLODE_COUNT, this.getEntityData().get(EXPLODE_COUNT) == -1 ? 0 : this.getEntityData().get(EXPLODE_COUNT));
        }
    }

    public static boolean checkSpawnRules(EntityType<? extends LivingEntity> entityType, ServerLevelAccessor level, MobSpawnType mobSpawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(PVZBlockTags.PLANTABLE_BLOCKS);
    }

}
