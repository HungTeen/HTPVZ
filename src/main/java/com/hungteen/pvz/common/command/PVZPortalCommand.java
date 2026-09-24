package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.entity.Portal;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PVZPortalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("pvzportal").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.literal("create")
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .then(Commands.argument("target", Vec3Argument.vec3())
                                        .executes((c) -> createPortal(c.getSource(), Vec3Argument.getVec3(c, "pos"), Vec3Argument.getVec3(c, "target")))
                                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                                .executes((c) -> createPortal(c.getSource(), Vec3Argument.getVec3(c, "pos"), Vec3Argument.getVec3(c, "target"),
                                                        UuidArgument.getUuid(c, "uuid"), null))
                                                .then(Commands.argument("target_uuid", UuidArgument.uuid())
                                                        .executes((c) -> createPortal(c.getSource(), Vec3Argument.getVec3(c, "pos"), Vec3Argument.getVec3(c, "target"),
                                                                UuidArgument.getUuid(c, "uuid"), UuidArgument.getUuid(c, "target_uuid"))))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                .executes((c) -> removePortal(c.getSource(), UuidArgument.getUuid(c, "uuid"))))
                        .then(Commands.literal("all")
                                .executes((c) -> removeAllPortals(c.getSource()))));
        dispatcher.register(builder);
    }

    private static int createPortal(CommandSourceStack source, Vec3 pos, Vec3 target) {
        return createPortal(source, pos, target, null, null);
    }

    private static int createPortal(CommandSourceStack source, Vec3 pos, Vec3 target, @Nullable UUID uuid, @Nullable UUID targetUuid) {
        ServerLevel level = source.getLevel();
        if ((uuid != null && level.getEntity(uuid) != null) || (targetUuid != null && level.getEntity(targetUuid) != null)) {
            source.sendFailure(Component.translatable("commands.pvz.portal.uuid_in_use"));
            return 0;
        }
        Pair<Portal, Portal> pair = Portal.createPair(level, pos, target, uuid, targetUuid);
        source.sendSuccess(Component.translatable("commands.pvz.portal.create", "[" + pair.getFirst().getUUID() + ", " + pair.getSecond().getUUID() + "]"), true);
        return 1;
    }

    private static int removePortal(CommandSourceStack source, UUID uuid) {
        ServerLevel level = source.getLevel();
        Entity entity = level.getEntity(uuid);
        if (entity instanceof Portal portal) {
            portal.setState(2);
            source.sendSuccess(Component.translatable("commands.pvz.portal.remove", "[" + uuid + "]"), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.pvz.portal.not_exists", "[" + uuid + "]"));
            return 0;
        }
    }

    private static int removeAllPortals(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        AtomicInteger num = new AtomicInteger();
        level.getAllEntities().forEach(entity -> {
            if (entity instanceof Portal portal) {
                portal.setState(2);
                num.incrementAndGet();
            }
        });
        source.sendSuccess(Component.translatable("commands.pvz.portal.remove_all", num.get()), true);
        return num.get();
    }
}
