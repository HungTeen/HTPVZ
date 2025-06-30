package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WallNutZombie extends AbstractZombotanyZombie {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_0.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_2.png");

    public WallNutZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    public ResourceLocation getPlantTextureLocation() {
        float healthPercent = this.getHealth()/this.getMaxHealth();
        return healthPercent > 0.67 ? STATE0 : (healthPercent > 0.33 ? STATE1 : STATE2);
    }

    @Override
    public String getPlantModelClassName() {
        return "com.hungteen.pvz.client.model.plants.WallNutModel";
    }

    @Override
    public float getPlantHeadScale() {
        return 0.75F;
    }

    @Override
    public float getPlantHeadOffsetY() {
        return 0.25F;
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.ARMOR, 25D)
                .add(Attributes.ARMOR_TOUGHNESS, 20D)
                .add(Attributes.ATTACK_DAMAGE, 50D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
}