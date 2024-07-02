package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class OwnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("own").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.literal("new")
                        .then(Commands.argument("owned entities", EntityArgument.entities())
                                .then(Commands.argument("owner", EntityArgument.entity())
                                        .executes((command) ->
                                                own(command.getSource(), EntityArgument.getEntities(command, "owned entities"), EntityArgument.getEntity(command, "owner"))
                                        ))
                                .executes((command) ->
                                                own(command.getSource(), EntityArgument.getEntities(command, "owned entities"), command.getSource().getEntityOrException())
                                        )
                        ))
                .then(Commands.literal("remove")
                        .then(Commands.argument("owned entities", EntityArgument.entities())
                                .executes((command) ->
                                        deown(command.getSource(), EntityArgument.getEntities(command, "owned entities"))
                                )
                        )
                ));
    }

    private static int own(CommandSourceStack source, Collection<? extends Entity> owned, Entity owner) {
        int count = 0;
        Entity tmpEntity = null;
        for (Entity entity: owned) {
            if (entity != owner) {
                PVZEntityCapability cap = entity.getCapability(PVZEntityCapability.CAP).orElse(null);
                if (cap != null && cap.getOwner() != owner) {
                    cap.setOwner(owner);
                    count ++;
                    tmpEntity = entity;
                }
            }
        }
        if (count == 1) {
            source.sendSuccess(Component.translatable("commands.pvz.own.own", tmpEntity.getName(), owner.getName()), true);
        } else {
            source.sendSuccess(Component.translatable("commands.pvz.own.owns", owner.getName(), count), true);
        }
        return count;
    }
    private static int deown(CommandSourceStack source, Collection<? extends Entity> owned) {
        int count = 0;
        Entity tmpEntity = null;
        for (Entity entity: owned) {
            PVZEntityCapability cap = entity.getCapability(PVZEntityCapability.CAP).orElse(null);
            if (cap != null) {
                cap.setOwner(null);
                count ++;
                tmpEntity = entity;
            }
        }
        if (count == 1) {
            source.sendSuccess(Component.translatable("commands.pvz.own.deown", tmpEntity.getName()), true);
        } else {
            source.sendSuccess(Component.translatable("commands.pvz.own.deowns", count), true);
        }
        return count;
    }

}
