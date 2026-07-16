package com.hungteen.pvz.mixin;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Bee.class)
public class BeeMixin {

    @Inject(method = "isFlowerValid", at = @At("HEAD"), cancellable = true)
    private void isFlowerValid(BlockPos p_27897_, CallbackInfoReturnable<Boolean> cir) {
        if (p_27897_ == null) cir.setReturnValue(false);
        Bee bee = (Bee) (Object) this;
        if (! bee.level.getEntities(bee
                , AABB.of(new BoundingBox(p_27897_.getX(), p_27897_.getY(), p_27897_.getZ(), p_27897_.getX() + 1, p_27897_.getY() + 1, p_27897_.getZ() + 1))
                , entity -> entity instanceof IGardenPlant).isEmpty()) cir.setReturnValue(true);
    }
}
