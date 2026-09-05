package com.hungteen.pvz.mixin;

import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slime.SlimeAttackGoal.class)
public class SlimeAttackGoalMixin {

    @Final @Shadow private Slime slime;

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (! (slime.getMoveControl() instanceof Slime.SlimeMoveControl)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void canContinueToUse(CallbackInfo ci) {
        if (! (slime.getMoveControl() instanceof Slime.SlimeMoveControl)) {
            ci.cancel();
        }
    }
}
