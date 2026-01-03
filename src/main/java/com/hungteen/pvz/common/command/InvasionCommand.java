package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.common.world.invasion.InvasionType;
import com.hungteen.pvz.util.Util;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class InvasionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invasion")
                .then(Commands.literal("add")
                        .then(Commands.argument("level", IntegerArgumentType.integer())
                                .then(Commands.argument("target", EntityArgument.entities())
                                .executes(c -> addInvasion(c.getSource(),
                                        IntegerArgumentType.getInteger(c, "level")
                                        , EntityArgument.getEntity(c, "target")
                                        , UUID.randomUUID()))
                                .then(Commands.argument("uuid", UuidArgument.uuid())
                                        .executes(c -> addInvasion(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "level")
                                                , EntityArgument.getEntity(c, "target")
                                                , UuidArgument.getUuid(c, "uuid")))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                .executes(c -> removeInvasion(c.getSource(), UuidArgument.getUuid(c, "uuid"))))));
    }

    public static int addInvasion(CommandSourceStack source, int level, Entity entity, UUID uuid) {
        if (! (entity instanceof LivingEntity living)) {
            source.sendFailure(Component.translatable("commands.pvz.invasion.target_must_be_living", uuid));
            return 0;
        }
        List<InvasionType> types = InvasionType.generateTypes(living);
        if (types.isEmpty()) {
            types.add(InvasionType.invasionTypes.get(Util.prefix("empty")));
            source.sendSystemMessage(Component.translatable("commands.pvz.invasion.using_empty_invasion_type"));
        }
        entity.level.getCapability(PVZZombieEventCapability.CAP).ifPresent(cap ->
                cap.addEvent(new Invasion(entity.level, uuid, types, living, entity.blockPosition(), Math.min(255, Math.max(1, level)))));
        source.sendSuccess(Component.translatable("commands.pvz.invasion.add", uuid), true);
        return 1;
    }

    public static int removeInvasion(CommandSourceStack source, UUID uuid) {
        AtomicBoolean result = new AtomicBoolean(false);
        source.getLevel().getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> {
            if (cap.hasEvent(uuid)) {
                cap.getEvent(uuid).remove();
                result.set(true);
            }
        });
        source.sendSuccess(Component.translatable("commands.pvz.invasion.remove", uuid), true);
        return result.get() ? 1 : 0;
    }
}
