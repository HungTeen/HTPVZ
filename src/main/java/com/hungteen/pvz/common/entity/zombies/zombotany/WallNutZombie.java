package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.common.entity.zombies.PVZZombie;
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

public class WallNutZombie extends PVZZombie implements IZombotany {
    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_0.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_2.png");

    public WallNutZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }
    @Override
    public void setSecondsOnFire(int seconds) {
        super.setSecondsOnFire(seconds * 3);//balance test.
    }
    @Override
    public ResourceLocation getPlantTextureLocation() {
        float healthPercent = this.getHealth() / this.getMaxHealth();
        return healthPercent > 0.67 ? STATE0 : (healthPercent > 0.33 ? STATE1 : STATE2);
    }

    @Override
    public EntityType<?> getPlantType() {
        return PVZEntities.WALL_NUT.get();
    }

    @Override
    public float getPlantHeadScale() {
        return 0.75F;
    }

    @Override
    public Vec3 getPlantHeadOffset() {
        return new Vec3(0, 2, 0);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.ARMOR, 25D)
                .add(Attributes.ARMOR_TOUGHNESS, 5D);
    }
}