package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.world.PVZFog;
import com.hungteen.pvz.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class InvasionEntityModifiers {
    public static final ResourceLocation BABYLIZE = Util.prefix("babylize");
    public static final ResourceLocation ADD_LIFEBUOY = Util.prefix("add_lifebuoy");
    public static final ResourceLocation FINALIZE_SPAWN = Util.prefix("finalize_spawn");
    public static final ResourceLocation WITH_FOG = Util.prefix("with_fog");

    public static void babylize(@Nullable Invasion invasion, Entity entity, int threat) {
        if (entity instanceof Mob mob && ! mob.isBaby()) {
            mob.setBaby(true);
            mob.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("babylize", -0.4, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }
    public static void addLifeBuoy(@Nullable Invasion invasion, Entity entity, int threat) {
        if (entity instanceof PathfinderMob mob && ! mob.getNavigation().canFloat() &&
                entity.level.getBlockState(entity.blockPosition()).getFluidState().is(Fluids.WATER)) {
            mob.setItemSlot(EquipmentSlot.LEGS, PVZItems.DUCK_LIFEBUOY.get().getDefaultInstance());
        }
    }
    public static void finalizeSpawn(@Nullable Invasion invasion, Entity entity, int threat) {
        if (entity instanceof Mob mob) {
            mob.finalizeSpawn(((ServerLevel) entity.level), entity.level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.EVENT, null, null);
        }
    }
    public static void withFog(@Nullable Invasion invasion, Entity entity, int threat) {
        if (invasion == null) {
            return;
        }
        PVZFog fog = PVZFog.getFog(invasion.uuid);
        if (fog == null) {
            PVZFog.addFog(invasion.level.dimension().location(), Vec3.atCenterOf(invasion.position), 100, ((double) invasion.invasionLevel / 5 + 1), invasion.range, invasion.uuid);
        } else {
            fog.lifeLeft = 100;
            fog.position = Vec3.atCenterOf(invasion.position);
        }
    }
}
