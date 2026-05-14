package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.world.PVZFog;
import com.hungteen.pvz.generator.InvasionTypeGen;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InvasionEntityModifiers {
    private static Pair<CompoundTag, Integer> TACO = Pair.of(InvasionTypeGen.EntityBuilder
            .of(PVZEntities.TACO_IMP.get())
            .effect(new MobEffectInstance(MobEffects.GLOWING, 400))
            .get(), 0);
    private static final Random random = new Random();
    public static final ResourceLocation BABYLIZE = Util.prefix("babylize");
    public static final ResourceLocation ADD_LIFEBUOY = Util.prefix("add_lifebuoy");
    public static final ResourceLocation FINALIZE_SPAWN = Util.prefix("finalize_spawn");
    public static final ResourceLocation CHECK_SPAWN_RULES = Util.prefix("check_spawn_rules");
    public static final ResourceLocation WITH_FOG = Util.prefix("with_fog");
    public static final ResourceLocation WITH_TACO = Util.prefix("with_taco");
    public static final ResourceLocation HOLD_RANDOM_JEWEL = Util.prefix("hold_random_jewel");
    public static final ResourceLocation HOLD_RANDOM_MATERIAL = Util.prefix("hold_random_material");

    public static boolean babylize(@Nullable Invasion invasion, Entity entity, int threat) {
        if (entity instanceof Mob mob && ! mob.isBaby()) {
            mob.setBaby(true);
            mob.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("babylize", -0.4, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        return true;
    }
    public static boolean addLifeBuoy(@Nullable Invasion invasion, Entity entity, int threat) {
        List<Entity> entities = new ArrayList<>();
        entities.add(entity);
        entity.getIndirectPassengers().iterator().forEachRemaining(entities::add);
        entities.forEach(passenger -> {
            if (entity instanceof PathfinderMob mob && ! mob.getNavigation().canFloat() && ! (mob.getNavigation() instanceof WaterBoundPathNavigation) &&
                    entity.level.getBlockState(entity.blockPosition().below()).getFluidState().is(Fluids.WATER)) {
                mob.setItemSlot(EquipmentSlot.LEGS, PVZItems.DUCK_LIFEBUOY.get().getDefaultInstance());
            }
        });
        return true;
    }
    public static boolean finalizeSpawn(@Nullable Invasion invasion, Entity entity, int threat) {
        List<Entity> entities = new ArrayList<>();
        entities.add(entity);
        entity.getIndirectPassengers().iterator().forEachRemaining(entities::add);
        entities.forEach(passenger -> {
            if (passenger instanceof Mob mob) {
                mob.finalizeSpawn(((ServerLevel) entity.level), entity.level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.EVENT, null, null);
            }
        });
        return true;
    }
    public static boolean checkSpawnRules(@Nullable Invasion invasion, Entity entity, int threat) {
        return SpawnPlacements.checkSpawnRules(entity.getType(), (ServerLevelAccessor) entity.level, MobSpawnType.EVENT, entity.blockPosition(), entity.level.getRandom());
    }
    public static boolean withFog(@Nullable Invasion invasion, Entity entity, int threat) {
        if (invasion == null) {
            return true;
        }
        PVZFog fog = PVZFog.getFog(invasion.uuid);
        if (fog == null) {
            PVZFog.addFogSided(invasion.level.dimension().location(), Vec3.atCenterOf(invasion.position), 100, ((double) invasion.invasionLevel / 5 + 1), invasion.range, invasion.uuid);
        } else {
            fog.lifeLeft = 100;
            fog.position = Vec3.atCenterOf(invasion.position);
        }
        return true;
    }
    public static boolean withTaco(@Nullable Invasion invasion, Entity entity, int threat) {
        if (invasion == null || ! EntityUtil.isEntityValid(invasion.target)) {
            return true;
        }
        if (invasion.currentWave > invasion.waves.size() / 3 && invasion.getCurrentWave().isBigWave &&
                threat > 500 && random.nextInt(invasion.getCurrentWave().threat) < threat / 2) {
            invasion.summonEntity(TACO);
        }
        return true;
    }
    public static boolean holdRandomJewel(@Nullable Invasion invasion, Entity entity, int threat) {
        if (invasion == null || ! EntityUtil.isEntityValid(invasion.target)) {
            return true;
        }
        if (entity instanceof Zombie zombie && zombie.getRandom().nextInt(50) == 0) {
            if (zombie.getOffhandItem().isEmpty()) {
                zombie.setItemInHand(InteractionHand.OFF_HAND
                        , (zombie.getRandom().nextBoolean() ? Items.DIAMOND : PVZItems.JEWEL.get()).getDefaultInstance());
                zombie.setGuaranteedDrop(EquipmentSlot.OFFHAND);
            }
        }
        return true;
    }
    public static boolean holdRandomMaterial(@Nullable Invasion invasion, Entity entity, int threat) {
        if (invasion == null || ! EntityUtil.isEntityValid(invasion.target)) {
            return true;
        }
        if (entity instanceof Zombie zombie && zombie.getRandom().nextInt(25) == 0) {
            if (zombie.getOffhandItem().isEmpty()) {
                int i = random.nextInt(10);
                Item item = i == 0 ? Items.NETHERITE_SCRAP : i <= 2 ? Items.PRISMARINE_CRYSTALS : i == 3 ? Items.DIAMOND : i <= 7 ? Items.IRON_INGOT : Items.GOLD_INGOT;
                zombie.setItemInHand(InteractionHand.OFF_HAND, item.getDefaultInstance());
                zombie.setGuaranteedDrop(EquipmentSlot.OFFHAND);
                if (zombie.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                    zombie.setItemSlot(EquipmentSlot.HEAD, Items.CHAINMAIL_HELMET.getDefaultInstance());
                }
                if (zombie.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                    zombie.setItemSlot(EquipmentSlot.CHEST, Items.CHAINMAIL_CHESTPLATE.getDefaultInstance());
                }
                if (zombie.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
                    zombie.setItemSlot(EquipmentSlot.FEET, Items.CHAINMAIL_BOOTS.getDefaultInstance());
                }
            }
        }
        return true;
    }
}
