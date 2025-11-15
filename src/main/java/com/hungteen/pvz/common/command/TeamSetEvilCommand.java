package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.world.PVZSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.scores.PlayerTeam;

public class TeamSetEvilCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("team").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.literal("modify")
                        .then(Commands.argument("team", TeamArgument.team())
                                .then(Commands.literal("pvz:isEvil").then(Commands.argument("isEvil", BoolArgumentType.bool()).executes(
                                        (cmd) -> setTeamEvil(cmd.getSource(), TeamArgument.getTeam(cmd, "team"), BoolArgumentType.getBool(cmd, "isEvil"))
                                )))
                        )
                );
        dispatcher.register(builder);
    }

    public static int setTeamEvil(CommandSourceStack source, PlayerTeam team, boolean isEvil) {
        ServerLevel level = source.getLevel();
        int result = PVZSavedData.setEvil(level.getScoreboard(), team.getName(), isEvil);
        if (result == 1) {
            source.sendSuccess(Component.translatable(isEvil ? "commands.pvz.team.add_evil" : "commands.pvz.team.remove_evil", team.getDisplayName()), true);
        } else {
            source.sendSuccess(Component.translatable("commands.pvz.team.not_changed_evilness", team.getDisplayName()), true);
        }
        return result;
    }
}
