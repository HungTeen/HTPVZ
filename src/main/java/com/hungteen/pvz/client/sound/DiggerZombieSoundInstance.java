package com.hungteen.pvz.client.sound;

import com.hungteen.pvz.common.entity.zombies.DiggerZombie;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;

public class DiggerZombieSoundInstance extends AbstractTickableSoundInstance {
    private final DiggerZombie zombie;

    public static void add(DiggerZombie zombie) {
        ClientProxy.MC.getSoundManager().play(new DiggerZombieSoundInstance(zombie));
    }

    protected DiggerZombieSoundInstance(DiggerZombie zombie) {
        super(PVZSoundEvents.DIGGER_ZOMBIE_DIG.get(), SoundSource.NEUTRAL, zombie.getRandom());
        this.zombie = zombie;
        this.x = (float)zombie.getX();
        this.y = (float)zombie.getY();
        this.z = (float)zombie.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
    }

    @Override
    public void tick() {
        if (this.zombie.isRemoved() || this.zombie.getPose() != Pose.SWIMMING) {
            this.stop();
        } else {
            this.x = (float)this.zombie.getX();
            this.y = (float)this.zombie.getY();
            this.z = (float)this.zombie.getZ();
            float f = (float)this.zombie.getDeltaMovement().horizontalDistance();
            if (f >= 0.01F) {
                this.pitch = Mth.clamp(this.pitch + 0.0025F, 0.0F, 1.0F);
                this.volume = Mth.lerp(Mth.clamp(f, 0.0F, 0.5F), 0.0F, 0.7F);
            } else {
                this.pitch = 0.0F;
                this.volume = 0.0F;
            }
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return ! this.zombie.isSilent() && this.zombie.getPose() == Pose.SWIMMING;
    }

}