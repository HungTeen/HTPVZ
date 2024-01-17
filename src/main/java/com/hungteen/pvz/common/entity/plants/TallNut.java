package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;

public class TallNut extends WallNut{
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.wall_nut.wall_nut_first_aid", PVZItems.ORIGIN_ESSENCE, 4, 4, 0, 0)
    );
    public TallNut(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 50D)
                .add(Attributes.ARMOR, 60D)
                .add(Attributes.ARMOR_TOUGHNESS, 30D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
}
