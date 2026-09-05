package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class TombstoneBlockEntity extends SpawnerBlockEntity {
    private final BlockEntityType<?> type;

    public TombstoneBlockEntity(BlockPos p_155752_, BlockState p_155753_) {
        super(p_155752_, p_155753_);
        this.type = PVZBlockEntities.TOMBSTONE.get();
        this.spawner = new TombstoneSpawner(this);
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    public static class TombstoneSpawner extends BaseSpawner {
        public TombstoneBlockEntity entity;
        private static final SpawnData.CustomSpawnRules SPAWN_RULES =
                new SpawnData.CustomSpawnRules(new InclusiveRange<>(0, 15), new InclusiveRange<>(0, 15));

        public TombstoneSpawner(TombstoneBlockEntity entity) {
            this.entity = entity;
            this.minSpawnDelay = 1200;
            this.maxSpawnDelay = 2400;
            this.spawnCount = 1;
            this.spawnRange = 2;
            this.maxNearbyEntities = 2;
        }

        @Override
        public void broadcastEvent(Level p_155767_, BlockPos p_155768_, int p_155769_) {
            p_155767_.blockEvent(p_155768_, PVZBlocks.TOMBSTONE.get(), p_155769_, 0);
        }

        @Override
        public void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData spawnData) {
            spawnData = new SpawnData(spawnData.entityToSpawn(), Optional.of(SPAWN_RULES));
            super.setNextSpawnData(level, pos, spawnData);
            if (level != null) {
                BlockState blockstate = level.getBlockState(pos);
                level.sendBlockUpdated(pos, blockstate, blockstate, 4);
            }
        }

        @Override
        public BlockEntity getSpawnerBlockEntity() {
            return entity;
        }


        @Override
        public void clientTick(Level p_151320_, BlockPos p_151321_) {
            if (this.isNearPlayer(p_151320_, p_151321_)) {
                RandomSource randomsource = p_151320_.getRandom();
                double d0 = (double)p_151321_.getX() + randomsource.nextDouble();
                double d1 = (double)p_151321_.getY() + randomsource.nextDouble();
                double d2 = (double)p_151321_.getZ() + randomsource.nextDouble();
                if (randomsource.nextInt(3) == 0) {
                    p_151320_.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                    p_151320_.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                }

                this.oSpin = this.spin;
                this.spin = (this.spin + (double)(1000.0F / ((float)this.spawnDelay + 200.0F))) % 360.0D;
            } else {
                this.oSpin = this.spin;
            }
        }
    }
}
