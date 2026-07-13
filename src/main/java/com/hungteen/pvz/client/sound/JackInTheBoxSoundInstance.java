package com.hungteen.pvz.client.sound;

import com.hungteen.pvz.common.item.JackInTheBoxItem;
import com.hungteen.pvz.common.network.ClientProxy;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JackInTheBoxSoundInstance extends AbstractTickableSoundInstance {
    private final LivingEntity entity;
    public final ItemStack itemStack;

    static final Map<UUID, SoundInstance> playingItems = new HashMap<>();

    public static void play(LivingEntity entity, ItemStack itemStack) {
        if (playingItems.containsKey(entity.getUUID())) return;
        if (itemStack.getItem() instanceof JackInTheBoxItem item) {
            SoundInstance instance = new JackInTheBoxSoundInstance(entity, itemStack, item.getHoldingSound(itemStack));
            playingItems.put(entity.getUUID(), instance);
            ClientProxy.MC.getSoundManager().play(instance);
        }
    }

    protected JackInTheBoxSoundInstance(LivingEntity entity, ItemStack itemStack, SoundEvent soundEvent) {
        super(soundEvent, SoundSource.NEUTRAL, entity.getRandom());
        this.entity = entity;
        this.itemStack = itemStack;
        this.x = (float)entity.getX();
        this.y = (float)entity.getY();
        this.z = (float)entity.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 1f;
    }

    @Override
    public void tick() {
        ItemStack itemStack = entity.getItemInHand(InteractionHand.MAIN_HAND);
        if (! (itemStack.getItem() instanceof JackInTheBoxItem)) itemStack = entity.getItemInHand(InteractionHand.OFF_HAND);
        if (playingItems.containsKey(entity.getUUID()) && playingItems.get(entity.getUUID()) == this
                && (this.entity.isRemoved() || itemStack.getItem() != this.itemStack.getItem())) {
            playingItems.remove(entity.getUUID());
            this.stop();
        } else {
            this.x = (float)this.entity.getX();
            this.y = (float)this.entity.getY();
            this.z = (float)this.entity.getZ();
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        boolean holding = (entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof JackInTheBoxItem
                || entity.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof JackInTheBoxItem);
        return ! this.entity.isSilent() && holding;
    }

}