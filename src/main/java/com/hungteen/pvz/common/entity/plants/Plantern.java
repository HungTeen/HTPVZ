package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.block.PlanternLightBlock;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.plantern.land_mark", PVZItems.LUX_ESSENCE, 8, 12, 150, 250),
            new Skill("skill.pvz.plantern.lantern_radar", PVZItems.LUX_ESSENCE, 8, 12, 0, 0).avoidSkills((short) 0)
    );
    public AnimationState idleAnimationState = new AnimationState();

    public Plantern(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.idleAnimationState.start(this.tickCount);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
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
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(2, new AxisLookAroundGoal(this));
    }
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
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
                List<Player> list = entity.level.getEntities(EntityTypeTest.forClass(Player.class),
                        new AABB(entity.getX() - 4, entity.getY() - 4, entity.getZ() - 4,
                                entity.getX() + 4, entity.getY() + 4, entity.getZ() + 4),
                        (player) -> PVZOwnedCapability.isTeammate(player, entity));
                list.forEach((player -> player.addEffect(new MobEffectInstance(PVZMobEffects.BRIGHTNESS.get(), 100), this.entity)));
            }
            return false;
        }
    }

}
