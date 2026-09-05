package com.hungteen.pvz.client.sound;

import com.hungteen.pvz.common.entity.plants.GatlingPea;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;

public class GatlingPeaSoundInstance extends AbstractTickableSoundInstance {
    private final GatlingPea gatlingPea;

    public static void add(GatlingPea gatlingPea) {
        ClientProxy.MC.getSoundManager().play(new GatlingPeaSoundInstance(gatlingPea));
    }

    protected GatlingPeaSoundInstance(GatlingPea gatlingPea) {
        super(PVZSoundEvents.GATLING_PEA_CONTINUAL_SHOOT.get(), SoundSource.NEUTRAL, gatlingPea.getRandom());
        this.gatlingPea = gatlingPea;
        this.x = (float)gatlingPea.getX();
        this.y = (float)gatlingPea.getY();
        this.z = (float)gatlingPea.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 1f;
    }

    @Override
    public void tick() {
        if (this.gatlingPea.isRemoved() || ! this.gatlingPea.controlledAnimationState.isStarted()) {
            this.stop();
        } else {
            this.x = (float)this.gatlingPea.getX();
            this.y = (float)this.gatlingPea.getY();
            this.z = (float)this.gatlingPea.getZ();
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return ! this.gatlingPea.isSilent() && this.gatlingPea.controlledAnimationState.isStarted();
    }

}