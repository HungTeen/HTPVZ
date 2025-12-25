package com.hungteen.pvz.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class InvasionCommand {
    //TODO 实现以下内容
    // ：add（指定uuid、主要invasionType、入侵等级level、入侵目标target(支持生物)）
    // ，modify (修改主invasionType或增加附属invasionType，修改入侵等级，修改入侵目标)
    // ，remove (指定uuid)
    // 再增加一个data get zombie_event和data modify zombie_event指令以便更细致的修改
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
//        dispatcher.register(Commands.literal("invasion")
//                .then(Commands.literal("add")
//                        .then(Commands.argument("level", IntegerArgumentType.integer())
//                                .executes()
//                                .then(Commands.))
//                .then(Commands.literal("remove"))));
    };
}
