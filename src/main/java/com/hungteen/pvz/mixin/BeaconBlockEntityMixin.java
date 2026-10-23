package com.hungteen.pvz.mixin;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {

//    @Inject(method = "applyEffects", at = @At("TAIL"))
//    private static void pvz$applyEffects(Level level, BlockPos pos, int beaconLevel,
//                                         MobEffect primary, MobEffect secondary, CallbackInfo ci) {
//        if (level.isClientSide || primary == null) {
//            return;
//        }
//        AABB aabb = new AABB(pos).inflate(beaconLevel * 10 + 10)
//                .expandTowards(0, level.getHeight(), 0);
//        int duration = 100 * beaconLevel;
//        int primaryAmplifier = (beaconLevel >= 4 && primary == secondary) ? 1 : 0;
//        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
//            if (entity instanceof IPlant) {
//                entity.addEffect(new MobEffectInstance(primary, duration, primaryAmplifier, true, true));
//                if (beaconLevel >= 4 && primary != secondary && secondary != null) {
//                    entity.addEffect(new MobEffectInstance(secondary, duration, 0, true, true));
//                }
//            }
//        }
//    }
}
