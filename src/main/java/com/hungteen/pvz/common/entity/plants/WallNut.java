package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.PVZPlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;
import java.util.function.Predicate;

public class WallNut extends PVZPlant {
    float storedHealth;

    static {
        staticSkillSet.add(
                new Skill("skill.pvz.explosive_nut", PVZItems.IGNIS_ESSENCE, 3, 8, 150, 0)
        );
    }
    public WallNut(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        storedHealth = 0;
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
        this.goalSelector.addGoal(3, new RandomPerpendicularlyLookAroundGoal(this));
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
    }

    public static class RandomPerpendicularlyLookAroundGoal extends RandomLookAroundGoal {
        private final Mob mob;
        private double relX;
        private double relZ;
        private int lookTime;

        public RandomPerpendicularlyLookAroundGoal(Mob p_25720_) {
            super(p_25720_);
            this.mob = p_25720_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public void start() {
            double d0 = (Math.PI * 0.5D) * this.mob.getRandom().nextInt(4);
            this.relX = Math.cos(d0);
            this.relZ = Math.sin(d0);
            this.lookTime = 20 + this.mob.getRandom().nextInt(20);
        }

        public boolean canContinueToUse() {
            return this.lookTime >= 0;
        }

        @Override
        public void tick() {
            --this.lookTime;
            this.mob.getLookControl().setLookAt(this.mob.getX() + this.relX, this.mob.getEyeY(), this.mob.getZ() + this.relZ);
        }
    }
}
