package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.PVZPlant;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class TallNut extends WallNut{
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.wall_nut.wall_nut_first_aid", PVZItems.ORIGIN_ESSENCE, 4, 4, 0, 0)
    );
    public TallNut(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PVZPlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 50D)
                .add(Attributes.ARMOR, 60D)
                .add(Attributes.ARMOR_TOUGHNESS, 30D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
}
