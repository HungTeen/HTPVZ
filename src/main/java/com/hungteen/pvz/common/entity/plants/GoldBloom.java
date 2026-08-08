package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.List;

public class GoldBloom extends SimplePlant {
    public AnimationState explodeAnimationState = new AnimationState();
    public AnimationState idleAnimationState = new AnimationState();
    public static final String TRANSPORTER_SKILL_NAME = "skill.pvz.gold_bloom.sun_transporter";
    public static List<Skill> staticSkillList = List.of(
            new Skill(TRANSPORTER_SKILL_NAME, PVZItems.LUX_ESSENCE, 4, 6, 300, PVZSeedPackets.VERY_SLOW - PVZSeedPackets.VERY_FAST)
    );

    public void setupPresentationAnim() {
        this.explodeAnimationState.stop();
        this.idleAnimationState.start(this.tickCount);
    }

    public GoldBloom(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.entityData.set(root(), false);
        this.explodeAnimationState.start(this.tickCount);
    }

    //entity settings
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new GoldBloomExplodeGoal(this));
    }

    public static class GoldBloomExplodeGoal extends Goal {
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
                goldBloom.level.playSound(null, goldBloom, PVZSoundEvents.GOLD_BLOOM_PRODUCE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 50, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 25, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 5, this.goldBloom.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 5, this.goldBloom.getOnPos().above(), 0.4F);
            }
            if (goldBloom.tickCount == 29 || goldBloom.tickCount == 30) {
                goldBloom.level.playSound(null, goldBloom, PVZSoundEvents.GOLD_BLOOM_PRODUCE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 50, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 25, this.goldBloom.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 5, this.goldBloom.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.goldBloom.level, 5, this.goldBloom.getOnPos().above(), 0.4F);
            }
            if (goldBloom.tickCount == 40 || goldBloom.tickCount == 41) {
                goldBloom.level.playSound(null, goldBloom, PVZSoundEvents.GOLD_BLOOM_PRODUCE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
