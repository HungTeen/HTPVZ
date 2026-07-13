package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.model.plants.PeaShooterModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.PeaShooter;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PeaShooterRenderer<T extends PeaShooter, M extends PeaShooterModel<T>> extends MobRenderer<T, M> {

    ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/pea_shooter/pea_shooter.png");
    ResourceLocation FIRE_TEXTURE = Util.prefix("textures/entity/plants/pea_shooter/fire_pea_shooter.png");

    public PeaShooterRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new PeaShooterModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("pea_shooter:main"))), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.hasSkill(PeaShooter.FIRE_SKILL_NAME) ? FIRE_TEXTURE : TEXTURE;
    }
}
