package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.EntityLifter;
import com.hungteen.pvz.common.entity.zombies.TacoImp;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.world.PVZFog;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class InvasionEntityModifiers {
    private static final Random random = new Random();
    public static final ResourceLocation BABYLIZE = Util.prefix("babylize");
    public static final ResourceLocation ADD_LIFEBUOY = Util.prefix("add_lifebuoy");
    public static final ResourceLocation FINALIZE_SPAWN = Util.prefix("finalize_spawn");
    public static final ResourceLocation WITH_FOG = Util.prefix("with_fog");
    public static final ResourceLocation WITH_TACO = Util.prefix("with_taco");

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

    public static void withTaco(@Nullable Invasion invasion, Entity entity, int threat) {
        if (invasion == null || ! EntityUtil.isEntityValid(invasion.target)) {
            return;
        }
        if (invasion.currentWave > invasion.waves.size() / 3 && invasion.getCurrentWave().isBigWave &&
                threat > 100 && random.nextInt(invasion.getCurrentWave().threat) < threat) {
            TacoImp entity1 = PVZEntities.TACO_IMP.get().create(invasion.level);
            Vec3 pos = entity.position().add(0, entity.getBbHeight(), 0);
            EntityLifter lifter = PVZEntities.ENTITY_LIFTER.get().create(invasion.level);
            lifter.setPos(pos);
            entity1.setPos(pos.add(0, - entity1.getBbHeight(), 0));
            entity1.getRootVehicle().startRiding(lifter);
            entity1.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10000));
            invasion.types.forEach(type -> type.getModifiers().forEach(
                    modifier -> modifier.accept(invasion, entity, 0)));
            ((ServerLevel) invasion.level).addFreshEntityWithPassengers(lifter);
            entity1.finalizeSpawn((ServerLevel) invasion.level, invasion.level.getCurrentDifficultyAt(new BlockPos(pos)), MobSpawnType.EVENT, null, null);
            entity1.setTarget(invasion.target);
        }
    }
}
