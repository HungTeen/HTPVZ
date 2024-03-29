package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.common.entity.bullet.SeedArrow;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SeedArrowRenderer extends ArrowRenderer<SeedArrow> {
    public static final ResourceLocation SEED_ARROW_LOCATION = Util.prefix("textures/entity/projectiles/seed_arrow.png");

    public SeedArrowRenderer(EntityRendererProvider.Context p_173917_) {
        super(p_173917_);
    }

    @Override
    public ResourceLocation getTextureLocation(SeedArrow p_114482_) {
        return SEED_ARROW_LOCATION;
    }
}
