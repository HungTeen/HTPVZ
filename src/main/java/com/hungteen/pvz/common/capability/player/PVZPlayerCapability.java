package com.hungteen.pvz.common.capability.player;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PVZPlayerCapability implements ICapabilitySerializable<CompoundTag> {

    private PVZPlayerCapNBT nbt = null;
    public static final Capability<PVZPlayerCapNBT> NBT = CapabilityManager.get(new CapabilityToken<>(){});
    private final LazyOptional<PVZPlayerCapNBT> opt = LazyOptional.of(this::createNBT);
    public static Set<Player> playerSet = new HashSet<>();
    public static int syncCount = 0;

    public PVZPlayerCapability(Player player) {
        this.opt.ifPresent(cap -> cap.setPlayer(player));
    }

    private PVZPlayerCapNBT createNBT(){
        if(nbt == null){
            nbt = new PVZPlayerCapNBT();
        }
        return nbt;
    }

    public static void tick(){
        //timed sync
        if (++ syncCount > 20){
            for (Player player : playerSet){
                getPlayerData(player).ifPresent(PVZPlayerCapNBT::syncAll);
            }
            syncCount = 0;
        }
        //sun changing when having effects
        for (Player player : playerSet) {
            getPlayerData(player).ifPresent( (nbt) -> {
                if (player.hasEffect(MobEffects.DARKNESS) && ++ nbt.sunCountDown >= 30/(player.getEffect(MobEffects.DARKNESS).getAmplifier() + 1)) {
                    Difficulty difficulty = player.getServer().getWorldData().getDifficulty();
                    int limitSun;
                    switch (difficulty) {
                        case PEACEFUL -> limitSun = 300;
                        case EASY -> limitSun = 200;
                        case HARD -> limitSun = 50;
                        default -> limitSun = 100;//normal difficulty or other possible situations.
                    }
                    int curSun = nbt.getValue("sun");
                    if (curSun - 5 < limitSun) {
                        nbt.setValue("sun", limitSun);
                    } else if (curSun > limitSun) {
                        nbt.addValue("sun", -5);
                    }
                    nbt.sunCountDown = 0;
                }
            });
        }
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == NBT){
            return opt.cast();
        }
        return LazyOptional.empty();
    }

    public PVZPlayerCapNBT getPlayerData() {
        return nbt;
    }

    public static LazyOptional<PVZPlayerCapNBT> getPlayerData(Player player){
        return player.getCapability(NBT);
    }

    public static int getValue(Player player, String key){
        AtomicInteger value = new AtomicInteger();
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> value.set(nbt.getValue(key)));
        return value.get();
    }

    public static Pair<Integer, Integer> getValueLimit(Player player, String key){
        AtomicReference<Pair<Integer, Integer>> value = new AtomicReference<>();
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> value.set(nbt.getValueLimit(key)));
        return value.get();
    }

    @Override
    public CompoundTag serializeNBT() {
        return getPlayerData().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        getPlayerData().deserializeNBT(tag);
    }
}
