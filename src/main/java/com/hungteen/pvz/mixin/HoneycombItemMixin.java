package com.hungteen.pvz.mixin;

import com.hungteen.pvz.common.block.CopperLadderBlock;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(HoneycombItem.class)
public abstract class HoneycombItemMixin {

    @Inject(method = "getWaxed", at = @At("HEAD"), cancellable = true)
    private static void pvz$getWaxed(BlockState state, CallbackInfoReturnable<Optional<BlockState>> cir) {
        Optional<BlockState> waxed = CopperLadderBlock.getWaxedState(state);
        if (waxed.isPresent()) {
            cir.setReturnValue(waxed);
        }
    }
}
