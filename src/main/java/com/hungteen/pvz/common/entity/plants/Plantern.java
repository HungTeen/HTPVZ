package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.block.EntityLightBlock;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class Plantern extends SimplePlant {
    public static final String LEAD_SKILL_NAME = "skill.pvz.plantern.light_house";
    public static final String DETECT_SKILL_NAME = "skill.pvz.plantern.lantern_radar";
    public static List<Skill> staticSkillList = List.of(
            new Skill(LEAD_SKILL_NAME, PVZItems.LUX_ESSENCE, 8, 8, 0, 350),
            new Skill(DETECT_SKILL_NAME, PVZItems.LUX_ESSENCE, 8, 8, 125, 350).avoidSkills(LEAD_SKILL_NAME)
    );
    public AnimationState idleAnimationState = new AnimationState();
    private int skillGlowTime = 0; //only available in client.

    public Plantern(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.idleAnimationState.start(this.tickCount);
    }
    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
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
        this.goalSelector.addGoal(1, new OfferBrightnessGoal(this));
        this.goalSelector.addGoal(1, new PlanternSearchEnemyGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(2, new AxisLookAroundGoal(this));
    }
    @Override
    public void tick() {
        super.tick();
        BlockPos pos = getOnPos().offset(0, Math.round(this.getBbHeight()), 0);
        if (level.isClientSide()) {
            this.skillGlowTime = Math.max(0, -- skillGlowTime);
            return ;
        } else if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, PVZBlocks.ENTITY_LIGHT.get().defaultBlockState()
                    .setValue(EntityLightBlock.LEVEL, 15), 2);
        } else if (level.getBlockState(pos).is(Blocks.WATER)) {
            level.setBlock(pos, PVZBlocks.ENTITY_LIGHT.get().defaultBlockState()
                    .setValue(EntityLightBlock.WATERLOGGED, true).setValue(EntityLightBlock.LEVEL, 15), 2);
        }
        if (level.getBlockState(pos).is(PVZBlocks.ENTITY_LIGHT.get())) {
            level.setBlock(pos, level.getBlockState(pos).setValue(EntityLightBlock.HAS_SOURCE, true).setValue(EntityLightBlock.LEVEL, 15), 2);
        }
        if (hasSkill(this, LEAD_SKILL_NAME)) {
            this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(0D); //can't attract enemy with lightHouse skill.
        }
        if (! level.isClientSide && hasSkill(LEAD_SKILL_NAME)) {
            this.setPose(this.getPose() == Pose.STANDING ? Pose.DIGGING : Pose.STANDING); // to refresh dimensions.
        }
    }
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (this.hasSkill(LEAD_SKILL_NAME)) {
            EntityDimensions dimensions = super.getDimensions(pose);
            float height = 2;
            while (height < 5 && height < this.getBbHeight() + 0.5) {
                BlockPos pos = this.blockPosition().offset(new Vec3i(0, (int) height, 0));
                if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                    height += 0.25;
                } else {
                    break;
                }
            }
            return new EntityDimensions(dimensions.width, height, dimensions.fixed);
        }
        return super.getDimensions(pose);
    }
    public void refreshSkillGlowTime() {
        this.skillGlowTime = 5;
    }
    public boolean isCurrentlyGlowing() {
        return this.skillGlowTime > 0 || super.isCurrentlyGlowing();
        //To avoid server crash the part of testing player position is not put here.
    }

    public static class OfferBrightnessGoal extends Goal {
        Entity entity;
        int waitTick;
        public OfferBrightnessGoal(Entity entity) {
            super();
            this.entity = entity;
            waitTick = 0;
        }

        @Override
        public boolean canUse() {
            if (waitTick ++ > 20) {
                waitTick = 0;
                List<LivingEntity> list = entity.level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                        new AABB(entity.getX() - 8, entity.getY() - 3, entity.getZ() - 8,
                                entity.getX() + 8, entity.getY() + 3, entity.getZ() + 8),
                        (player) -> EntityUtil.isTeammate(player, entity));
                list.forEach((player -> player.addEffect(new MobEffectInstance(PVZMobEffects.BRIGHTNESS.get(), 100, 0, false, false), this.entity)));
                List<LivingEntity> list2 = entity.level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                        new AABB(entity.getX() - 8, entity.getY() - 3, entity.getZ() - 8,
                                entity.getX() + 8, entity.getY() + 3, entity.getZ() + 8),
                        (entity) -> ! EntityUtil.isTeammate(entity, this.entity) && entity.hasEffect(MobEffects.INVISIBILITY));
                list2.forEach((entity -> entity.removeEffect(MobEffects.INVISIBILITY)));
            }
            return false;
        }
    }
    public static class PlanternSearchEnemyGoal extends Goal {
        private final Plantern plantern;
        public double angle;
        public PlanternSearchEnemyGoal(Plantern plantern) {
            this.plantern = plantern;
            angle = (plantern.random.nextFloat() - 0.5) * 2 * Math.PI;
        }
        @Override
        public boolean canUse() {
            return plantern.hasSkill(DETECT_SKILL_NAME);
        }
        @Override
        public void tick() {
            angle = angle + 0.1 > Math.PI ? angle + 0.1 - 2 * Math.PI : angle + 0.1;
            plantern.level.getEntities(this.plantern, this.plantern.getBoundingBox().inflate(24, 10, 24), (entity) -> {
                if (! EntityUtil.checkCanEntityBeAttack(this.plantern, entity)) return false;
                double entityAngle = Math.atan2(entity.getZ() - plantern.getZ(), entity.getX() - plantern.getX());
                return Math.abs(angle - entityAngle) < 0.5;
            }).forEach((entity) -> {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 15));
                }
            });
        }
    }
}
