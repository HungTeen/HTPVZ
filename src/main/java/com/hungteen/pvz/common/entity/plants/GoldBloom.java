package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.List;

public class GoldBloom extends SimplePlant {
    public AnimationState explodeAnimationState = new AnimationState();
    public AnimationState idleAnimationState = new AnimationState();
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.gold_bloom.sun_transporter", PVZItems.LUX_ESSENCE, 4, 6, 300, -1140)
    );

    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
    }

    public GoldBloom(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.explodeAnimationState.start(this.tickCount);
        this.entityData.set(root(), false);
    }

    //entity settings
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new GoldBloomExplodeGoal(this));
    }

    public static class GoldBloomExplodeGoal extends Goal{
        GoldBloom goldBloom;
        public GoldBloomExplodeGoal(GoldBloom goldBloom) {
            this.goldBloom = goldBloom;
        }
        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            if (goldBloom.tickCount == 16 || goldBloom.tickCount == 17) {
                Sun.spawnSunWithEffects(this.goldBloom.level, 50, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 25, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 5, this.goldBloom.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 5, this.goldBloom.getOnPos().above(), 0.4F);
            }
            if (goldBloom.tickCount == 29 || goldBloom.tickCount == 30) {
                Sun.spawnSunWithEffects(this.goldBloom.level, 50, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 25, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 5, this.goldBloom.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 5, this.goldBloom.getOnPos().above(), 0.4F);
            }
            if (goldBloom.tickCount == 40 || goldBloom.tickCount == 41) {
                Sun.spawnSunWithEffects(this.goldBloom.level, 50, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 25, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 25, this.goldBloom.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 15, this.goldBloom.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 15, this.goldBloom.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 10, this.goldBloom.getOnPos().above(), 0.4F);
            }
            if (goldBloom.tickCount > 80) {
                goldBloom.discard();
            }
        }
    }
}
