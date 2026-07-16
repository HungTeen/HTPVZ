package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Bee;

import java.util.EnumSet;
import java.util.List;

public class BeeGoToGardenPlantsGoal extends Goal {
    private final Bee bee;
    public BeeGoToGardenPlantsGoal(Bee bee) {
        this.bee = bee;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (bee.isAngry()) return false;
        if (bee.remainingCooldownBeforeLocatingNewFlower > 2) {
            return false;
        } else if (bee.hasNectar()) {
            return false;
        } else if (bee.level.isRaining()) {
            return false;
        } else {
            BlockPos plant = this.findNearbyGardenPlant();
            if (plant != null) {
                bee.savedFlowerPos = plant;
                bee.getNavigation().moveTo((double)bee.savedFlowerPos.getX() + 0.5D, (double)bee.savedFlowerPos.getY() + 0.5D, (double)bee.savedFlowerPos.getZ() + 0.5D, (double)1.2F);
                return true;
            }
        }
        return false;
    }

    public BlockPos findNearbyGardenPlant() {
        List<Entity> entities = bee.level.getEntities(bee
                , bee.getBoundingBox().inflate(8, 2, 8), entity -> entity instanceof IGardenPlant);
        if (! entities.isEmpty()) {
            int plant = entities.size() == 1 ? 0 : bee.getRandom().nextInt(entities.size());
            return entities.get(plant).blockPosition();
        }
        return null;
    }

    public boolean canContinueToUse() {
        if (bee.isAngry()) return false;
        if (! bee.beePollinateGoal.isPollinating()) {
            return false;
        } else if (!bee.hasSavedFlowerPos()) {
            return false;
        } else if (bee.level.isRaining()) {
            return false;
        } else if (bee.beePollinateGoal.hasPollinatedLongEnough()) {
            return bee.getRandom().nextFloat() < 0.2F;
        } else if (bee.tickCount % 20 == 0 && ! bee.isFlowerValid(bee.savedFlowerPos)) {
            bee.savedFlowerPos = null;
            return false;
        } else {
            return true;
        }
    }

    public void start() {
        bee.beePollinateGoal.start();
    }

    public void tick() {
        bee.beePollinateGoal.tick();
    }

    public void stop() {
        bee.beePollinateGoal.stop();
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
