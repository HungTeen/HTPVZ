package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
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
                .then(Commands.argument("owned entities", EntityArgument.entities())
                        .then(Commands.argument("owner", EntityArgument.entity())
                                .executes((command) ->
                                        own(command.getSource(), EntityArgument.getEntities(command, "owned entities"), EntityArgument.getEntity(command, "owner"))
                                ))
                        .executes((command) ->
                                        own(command.getSource(), EntityArgument.getEntities(command, "owned entities"), command.getSource().getEntityOrException())
                                )
                ));
    }

    private static int own(CommandSourceStack source, Collection<? extends Entity> owned, Entity owner) {
        int count = 0;
        for (Entity entity: owned) {
            if (entity != owner) {
                PVZOwnedCapability cap = PVZOwnedCapability.getCap(entity);
                if (cap != null && cap.getOwner() != owner) {
                    cap.setOwner(owner);
                    count ++;
                }
            }
        }
        source.sendSuccess(Component.translatable(owner.getName() + " owned " + count + " entities."), true);
        return count;
    }

}
