package com.hungteen.pvz.common.command;

import com.hungteen.pvz.api.PVZAPI;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ZombieEventDataAccessor implements DataAccessor {

    public static final Function<String, DataCommands.DataProvider> PROVIDER = (p_139517_) -> new DataCommands.DataProvider() {
        public DataAccessor access(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
            return new ZombieEventDataAccessor(c.getSource().getLevel(), UuidArgument.getUuid(c, p_139517_));
        }

        public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> builder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> p_139528_) {
            return builder.then(Commands.literal("zombie_event").then(p_139528_.apply(Commands.argument(p_139517_, UuidArgument.uuid()))));
        }
    };
    private final ZombieEvent event;

    public ZombieEventDataAccessor(Level level, UUID uuid) {
        AtomicReference<ZombieEvent> event = new AtomicReference<>();
        level.getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> event.set(cap.getEvent(uuid)));
        this.event = event.get();
    }

    public void setData(CompoundTag tag) throws CommandSyntaxException {
        tag.putString("event_type", PVZAPI.get().getZombieEventType(this.event).toString());
        this.event.deserializeNBT(tag);
    }

    public CompoundTag getData() {
        CompoundTag tag = this.event.serializeNBT();
        tag.remove("event_type");
        return tag;
    }

    public Component getModifiedSuccess() {
        return Component.translatable("commands.data.zombie_event.modified", this.event.getDisplayName());
    }

    public Component getPrintSuccess(Tag p_139521_) {
        return Component.translatable("commands.data.zombie_event.query", this.event.getDisplayName(), NbtUtils.toPrettyComponent(p_139521_));
    }

    public Component getPrintSuccess(NbtPathArgument.NbtPath p_139513_, double p_139514_, int p_139515_) {
        return Component.translatable("commands.data.zombie_event.get", p_139513_, this.event.getDisplayName(), String.format(Locale.ROOT, "%.2f", p_139514_), p_139515_);
    }
}
