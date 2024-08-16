package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.Fluids;

public class InvasionEntityModifiers {
    public static final ResourceLocation BABYLIZE = Util.prefix("babylize");
    public static final ResourceLocation ADD_LIFEBUOY = Util.prefix("add_lifebuoy");
    public static final ResourceLocation FINALIZE_SPAWN = Util.prefix("finalize_spawn");

    public static void babylize(Invasion invasion, Entity entity, int threat) {
        if (entity instanceof Mob) {
            ((Mob) entity).setBaby(true);
        }
    }
    public static void addLifeBuoy(Invasion invasion, Entity entity, int threat) {
        if (entity instanceof PathfinderMob mob && ! mob.getNavigation().canFloat() &&
                entity.level.getBlockState(entity.blockPosition()).getFluidState().is(Fluids.WATER)) {
            mob.setItemSlot(EquipmentSlot.FEET, PVZItems.DUCK_LIFEBUOY.get().getDefaultInstance());
        }
    }
    public static void finalizeSpawn(Invasion invasion, Entity entity, int threat) {
        if (entity instanceof Mob mob) {
            mob.finalizeSpawn(((ServerLevel) entity.level), entity.level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.EVENT, null, null);
        }
    }
}
