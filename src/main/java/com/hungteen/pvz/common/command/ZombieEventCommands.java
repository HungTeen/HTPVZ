package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.register.PVZZombieEvents;
import com.hungteen.pvz.common.world.ZombieGroup;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ZombieEventCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zombieevent").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.literal("invasion")
                        .then(Commands.literal("add")
                                .executes(c -> addInvasion(c.getSource(), 5
                                        , c.getSource().getEntity()
                                        , UUID.randomUUID()))
                                .then(Commands.argument("level", IntegerArgumentType.integer())
                                        .executes(c -> addInvasion(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "level")
                                                , c.getSource().getEntity()
                                                , UUID.randomUUID()))
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
                                .then(Commands.literal("all")
                                        .executes(c -> removeAllInvasion(c.getSource())))
                                .then(Commands.argument("uuid", UuidArgument.uuid())
                                        .executes(c -> removeInvasion(c.getSource(), UuidArgument.getUuid(c, "uuid"))))))
                .then(Commands.literal("zombiegroup")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(c -> ZombieGroup.spawnFor(EntityArgument.getPlayer(c, "target")) ? 1 : 0)
                        )
                )
        );
    }

    public static int addInvasion(CommandSourceStack source, int level, @Nullable Entity entity, UUID uuid) {
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
        if (result.get()) {
            source.sendSuccess(Component.translatable("commands.pvz.invasion.remove", uuid), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.pvz.invasion.not_found"));
            return 0;
        }
    }

    public static int removeAllInvasion(CommandSourceStack source) {
        AtomicInteger result = new AtomicInteger(0);
        source.getLevel().getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> {
            cap.getEvents().forEach(event -> {
                if (event.getType().equals(PVZZombieEvents.INVASION.getId())) {
                    event.remove();
                    result.incrementAndGet();
                }
            });
        });
        if (result.get() > 0) {
            source.sendSuccess(Component.translatable("commands.pvz.invasion.remove_all", result.get()), true);
            return result.get();
        } else {
            source.sendFailure(Component.translatable("commands.pvz.invasion.not_found"));
            return 0;
        }
    }
}
