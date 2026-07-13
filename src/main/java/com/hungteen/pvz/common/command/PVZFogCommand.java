package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.capability.level.PVZFogCapability;
import com.hungteen.pvz.common.network.PVZFogPacket;
import com.hungteen.pvz.common.world.PVZFog;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PVZFogCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("pvzfog").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes((c) -> addFog(c.getSource(), BlockPosArgument.getSpawnablePos(c, "pos"), 60, 3, 10))
                                .then(Commands.argument("uuid", UuidArgument.uuid()).executes((command) -> addFog(command.getSource(), BlockPosArgument.getSpawnablePos(command, "pos"),
                                        500, 1, 10, UuidArgument.getUuid(command, "uuid")))
                                )
                                .then(Commands.argument("seconds", IntegerArgumentType.integer())
                                        .executes((c) -> addFog(c.getSource(), BlockPosArgument.getSpawnablePos(c, "pos"),
                                                IntegerArgumentType.getInteger(c, "seconds") * 20, 1,
                                                10))
                                        .then(Commands.argument("strength", DoubleArgumentType.doubleArg())
                                                .executes((c) -> addFog(c.getSource(), BlockPosArgument.getSpawnablePos(c, "pos"),
                                                        IntegerArgumentType.getInteger(c, "seconds") * 20, DoubleArgumentType.getDouble(c, "strength"),
                                                        10))
                                                .then(Commands.argument("range", DoubleArgumentType.doubleArg())
                                                        .executes((c) -> addFog(c.getSource(), BlockPosArgument.getSpawnablePos(c, "pos"),
                                                                IntegerArgumentType.getInteger(c, "seconds") * 20, DoubleArgumentType.getDouble(c, "strength"),
                                                                DoubleArgumentType.getDouble(c, "range")))
                                                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                                                .executes((c) -> addFog(c.getSource(), BlockPosArgument.getSpawnablePos(c, "pos"),
                                                                        IntegerArgumentType.getInteger(c, "seconds") * 20, DoubleArgumentType.getDouble(c, "strength"),
                                                                        DoubleArgumentType.getDouble(c, "range"), UuidArgument.getUuid(c, "uuid")))
                                                                ))))))
                .then(Commands.literal("modify")
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                .then(Commands.literal("pos").then(Commands.argument("value", Vec3Argument.vec3()).executes(
                                        (c -> modifyFog(c.getSource(), UuidArgument.getUuid(c, "uuid"), BlockPosArgument.getSpawnablePos(c, "value"))))))
                                .then(Commands.literal("seconds").then(Commands.argument("value", IntegerArgumentType.integer()).executes(
                                        (c -> modifyFog(c.getSource(), UuidArgument.getUuid(c, "uuid"), PVZFogPacket.ModifyType.LIFE_TIME, IntegerArgumentType.getInteger(c, "value") * 20)))))
                                .then(Commands.literal("strength").then(Commands.argument("value", IntegerArgumentType.integer()).executes(
                                        (c -> modifyFog(c.getSource(), UuidArgument.getUuid(c, "uuid"), PVZFogPacket.ModifyType.STRENGTH, IntegerArgumentType.getInteger(c, "value"))))))
                        ))
                .then(Commands.literal("remove")
                        .then(Commands.argument("uuid", UuidArgument.uuid()).executes(
                                (c -> modifyFog(c.getSource(), UuidArgument.getUuid(c, "uuid"), PVZFogPacket.ModifyType.REMOVE, 0))))
                        .then(Commands.literal("all").executes(
                                (c -> removeAllFogs(c.getSource()))
                        )));
        dispatcher.register(builder);
    }

    private static int addFog(CommandSourceStack source, BlockPos pos, int seconds, double strength, double range) {
        return addFog(source, pos, seconds, strength, range, UUID.randomUUID());
    }

    private static int addFog(CommandSourceStack source, BlockPos pos, int seconds, double strength, double range, UUID uuid) {
        if (PVZFogCapability.addOrResetFog(source.getLevel(), pos, seconds, strength, range, uuid)) {
            source.sendSuccess(Component.translatable("commands.pvz.fog.add", "["+ uuid +"]"), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.pvz.fog.already_exists", "["+ uuid +"]"));
            return 0;
        }
    }

    private static int modifyFog(CommandSourceStack source, UUID uuid, BlockPos pos) {
        if (PVZFogCapability.modifyFogPosition(source.getLevel(), uuid, pos)) {
            source.sendSuccess(Component.translatable("commands.pvz.fog.modify", "["+ uuid +"]"), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.pvz.fog.not_exists", "["+ uuid +"]"));
            return 0;
        }
    }

    private static int modifyFog(CommandSourceStack source, UUID uuid, PVZFogPacket.ModifyType modifyType, int value) {
        if (PVZFogCapability.modifyFogFeatures(source.getLevel(), uuid, modifyType, value)) {
            source.sendSuccess(Component.translatable("commands.pvz.fog.modify", "["+ uuid +"]"), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.pvz.fog.not_exists", "["+ uuid +"]"));
            return 0;
        }
    }

    private static int removeAllFogs(CommandSourceStack source) {
        AtomicInteger num = new AtomicInteger();
        source.getLevel().getCapability(PVZFogCapability.CAP).ifPresent(cap -> {
            num.set(cap.fogs.size());
            Set<PVZFog> fogs = Set.copyOf(cap.fogs.values());
            fogs.forEach(fog -> modifyFog(source, fog.uuid, PVZFogPacket.ModifyType.REMOVE, 0));
            source.sendSuccess(Component.translatable("commands.pvz.fog.remove_all", num), true);
        });
        return num.get();
    }
}
