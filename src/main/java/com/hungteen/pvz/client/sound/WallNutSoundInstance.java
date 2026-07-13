package com.hungteen.pvz.client.sound;

import com.hungteen.pvz.common.entity.plants.WallNut;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class WallNutSoundInstance extends AbstractTickableSoundInstance {
    private final WallNut wallNut;

    public static void add(WallNut wallNut) {
        ClientProxy.MC.getSoundManager().play(new WallNutSoundInstance(wallNut));
    }

    protected WallNutSoundInstance(WallNut wallNut) {
        super(PVZSoundEvents.WALL_NUT_ROLL.get(), SoundSource.NEUTRAL, wallNut.getRandom());
        this.wallNut = wallNut;
        this.x = (float)wallNut.getX();
        this.y = (float)wallNut.getY();
        this.z = (float)wallNut.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
    }

    @Override
    public void tick() {
        if (this.wallNut.isRemoved() || ! this.wallNut.isBowling()) {
            this.stop();
        } else {
            this.x = (float)this.wallNut.getX();
            this.y = (float)this.wallNut.getY();
            this.z = (float)this.wallNut.getZ();
            float f = (float)this.wallNut.getDeltaMovement().horizontalDistance();
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
        return ! this.wallNut.isSilent() && ! EntityUtil.isLeavingGround(this.wallNut) && this.wallNut.isBowling();
    }

}