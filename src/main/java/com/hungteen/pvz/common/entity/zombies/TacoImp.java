package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.TeammateTestingEvent;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class TacoImp extends Imp {
    //TODO add special sounds for Taco Imp.
    public TacoImp(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Imp.createAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D);
    }

    @SubscribeEvent
    public static void onPlantCheckTeammate(TeammateTestingEvent event) {
        //won't be regarded as target by shooters/pults.
        if (! event.forCombat) return;
        if ((event.A instanceof TacoImp || event.B instanceof TacoImp) && event.A.distanceToSqr(event.B) > 6) {
            event.currentResult = true;
        }
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(1, new PanicGoal(this, 1F));
    }

    @Override
    public SoundEvent getAmbientSound() {
        return PVZSoundEvents.TACO_IMP_AMBIENT.get();
    }
}
