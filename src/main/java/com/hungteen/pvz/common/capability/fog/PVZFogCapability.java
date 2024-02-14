package com.hungteen.pvz.common.capability.fog;

import com.hungteen.pvz.common.world.PVZFog;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PVZFogCapability implements ICapabilitySerializable<CompoundTag> {
    public PVZFogCapability() {}

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.of(() -> this).cast();
    }

    @Override
    public CompoundTag serializeNBT() {
        return PVZFog.serialize();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        PVZFog.deserialize(nbt);
    }
}
