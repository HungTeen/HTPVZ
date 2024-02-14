package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.network.PVZFogPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;

import java.util.UUID;

public class PVZFogCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("pvzfog").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                .then(Commands.argument("seconds", IntegerArgumentType.integer())
                .then(Commands.argument("strength", DoubleArgumentType.doubleArg())
                .then(Commands.argument("range", DoubleArgumentType.doubleArg())
                .executes((command) -> {
                    PVZFogPacket.fog(command.getSource().getLevel(), Vec3Argument.getVec3(command, "pos"), IntegerArgumentType.getInteger(command, "seconds"),
                        DoubleArgumentType.getDouble(command, "strength"), DoubleArgumentType.getDouble(command, "range"), UUID.randomUUID());
                    return 1;
                }))))
                .executes((command) -> {
                    PVZFogPacket.fog(command.getSource().getLevel(), Vec3Argument.getVec3(command, "pos"), 15, 1, 10, UUID.randomUUID());
                    return 1;
                }));
        dispatcher.register(builder);
    }
}
