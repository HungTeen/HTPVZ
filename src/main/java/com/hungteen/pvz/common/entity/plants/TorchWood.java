package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.block.PlanternLightBlock;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class TorchWood extends SimplePlant {
    public AnimationState idleAnimationState = new AnimationState();
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.torch_wood.soul_torch", PVZItems.IGNIS_ESSENCE, 8, 4, 50, 0),
            new Skill("skill.pvz.torch_wood.tough_bark", PVZItems.TERRA_ESSENCE, 8, 4, 50, 0)
    );

    public TorchWood(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    public boolean isSoulFire() {
        return hasSkill("skill.pvz.torch_wood.soul_torch") && level.getBlockState(getOnPos()).is(BlockTags.SOUL_SPEED_BLOCKS);
    }
    public boolean canBurn() {
        return true;
    }

    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 15D)
                .add(Attributes.ARMOR, 0D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.UNPLANTABLE_DIRT, PVZBlockTags.PLANTABLE_DIRT);
    }
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide && ! this.idleAnimationState.isStarted()) {
            this.idleAnimationState.start(this.tickCount);
        }
        if (level.isClientSide() && random.nextBoolean()) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    getX() - 0.5 + this.random.nextFloat(),
                    getY() + 0.8 + this.random.nextFloat() / 5,
                    getZ() - 0.5 + this.random.nextFloat(),
                    0, 0, 0);
        }
        if (hasSkill(this, "skill.pvz.torch_wood.tough_bark")) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(30D);
            this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(20D);
        }
        if (level.isClientSide() || ! this.canBurn()) {
            return ;
        } else if (level.getBlockState(getOnPos().above().above()).isAir()) {
            level.setBlock(getOnPos().above().above(),
                    PVZBlocks.PLANTERN_LIGHT.get().defaultBlockState(), 2);
        } else if (level.getBlockState(getOnPos().above().above()).is(Blocks.WATER)) {
            level.setBlock(getOnPos().above().above(),
                    PVZBlocks.PLANTERN_LIGHT.get().defaultBlockState().setValue(PlanternLightBlock.WATERLOGGED, true), 2);
        }
        if (level.getBlockState(getOnPos().above().above()).is(PVZBlocks.PLANTERN_LIGHT.get())) {
            level.setBlock(getOnPos().above().above(),
                    level.getBlockState(getOnPos().above().above()).setValue(PlanternLightBlock.HAS_SOURCE, true), 2);
        }
    }
    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
    }
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
        this.goalSelector.addGoal(1, new TorchWoodLitBulletGoal(this));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    public Predicate<Entity> canPush(){
        return entity -> true;
    }

    public static class TorchWoodLitBulletGoal extends Goal {
        TorchWood entity;
        public TorchWoodLitBulletGoal(TorchWood entity) {
            this.entity = entity;
        }
        @Override
        public boolean canUse() {
            return entity.canBurn();
        }
        @Override
        public void tick() {
            List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(1, 1, 1).move(0, 1.5, 0),
                    (entity) -> (entity instanceof PeaBullet && EntityUtil.isTeammate(entity, this.entity)));
            entities.forEach((entity) -> {
                if (entity instanceof PeaBullet pea && pea.changeCoolDown <= 0) {
                    if (pea.getPeaType() == PeaBullet.PeaType.SoulFire) {
                        return;
                    } else if (pea.getPeaType() == PeaBullet.PeaType.Common) {
                        pea.setAttackDamage(pea.getAttackDamage() + (this.entity.isSoulFire() ? 6 : 2F));
                    }
                    pea.setPeaType(pea.getPeaType() == PeaBullet.PeaType.Ice ? PeaBullet.PeaType.Common :
                            this.entity.isSoulFire() ? PeaBullet.PeaType.SoulFire : PeaBullet.PeaType.Fire);
                    pea.changeCoolDown = 5;
                }
            });
        }
    }
}
