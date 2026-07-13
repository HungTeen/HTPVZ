package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.block.InvasionSpawnerBlock;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.tags.PVZBiomeTags;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.common.world.invasion.InvasionType;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.MathUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
        UUID uuid = MathUtil.posToUuid(pos);
        if (! level.isClientSide) {
            long gameTime = level.getGameTime();
            if (gameTime % 20 > 0) return;
            level.getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> {
                boolean current = blockState.getValue(InvasionSpawnerBlock.TRIGGERED);
                Invasion invasion = cap.getNearestEvent(Invasion.class, pos);
                boolean hasInvasion = invasion != null && invasion.position.distSqr(pos) < 256;
                if (! current) {
                    if (hasInvasion) {
                        level.setBlock(pos, blockState.setValue(InvasionSpawnerBlock.TRIGGERED, true), 2);
                        if (invasion.target != null) blockEntity.triggerHistory.put(invasion.target.getUUID(), level.getGameTime());
                    }
                } else {
                    if (hasInvasion) {
                        BlockPos invasionPos = MathUtil.posFromUuid(invasion.uuid);
                        if (invasionPos.equals(pos) && invasion.isEnded()) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            ((ServerLevel) level).sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE
                                    , pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10
                                    , 0.3F, 0.3F, 0.3F, 0.01f);
                        }
                    } else {
                        level.setBlock(pos, blockState.setValue(InvasionSpawnerBlock.TRIGGERED, false), 2);
                    }
                }
            });
            Set<UUID> uuids = Set.copyOf(blockEntity.triggerHistory.keySet());
            for (UUID playerUuid: uuids) {
                if (gameTime - blockEntity.triggerHistory.get(playerUuid) >
                        (blockEntity.coolDown >= 0 ? blockEntity.coolDown : PVZConfig.PVZGameRules.getInt(level, PVZConfig.Common.invasionSpawnerCoolDown))) {
                    blockEntity.triggerHistory.remove(playerUuid);
                }
            }
            if (! blockState.getValue(InvasionSpawnerBlock.TRIGGERED) && blockEntity.coolDown != 0) {
                Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 12
                        , p -> ! EntityUtil.isEntityEvil(p) && ! blockEntity.triggerHistory.containsKey(p.getUUID()) && EntityUtil.isSurvivalPlayer(p));
                if (player != null
                        && ! level.getBiome(pos).is(PVZBiomeTags.UNABLE_INVASION)
                        && ! blockState.getValue(InvasionSpawnerBlock.TRIGGERED)) {
                    int teammateNum = level.getEntities(player
                            , new AABB(pos.offset(-24, -12, -24), pos.offset(24, 12, 24))
                            , e -> ! (e instanceof Player) && ! EntityUtil.isEntityEvil(e)).size();
                    if ((player.blockPosition().distSqr(pos) < 16 || teammateNum < 4) && teammateNum < 10) {
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
                            level.setBlock(pos, blockState.setValue(InvasionSpawnerBlock.TRIGGERED, true), 2);
                        }
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
