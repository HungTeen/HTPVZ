package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.network.PVZFogPacket;
import com.hungteen.pvz.common.world.PVZFog;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

public class PVZFogCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("pvzfog").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.literal("add")
                .then(Commands.argument("pos", Vec3Argument.vec3())
                        .executes((c) -> addFog(c.getSource(), Vec3Argument.getVec3(c, "pos"), 60, 3, 10))
                        .then(Commands.argument("uuid", UuidArgument.uuid()).executes((command) -> addFog(command.getSource(), Vec3Argument.getVec3(command, "pos"),
                                15, 1, 10, UuidArgument.getUuid(command, "uuid")))
                        )
                        .then(Commands.argument("seconds", IntegerArgumentType.integer())
                        .then(Commands.argument("strength", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("range", DoubleArgumentType.doubleArg())
                                .executes((c) -> addFog(c.getSource(), Vec3Argument.getVec3(c, "pos"),
                                        IntegerArgumentType.getInteger(c, "seconds"), DoubleArgumentType.getDouble(c, "strength"),
                                        DoubleArgumentType.getDouble(c, "range")))
                                .then(Commands.argument("uuid", UuidArgument.uuid())
                                        .executes((c) -> addFog(c.getSource(), Vec3Argument.getVec3(c, "pos"),
                                                IntegerArgumentType.getInteger(c, "seconds"), DoubleArgumentType.getDouble(c, "strength"),
                                                DoubleArgumentType.getDouble(c, "range"), UuidArgument.getUuid(c, "uuid")))
                                        ))))))
                .then(Commands.literal("modify")
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                .then(Commands.literal("pos").then(Commands.argument("value", Vec3Argument.vec3()).executes(
                                        (c -> modifyFog(c.getSource(), UuidArgument.getUuid(c, "uuid"), Vec3Argument.getVec3(c, "value"))))))
                                .then(Commands.literal("seconds").then(Commands.argument("value", IntegerArgumentType.integer()).executes(
                                        (c -> modifyFog(c.getSource(), UuidArgument.getUuid(c, "uuid"), PVZFogPacket.ModifyType.LIFE_TIME, IntegerArgumentType.getInteger(c, "value"))))))
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

    private static int addFog(CommandSourceStack source, Vec3 pos, int seconds, double strength, double range) {
        return addFog(source, pos, seconds, strength, range, UUID.randomUUID());
    }
    private static int addFog(CommandSourceStack source, Vec3 pos, int seconds, double strength, double range, UUID uuid) {
        if (PVZFogPacket.fog(source.getLevel(), pos, seconds, strength, range, uuid)) {
            source.sendSuccess(Component.translatable("commands.pvz.fog.add", "["+ uuid +"]"), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.pvz.fog.already_exists", "["+ uuid +"]"));
            return 0;
        }
    }
    private static int modifyFog(CommandSourceStack source, UUID uuid, Vec3 pos) {
        if (PVZFogPacket.modifyFog(uuid, pos)) {
            source.sendSuccess(Component.translatable("commands.pvz.fog.modify", "["+ uuid +"]"), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.pvz.fog.not_exists", "["+ uuid +"]"));
            return 0;
        }
    }
    private static int modifyFog(CommandSourceStack source, UUID uuid, PVZFogPacket.ModifyType modifyType, int value) {
        if (PVZFogPacket.modifyFog(uuid, modifyType, value)) {
            source.sendSuccess(Component.translatable("commands.pvz.fog.modify", "["+ uuid +"]"), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.pvz.fog.not_exists", "["+ uuid +"]"));
            return 0;
        }
    }

    private static int removeAllFogs(CommandSourceStack source) {
        int num = PVZFog._pvzFogs.size();
        Set<PVZFog> fogs = Set.copyOf(PVZFog._pvzFogs.values());
        fogs.forEach(fog -> modifyFog(source, fog.uuid, PVZFogPacket.ModifyType.REMOVE, 0));
        UUID uuid;
        if (num == 1) {
            uuid = fogs.iterator().next().uuid;
        } else {
            source.sendSuccess(Component.translatable("commands.pvz.fog.remove_all", num), true);
        }
        return num;
    }
}
