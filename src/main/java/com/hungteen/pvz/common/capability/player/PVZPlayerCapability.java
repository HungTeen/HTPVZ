package com.hungteen.pvz.common.capability.player;

import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.PlayerCoolDownPacket;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PVZPlayerCapability implements ICapabilitySerializable<CompoundTag> {

    private PVZPlayerCapNBT nbt = null;
    public static final Capability<PVZPlayerCapNBT> NBT = CapabilityManager.get(new CapabilityToken<>(){});
    private final LazyOptional<PVZPlayerCapNBT> opt = LazyOptional.of(this::createNBT);
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

    public static void tick(TickEvent.ServerTickEvent ev){
        //timed sync
        if (++ syncCount > 20){
            for (Player player : ev.getServer().getPlayerList().getPlayers()){
                getPlayerData(player).ifPresent(PVZPlayerCapNBT::syncAll);
            }
            syncCount = 0;
        }
        for (Player player : ev.getServer().getPlayerList().getPlayers()) {
            getPlayerData(player).ifPresent((nbt) -> {
                //sun related mob effects.
                ++ nbt.sunCountDown;
                if (player.hasEffect(PVZMobEffects.BRIGHTNESS.get())) {
                    if (player.hasEffect(MobEffects.DARKNESS)) {
                        player.removeEffect(MobEffects.DARKNESS);
                    }
                    if (nbt.sunCountDown >= 15/(player.getEffect(PVZMobEffects.BRIGHTNESS.get()).getAmplifier() + 1)) {
                        int limitSun = getDifficultyLimitSun(player);
                        int curSun = nbt.getValue(PVZPlayerCapNBT.SUN);
                        if (curSun < limitSun) {
                            nbt.setValue(PVZPlayerCapNBT.SUN, Math.min(curSun + 5, limitSun));
                        }
                        nbt.sunCountDown = 0;
                    }
                } else if (player.hasEffect(MobEffects.DARKNESS) && nbt.sunCountDown >= 15/(player.getEffect(MobEffects.DARKNESS).getAmplifier() + 1)) {
                    int limitSun = getDifficultyLimitSun(player);
                    int curSun = nbt.getValue(PVZPlayerCapNBT.SUN);
                    if (curSun > limitSun) {
                        nbt.setValue(PVZPlayerCapNBT.SUN, Math.max(curSun - 5, limitSun));
                    }
                    nbt.sunCountDown = 0;
                }
                if (EntityUtil.isSurvivalPlayer(player) && EntityUtil.isEntityPeace(player,100) && player.tickCount % 30 == 0) {
                    int limitSun = nbt.getValueLimit(PVZPlayerCapNBT.SUN).getSecond();
                    int curSun = nbt.getValue(PVZPlayerCapNBT.SUN);
                    if (curSun < limitSun) {
                        nbt.addValue(PVZPlayerCapNBT.SUN, 5);
                    }
                }
                //cool down effects.
                if (nbt.getValue("plant_have_cd") == 0) {
                    Set<Item> keyset = Set.copyOf(player.getCooldowns().cooldowns.keySet());
                    for (Item i : keyset) {
                        if (i instanceof SeedPacketItem) {
                            player.getCooldowns().removeCooldown(i);
                        }
                    }
                }
                if (player.hasEffect(PVZMobEffects.EXCITEMENT.get())) {
                    for (int i = 0; i < 10; i ++) {
                        player.getCooldowns().tick();
                    }
                    if (player instanceof ServerPlayer) {
                        PlayerCoolDownPacket.clientCoolDown((ServerPlayer) player);
                    }
                }
                //auto set sun cost and cd.
                if (nbt.getValue("auto_set_cost_and_cd") == 1) {
                    nbt.setValue("plant_have_cost", player.isCreative() ? 0 : 1);
                    nbt.setValue("plant_have_cd", player.isCreative() ? 0 : 1);
                }
            });
        }
    }

    public static int getDifficultyLimitSun(Player player) {
        Difficulty difficulty = player.getServer().getWorldData().getDifficulty();
        int limitSun;
        switch (difficulty) {
            case PEACEFUL -> limitSun = 300;
            case EASY -> limitSun = 200;
            case HARD -> limitSun = 50;
            default -> limitSun = 100;//normal difficulty or other possible situations.
        }
        return limitSun;
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
