package com.hungteen.pvz.common.capability.fog;

import com.hungteen.pvz.common.world.PVZFog;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PVZFogCapability implements ICapabilitySerializable<CompoundTag> {
    public PVZFogCapability() {}
    public static final Capability<PVZFogCapability> CAP = CapabilityManager.get(new CapabilityToken<>(){});

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? LazyOptional.of(() -> (T) this) : LazyOptional.empty();
    }

    public static void tick(TickEvent.ServerTickEvent ev) {
        PVZFog.serverFogsTick();
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
