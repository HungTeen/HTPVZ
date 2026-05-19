package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerStatsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerstats").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("add")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes((command) -> {
                                                    return addPlayerStats(command.getSource(), EntityArgument.getPlayer(command, "player"), StringArgumentType.getString(command, "name"), IntegerArgumentType.getInteger(command, "amount"));
                                                }))))
                        .then(Commands.literal("query")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes((command) -> {
                                            return queryPlayerStats(command.getSource(), EntityArgument.getPlayer(command, "player"), StringArgumentType.getString(command, "name"), true);
                                        })
                                        .then(Commands.literal("limit")
                                                .executes((command) -> {
                                                    return queryPlayerStats(command.getSource(), EntityArgument.getPlayer(command, "player"), StringArgumentType.getString(command, "name"), false);
                                                }))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes((command) -> {
                                                    return setPlayerStats(command.getSource(), EntityArgument.getPlayer(command, "player"), StringArgumentType.getString(command, "name"), IntegerArgumentType.getInteger(command, "amount"));
                                                }))
                                        .then(Commands.literal("limit")
                                                .then(Commands.argument("min", IntegerArgumentType.integer())
                                                        .then(Commands.argument("max", IntegerArgumentType.integer())
                                                                .executes((command) -> {
                                                                    return setPlayerStatsLimit(command.getSource(), EntityArgument.getPlayer(command, "player"), StringArgumentType.getString(command, "name"), IntegerArgumentType.getInteger(command, "min"), IntegerArgumentType.getInteger(command, "max"));
                                                                }))))
                                )
                        )
                )
        );
    }

    public static int addPlayerStats(CommandSourceStack source, ServerPlayer player, String name, int num) {
        AtomicInteger before = new AtomicInteger(0);
        AtomicInteger after = new AtomicInteger(0);
        PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
            before.set(nbt.getValue(name));
            nbt.addValue(name, num);
            after.set(nbt.getValue(name));
            source.sendSuccess(Component.translatable("commands.pvz.playerstats.set_value", name, nbt.getValue(name)), true);
        }));
        return before.get() + num - after.get();
    }

    public static int queryPlayerStats(CommandSourceStack source, ServerPlayer player, String name, boolean valueOrLimit) {
        AtomicInteger num = new AtomicInteger(0);
        if (valueOrLimit) {
            PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
                num.set(nbt.getValue(name));
                source.sendSuccess(Component.translatable("commands.pvz.playerstats.get_value", name, nbt.getValue(name)), true);
            }));
        } else {
            PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
                Pair<Integer, Integer> limit = nbt.getValueLimit(name);
                num.set(limit.getSecond());
                source.sendSuccess(Component.translatable("commands.pvz.playerstats.get_limit", name, limit.getFirst(), limit.getSecond()), true);
            }));
        }
        return num.get();
    }

    public static int setPlayerStats(CommandSourceStack source, ServerPlayer player, String name, int num) {
        PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
            nbt.setValue(name, num);
            source.sendSuccess(Component.translatable("commands.pvz.playerstats.set_value", name, nbt.getValue(name)), true);
        }));
        return 1;
    }

    public static int setPlayerStatsLimit(CommandSourceStack source, ServerPlayer player, String name, int min, int max) {
        PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
            nbt.setValueLimit(name, min, max);
            Pair<Integer, Integer> limit = nbt.getValueLimit(name);
            source.sendSuccess(Component.translatable("commands.pvz.playerstats.set_limit", name, limit.getFirst(), limit.getSecond()), true);
        }));
        return 1;
    }
}
