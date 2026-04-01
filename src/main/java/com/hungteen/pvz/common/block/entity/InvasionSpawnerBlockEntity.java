package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.block.InvasionSpawnerBlock;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.common.world.invasion.InvasionType;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.MathUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.*;

public class InvasionSpawnerBlockEntity extends BlockEntity {
    public Map<UUID, Long> triggerHistory = new HashMap<>();
    /**When empty, generate invasion types each time.*/
    public @Nonnull List<InvasionType> invasionTypes = new ArrayList<>();
    /**When less than 0, use gamerule value; equals to 0, disable invasion summoning; greater than 0, use this cool down.*/
    public int coolDown = -1;
    public InvasionSpawnerBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(PVZBlockEntities.INVASION_SPAWNER.get(), p_155229_, p_155230_);
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, InvasionSpawnerBlockEntity blockEntity) {
        UUID uuid = MathUtil.posToUuid(pos, 0xd0d3a94b);
        if (! level.isClientSide) {
            level.getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> {
                boolean stored = blockState.getValue(InvasionSpawnerBlock.TRIGGERED);
                boolean current = cap.hasEvent(uuid);
                if (stored != current) {
                    level.setBlock(pos, blockState.setValue(InvasionSpawnerBlock.TRIGGERED, current), 2);
                }
            });
            long gameTime = level.getGameTime();
            Set<UUID> uuids = Set.copyOf(blockEntity.triggerHistory.keySet());
            for (UUID playerUuid: uuids) {
                if (gameTime - blockEntity.triggerHistory.get(playerUuid) >
                        (blockEntity.coolDown >= 0 ? blockEntity.coolDown : PVZConfig.PVZGameRules.getInt(level, PVZConfig.Common.invasionSpawnerCoolDown))) {
                    blockEntity.triggerHistory.remove(playerUuid);
                }
            }
            if (gameTime % 20 == 0 && ! blockState.getValue(InvasionSpawnerBlock.TRIGGERED) && blockEntity.coolDown != 0) {
                Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16
                        , p -> ! EntityUtil.isEntityEvil(p) && ! blockEntity.triggerHistory.containsKey(p.getUUID()) && EntityUtil.isSurvivalPlayer(p));
                if (player != null
                        && ! blockState.getValue(InvasionSpawnerBlock.TRIGGERED)
                        && ! Util.hasBlockBetween(level, player.getEyePosition(), Vec3.atBottomCenterOf(blockEntity.getBlockPos().above()))
                        && (player.blockPosition().distSqr(pos) < 4 || level.getEntities((Entity) null
                        , new AABB(pos.offset(-32, -8, -32), pos.offset(32, 8, 32))
                        , e -> ! EntityUtil.isEntityEvil(e)).size() > 4)) {
                    List<InvasionType> types = blockEntity.invasionTypes.isEmpty() ? InvasionType.generateTypes(player) : blockEntity.invasionTypes;
                    if (! types.isEmpty()) {
                        LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(level);
                        lightningbolt.moveTo(Vec3.atBottomCenterOf(pos));
                        lightningbolt.setVisualOnly(true);
                        level.addFreshEntity(lightningbolt);
                        level.getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> {
                            Invasion invasion = new Invasion(level, uuid, types, player, pos, Util.getInvasionLevel(player));
                            invasion.trackable = false;
                            cap.addEvent(invasion);
                        });
                        blockEntity.triggerHistory.put(player.getUUID(), level.getGameTime());
                        level.setBlock(pos, blockState.setValue(InvasionSpawnerBlock.TRIGGERED, true), 2);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.level == null) return;
        ListTag triggeredTag = new ListTag();
        for (UUID uuid: triggerHistory.keySet()) {
            CompoundTag pair = new CompoundTag();
            pair.putUUID("Player", uuid);
            pair.putLong("Time", triggerHistory.get(uuid));
            triggeredTag.add(pair);
        }
        tag.put("History", triggeredTag);
        if (! invasionTypes.isEmpty()) {
            ListTag types = new ListTag();
            for (InvasionType type : this.invasionTypes) {
                if (type.getName() != null) {
                    ResourceLocation location = type.getName();
                    if (location == null) {
                        PVZMod.LOGGER.warn("Trying to save an invasion type which is not registered.");
                    } else {
                        types.add(StringTag.valueOf(location.toString()));
                    }
                }
            }
            tag.put("InvasionTypes", types);
        }
        if (coolDown >= 0) {
            tag.putInt("CoolDown", coolDown);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("History")) {
            ListTag triggeredTag = tag.getList("History", Tag.TAG_COMPOUND);
            for (Tag pair : triggeredTag) {
                if (pair instanceof CompoundTag tag1) {
                    triggerHistory.put(tag1.getUUID("Player"), tag1.getLong("Time"));
                }
            }
        }
        if (tag.contains("InvasionTypes")) {
            ListTag typesTag = tag.getList("InvasionTypes", Tag.TAG_STRING);
            invasionTypes.clear();
            typesTag.forEach(typeName ->  {
                InvasionType type = InvasionType.getInvasionType(new ResourceLocation(typeName.getAsString()));
                if (type == null) {
                    PVZMod.LOGGER.warn("Trying to load an invasion type with name" + new ResourceLocation(typeName.getAsString()) + " which is unavailable.");
                } else {
                    invasionTypes.add(type);
                }
            });
        }
        if (tag.contains("CoolDown")) {
            this.coolDown = tag.getInt("CoolDown");
        }
    }
}
