package com.hungteen.pvz.mixin;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(targets = {"net.minecraft.world.entity.animal.Bee$BeePollinateGoal"})
public class BeePollinateGoalMixin {

    @Shadow(aliases = {"this$0"}) private Bee bee;

    @Inject(method = "findNearbyFlower", at = @At("RETURN"), cancellable = true)
    public void findNearbyFlower(CallbackInfoReturnable<Optional<BlockPos>> cir) {
        if (cir.getReturnValue().isEmpty() || bee.getRandom().nextBoolean()) {
            List<Entity> entities = bee.level.getEntities(bee
                    , bee.getBoundingBox().inflate(8, 2, 8), entity -> entity instanceof IGardenPlant);
            if (! entities.isEmpty()) {
                int plant = entities.size() == 1 ? 0 : bee.getRandom().nextInt(entities.size());
                cir.setReturnValue(Optional.of(entities.get(plant).blockPosition()));
            }
        }
    }
}