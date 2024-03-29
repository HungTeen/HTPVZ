package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public class GoldBloom extends SimplePlant {
    public AnimationState idleAnimationState = new AnimationState();
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.gold_bloom.sun_transporter", PVZItems.LUX_ESSENCE, 4, 6, 300, -1140)
    );

    public GoldBloom(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.idleAnimationState.start(this.tickCount);
        this.entityData.set(root(), false);
    }

    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return ShooterPlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D);
    }
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
        GoldBloom jalapeno;
        public GoldBloomExplodeGoal(GoldBloom jalapeno) {
            this.jalapeno = jalapeno;
        }
        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            if (jalapeno.tickCount == 16 || jalapeno.tickCount == 17) {
                Sun.spawnSunWithEffects(this.jalapeno.level, 50, this.jalapeno.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 25, this.jalapeno.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 5, this.jalapeno.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 5, this.jalapeno.getOnPos().above(), 0.4F);
            }
            if (jalapeno.tickCount == 29 || jalapeno.tickCount == 30) {
                Sun.spawnSunWithEffects(this.jalapeno.level, 50, this.jalapeno.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 25, this.jalapeno.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 5, this.jalapeno.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 5, this.jalapeno.getOnPos().above(), 0.4F);
            }
            if (jalapeno.tickCount == 40 || jalapeno.tickCount == 41) {
                Sun.spawnSunWithEffects(this.jalapeno.level, 50, this.jalapeno.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 25, this.jalapeno.getOnPos().above(), 0.3F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 25, this.jalapeno.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 15, this.jalapeno.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 15, this.jalapeno.getOnPos().above(), 0.4F);
                Sun.spawnSunWithEffects(this.jalapeno.level, 10, this.jalapeno.getOnPos().above(), 0.4F);
            }
            if (jalapeno.tickCount > 80) {
                jalapeno.discard();
            }
        }
    }
}
