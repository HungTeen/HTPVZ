package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class TallNutZombie extends WallNutZombie {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/tall_nut/tall_nut.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/tall_nut/tall_nut_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/tall_nut/tall_nut_2.png");

    public TallNutZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    public ResourceLocation getPlantTextureLocation() {
        float healthPercent = this.getHealth() / this.getMaxHealth();
        return healthPercent > 0.67 ? STATE0 : (healthPercent > 0.33 ? STATE1 : STATE2);
    }
    @Override
    public EntityType<?> getPlantType() {
        return PVZEntities.TALL_NUT.get();
    }
    @Override
    public Vec3 getPlantHeadOffset() {
        return new Vec3(0, 4, -2);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 80D)
                .add(Attributes.ARMOR, 50D)
                .add(Attributes.ARMOR_TOUGHNESS, 10D);
    }
}