package com.hungteen.pvz.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;

public class CoolDownCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("item").then(Commands.literal("cooldown").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("item", ItemArgument.item(context))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("ticks", IntegerArgumentType.integer())
                                                .executes(command -> addCoolDown(command.getSource(), ItemArgument.getItem(command, "item").getItem(), IntegerArgumentType.getInteger(command, "ticks")))))
                                .then(Commands.literal("remove")
                                        .executes(command -> removeCoolDown(command.getSource(), ItemArgument.getItem(command, "item").getItem())))))));
    }

    public static int addCoolDown(CommandSourceStack source, Item item, int count) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        boolean tmp = true;
        ItemCooldowns coolDowns = player.getCooldowns();
        if (coolDowns.cooldowns.containsKey(item)) {
            tmp = false;
        }
        coolDowns.addCooldown(item, count);
        return tmp ? 1 : 0;
    }

    public static int removeCoolDown(CommandSourceStack source, Item item) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        boolean tmp = false;
        ItemCooldowns coolDowns = player.getCooldowns();
        if (coolDowns.cooldowns.containsKey(item)) {
            coolDowns.removeCooldown(item);
            tmp = true;
        }
        return tmp ? 1 : 0;
    }
}
