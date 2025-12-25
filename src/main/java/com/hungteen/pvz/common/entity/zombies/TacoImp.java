package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.TeammateTestingEvent;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
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
                .add(Attributes.MOVEMENT_SPEED, 0.18D);
    }

    @SubscribeEvent
    public static void onPlantCheckTeammate(TeammateTestingEvent event) {
        //won't be regarded as target by shooters/pults.
        if (! event.forCombat) return;
        if (event.A instanceof TacoImp || event.B instanceof TacoImp) {
            Entity other = event.A instanceof TacoImp ? event.B : event.A;
            event.currentResult = event.currentResult || other instanceof ShooterPlant;
        }
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2F));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, LivingEntity.class, (entity) -> entity instanceof IPlant,
            5, 1, 1.2D, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test));
    }
}
