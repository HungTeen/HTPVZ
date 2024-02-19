package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class SeedArrow<T extends Entity> extends Arrow implements IHaveSkills {
    protected final Supplier<EntityType<T>> entitySupplier;
    private final List<Skill> skillList;
    public SeedArrow(EntityType<? extends Arrow> entityType, Level level, Supplier<EntityType<T>> entitySupplier, List<Skill> skillList) {
        super(entityType, level);
        this.entitySupplier = entitySupplier;
        this.skillList = skillList;
    }

    @Override
    public int getSkillVal(Object obj) {
        return 0;
    }

    @Override
    public void setSkillVal(Object obj, int value) {

    }
}
