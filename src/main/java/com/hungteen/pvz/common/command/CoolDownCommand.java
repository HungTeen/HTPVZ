package com.hungteen.pvz.common.command;

import com.hungteen.pvz.common.network.PlayerContinueCoolDownPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class CoolDownCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("item").then(Commands.literal("cooldown").requires((ctx) -> ctx.hasPermission(2))
                .then(Commands.literal("tick")
                        .executes(command -> tickCooldown(command.getSource())))
                .then(Commands.argument("item", ItemArgument.item(context))
                        .then(Commands.literal("set")
                                .then(Commands.literal("score").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective())
                                        .then(Commands.argument("total", IntegerArgumentType.integer())
                                                .executes(command -> setCoolDown(command.getSource(), ItemArgument.getItem(command, "item").getItem(), getScore(command.getSource(), ScoreHolderArgument.getName(command, "target"), ObjectiveArgument.getObjective(command, "objective")), IntegerArgumentType.getInteger(command, "total"))))
                                        .executes(command -> setCoolDown(command.getSource(), ItemArgument.getItem(command, "item").getItem(), getScore(command.getSource(), ScoreHolderArgument.getName(command, "target"), ObjectiveArgument.getObjective(command, "objective")))))))
                                .then(Commands.argument("ticks", IntegerArgumentType.integer())
                                        .then(Commands.argument("total", IntegerArgumentType.integer())
                                                .executes(command -> setCoolDown(command.getSource(), ItemArgument.getItem(command, "item").getItem(), IntegerArgumentType.getInteger(command, "ticks"), IntegerArgumentType.getInteger(command, "total"))))
                                        .executes(command -> setCoolDown(command.getSource(), ItemArgument.getItem(command, "item").getItem(), IntegerArgumentType.getInteger(command, "ticks")))))
                        .then(Commands.literal("remove")
                                .executes(command -> removeCoolDown(command.getSource(), ItemArgument.getItem(command, "item").getItem())))
                        .then(Commands.literal("get")
                                .executes(command -> getCoolDown(command.getSource(), ItemArgument.getItem(command, "item").getItem()))))
                .then(Commands.argument("predicate", ItemPredicateArgument.itemPredicate(context))
                        .then(Commands.literal("set")
                                .then(Commands.literal("score").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective())
                                        .then(Commands.argument("total", IntegerArgumentType.integer())
                                                .executes(command -> multiItems(
                                                        item -> {
                                                            try {
                                                                return setCoolDown(command.getSource(), item, getScore(command.getSource(), ScoreHolderArgument.getName(command, "target"), ObjectiveArgument.getObjective(command, "objective")), IntegerArgumentType.getInteger(command, "total"));
                                                            } catch (CommandSyntaxException e) {
                                                                throw new RuntimeException(e);
                                                            }
                                                        }
                                                        , command.getSource(), ItemPredicateArgument.getItemPredicate(command, "predicate"))))
                                        .executes(command -> multiItems(
                                                item -> {
                                                    try {
                                                        return setCoolDown(command.getSource(), item, getScore(command.getSource(), ScoreHolderArgument.getName(command, "target"), ObjectiveArgument.getObjective(command, "objective")));
                                                    } catch (CommandSyntaxException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                                , command.getSource(), ItemPredicateArgument.getItemPredicate(command, "predicate"))))))
                                .then(Commands.argument("ticks", IntegerArgumentType.integer())
                                        .then(Commands.argument("total", IntegerArgumentType.integer())
                                                .executes(command -> multiItems(
                                                        item -> setCoolDown(command.getSource(), item, IntegerArgumentType.getInteger(command, "ticks"), IntegerArgumentType.getInteger(command, "total"))
                                                        , command.getSource(), ItemPredicateArgument.getItemPredicate(command, "predicate"))))
                                        .executes(command -> multiItems(
                                                item -> setCoolDown(command.getSource(), item, IntegerArgumentType.getInteger(command, "ticks"))
                                                , command.getSource(), ItemPredicateArgument.getItemPredicate(command, "predicate")))))
                        .then(Commands.literal("remove")
                                .executes(command -> multiItems(item -> removeCoolDown(command.getSource(), item)
                                        , command.getSource(), ItemPredicateArgument.getItemPredicate(command, "predicate")))))));
    }
    public static int multiItems(Function<Item, Integer> function, CommandSourceStack source, Predicate<ItemStack> predicate) {
        ServerPlayer player = source.getPlayer();
        int resultCount = 0;
        if (player != null) {
            List<ItemStack> list = new ArrayList<>();
            Set.of(player.getInventory().items, player.getInventory().armor, player.getInventory().offhand).forEach(list::addAll);
            for (ItemStack itemStack : list) {
                if (predicate.test(itemStack)) {
                    resultCount += function.apply(itemStack.getItem());
                }
            }
        }
        source.sendSuccess(Component.translatable("commands.pvz.cooldown_modified", player.getName(), resultCount), true);
        return resultCount;
    }
    private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((p_138534_, p_138535_) -> {
        return Component.translatable("commands.scoreboard.players.get.null", p_138534_, p_138535_);
    });
    private static int getScore(CommandSourceStack p_138499_, String p_138500_, Objective p_138501_) throws CommandSyntaxException {
        Scoreboard scoreboard = p_138499_.getServer().getScoreboard();
        if (!scoreboard.hasPlayerScore(p_138500_, p_138501_)) {
            throw ERROR_NO_VALUE.create(p_138501_.getName(), p_138500_);
        } else {
            Score score = scoreboard.getOrCreatePlayerScore(p_138500_, p_138501_);
            return score.getScore();
        }
    }
    public static int setCoolDown(CommandSourceStack source, Item item, int count) {
        return setCoolDown(source, item, count, 0);
    }

    public static int setCoolDown(CommandSourceStack source, Item item, int count, int total) {
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
        if (total > count) {
            coolDowns.cooldowns.computeIfPresent(item, (item1, instance) -> new ItemCooldowns.CooldownInstance(instance.startTime + count - total, instance.endTime));
            PlayerContinueCoolDownPacket.sync(player);
        }
        return tmp ? 1 : 0;
    }
    public static int getCoolDown(CommandSourceStack source, Item item) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        ItemCooldowns coolDowns = player.getCooldowns();
        if (coolDowns.cooldowns.containsKey(item)) {
            int tmp = coolDowns.cooldowns.get(item).endTime - coolDowns.tickCount;
            source.sendSuccess(Component.translatable("commands.pvz.cooldown_got", player.getName(), item.getName(item.getDefaultInstance()), tmp), true);
            return tmp;
        }
        source.sendSuccess(Component.translatable("commands.pvz.cooldown_has_ended", player.getName(), item.getName(item.getDefaultInstance())), true);
        return 0;
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
    public static int tickCooldown(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        boolean tmp = false;
        ItemCooldowns coolDowns = player.getCooldowns();
        coolDowns.tick();
        return tmp ? 1 : 0;
    }
}
