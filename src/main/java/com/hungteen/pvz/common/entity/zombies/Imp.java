package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class Imp extends PVZZombie {
    public Imp(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
        this.setBaby(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PVZZombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 12D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, data, tag);
        this.setBaby(true);
        return spawnGroupData;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (! tag.contains("IsBaby")) {
            this.setBaby(true);
        }
    }

    //sounds
    @Override
    public @NotNull SoundEvent getAmbientSound() {
        return PVZSoundEvents.IMP_AMBIENT.get();
    }

    @Override
    public @NotNull SoundEvent getDeathSound() {
        return PVZSoundEvents.IMP_DEATH.get();
    }

    public SoundEvent getThrowSound(Entity thrower) {
        return PVZSoundEvents.IMP_THROWN.get();
    }
}
