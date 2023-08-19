package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class PlayerStatsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("playerstats").requires((ctx) -> ctx.hasPermission(2));
        builder.then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.literal("add")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes((command) -> {
                                            return addPlayerStats(command.getSource(), EntityArgument.getPlayers(command, "targets"), StringArgumentType.getString(command, "name"), IntegerArgumentType.getInteger(command, "amount"));
                                        }))))
                .then(Commands.literal("query")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.literal("value")
                                        .executes((command) -> {
                                            return queryPlayerStats(command.getSource(), EntityArgument.getPlayers(command, "targets"), StringArgumentType.getString(command, "name"), true);
                                        }))
                                .then(Commands.literal("limit")
                                        .executes((command) -> {
                                            return queryPlayerStats(command.getSource(), EntityArgument.getPlayers(command, "targets"), StringArgumentType.getString(command, "name"), false);
                                        }))))
                .then(Commands.literal("set")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.literal("value")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes((command) -> {
                                                    return setPlayerStats(command.getSource(), EntityArgument.getPlayers(command, "targets"), StringArgumentType.getString(command, "name"), IntegerArgumentType.getInteger(command, "amount"));
                                                })))
                                .then(Commands.literal("limit")
                                        .then(Commands.argument("min", IntegerArgumentType.integer())
                                                .then(Commands.argument("max", IntegerArgumentType.integer())
                                                        .executes((command) -> {
                                                            return setPlayerStatsLimit(command.getSource(), EntityArgument.getPlayers(command, "targets"), StringArgumentType.getString(command, "name"), IntegerArgumentType.getInteger(command, "min"), IntegerArgumentType.getInteger(command, "max"));
                                                        }))))
                        )));
        dispatcher.register(builder);
    }

    public static int addPlayerStats(CommandSourceStack source, Collection<? extends ServerPlayer> targets, String name, int num) {
        for (ServerPlayer player : targets) {
            PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
                nbt.addValue(name, num);
                source.sendSuccess(Component.translatable(name + " : " + nbt.getValue(name)), true);
            }));
        }
        return targets.size();
    }

    public static int queryPlayerStats(CommandSourceStack source, Collection<? extends ServerPlayer> targets, String name, boolean valueOrLimit) {
        for (ServerPlayer player : targets) {
            if (valueOrLimit) {
                PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
                    source.sendSuccess(Component.translatable(name + " : " + nbt.getValue(name)), true);
                }));
            } else {
                PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
                    Pair<Integer, Integer> limit = nbt.getValueLimit(name);
                    source.sendSuccess(Component.translatable(name + " : " + limit.getFirst() + " ~ " + limit.getSecond()), true);
                }));
            }
        }
        return targets.size();
    }

    public static int setPlayerStats(CommandSourceStack source, Collection<? extends ServerPlayer> targets, String name, int num) {
        for (ServerPlayer player : targets) {
            PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
                nbt.setValue(name, num);
                source.sendSuccess(Component.translatable(name + " : " + nbt.getValue(name)), true);
            }));
        }
        return targets.size();
    }

    public static int setPlayerStatsLimit(CommandSourceStack source, Collection<? extends ServerPlayer> targets, String name, int min, int max) {
        for (ServerPlayer player : targets) {
            PVZPlayerCapability.getPlayerData(player).ifPresent(((nbt) -> {
                nbt.setValueLimit(name, min, max);
                Pair<Integer, Integer> limit = nbt.getValueLimit(name);
                source.sendSuccess(Component.translatable(name + " : " + limit.getFirst() + " ~ " + limit.getSecond()), true);
            }));
        }
        return targets.size();
    }
}
