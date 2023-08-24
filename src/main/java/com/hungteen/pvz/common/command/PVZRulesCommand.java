package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.capability.pvzRules.PVZRulesCapability;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.HashMap;

public class PVZRulesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("pvzrules").requires((ctx) -> ctx.hasPermission(2));
        HashMap<String, Boolean> booleanMap = PVZRulesCapability.initBooleanMap();
        for (String key : booleanMap.keySet()) {
            builder.then(Commands.literal(key)
                    .then(Commands.argument("value", BoolArgumentType.bool()
                                    )
                                    .executes((command) -> setRuleValue(command.getSource(), key, BoolArgumentType.getBool(command, "value")))
                    )
                    .executes((command) -> getRuleValue(command.getSource(), key))
            );
        }
        dispatcher.register(builder);
    }

    private static int getRuleValue(CommandSourceStack source, String key) {
        boolean value = PVZRulesCapability.get().booleanMap.get(key);
        source.sendSuccess(Component.translatable("commands.gamerule.query", key, value), true);
        return value ? 1 : 0;
    }

    private static int setRuleValue(CommandSourceStack source, String key, Boolean value) {
        boolean curValue = PVZRulesCapability.get().booleanMap.get(key);
        if (curValue != value) {
            PVZRulesCapability.get().booleanMap.put(key, value);
            source.sendSuccess(Component.translatable("commands.gamerule.set", key, value), true);
            return 1;
        } else {
            source.sendSuccess(Component.translatable("The value of " + key + " has not been changed. It's originally " + value + "."), true);
            return 0;
        }
    }
}
