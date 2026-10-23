package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link BeaconBlockEntity}
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/23 9:02
 **/
public class ResinBeaconBlockEntity extends BlockEntity {

    /**Maximum level, determined by the pyramid of {@link PVZBlocks#ORIGIN_BLOCK} below.
     * Layer 1 is 3x3, layer 2 is 5x5, and so on, like a vanilla beacon pyramid.*/
    public static final int MAX_LEVEL = 4;
    /**Horizontal search radius for vanilla beacons whose powers are absorbed.*/
    public static final int SOURCE_RADIUS = 16;

    private int level = 0;

    public ResinBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(PVZBlockEntities.RESIN_BEACON.get(), pos, state);
    }

    public int getResinLevel() {
        return this.level;
    }

    public int getRange() {
        return this.level <= 0 ? 0 : this.level * 10 + 10;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ResinBeaconBlockEntity blockEntity) {
        if (level.getGameTime() % 20 == 0) {
            blockEntity.updateLevel(level);
        }
        if (! level.isClientSide && level.getGameTime() % 80 == 0) {
            blockEntity.applyEffects(level);
        }
        // TODO PT 光线遮挡 染色
    }

    private void updateLevel(Level level) {
        int newLevel = 0;
        for (int layer = 1; layer <= MAX_LEVEL; layer ++) {
            if (!this.isLayerComplete(level, layer)) {
                break;
            }
            newLevel = layer;
        }
        this.level = newLevel;
    }

    private boolean isLayerComplete(Level level, int layer) {
        int y = this.worldPosition.getY() - layer;
        for (int dx = -layer; dx <= layer; dx ++) {
            for (int dz = -layer; dz <= layer; dz ++) {
                BlockPos pos = new BlockPos(this.worldPosition.getX() + dx, y, this.worldPosition.getZ() + dz);
                if (! level.getBlockState(pos).is(PVZBlocks.ORIGIN_BLOCK.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void applyEffects(Level level) {
        if (this.level <= 0) {
            return;
        }
        Set<MobEffect> effects = new HashSet<>();
        BlockPos min = this.worldPosition.offset(-SOURCE_RADIUS, -SOURCE_RADIUS, -SOURCE_RADIUS);
        BlockPos max = this.worldPosition.offset(SOURCE_RADIUS, SOURCE_RADIUS, SOURCE_RADIUS);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BeaconBlockEntity beacon) {
                CompoundTag tag = beacon.saveWithoutMetadata();
                if (tag.getInt("Levels") <= 0) {
                    continue;
                }
                MobEffect primary = MobEffect.byId(tag.getInt("Primary"));
                MobEffect secondary = MobEffect.byId(tag.getInt("Secondary"));
                if (primary != null) {
                    effects.add(primary);
                }
                if (secondary != null) {
                    effects.add(secondary);
                }
            }
        }
        if (effects.isEmpty()) {
            return;
        }
        AABB aabb = new AABB(this.worldPosition).inflate(this.getRange())
                .expandTowards(0, level.getHeight(), 0);
        int duration = 100 * this.level;
        int amplifier = this.level >= MAX_LEVEL ? 1 : 0;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
            if (entity instanceof IPlant) {
                for (MobEffect effect : effects) {
                    entity.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
                }
            }
        }
    }
}
