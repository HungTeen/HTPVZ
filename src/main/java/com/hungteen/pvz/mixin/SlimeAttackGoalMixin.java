package com.hungteen.pvz.mixin;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//TODO This mixin is not in use. Activate this and delete limit of chomper tracking slime.
@Mixin(Slime.SlimeAttackGoal.class)
public abstract class SlimeAttackGoalMixin extends Goal {

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) instanceof Slime.SlimeAttackGoal goal && ! (goal.slime.getMoveControl() instanceof Slime.SlimeMoveControl)) cir.setReturnValue(false);
    }
}
