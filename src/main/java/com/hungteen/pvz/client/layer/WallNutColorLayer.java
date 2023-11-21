package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.Util;
import com.hungteen.pvz.client.model.plants.WallNutModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.WallNut;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

import static com.hungteen.pvz.common.entity.plants.WallNut.EXPLODE_COUNT;

public class WallNutColorLayer<T extends WallNut> extends RenderLayer<T, WallNutModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_bleach.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_bleach_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_bleach_2.png");
    private final WallNutModel<T> model;

    public WallNutColorLayer(RenderLayerParent<T, WallNutModel<T>> layerParent, EntityModelSet modelSet) {
        super(layerParent);
        this.model = new WallNutModel<>(modelSet.bakeLayer(PVZLayerHandler.LayerLocationMap.get("wall_nut:main")));
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int p_117351_, T wallNut, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        if (wallNut.hasSkill(this, 0) || (! wallNut.isInvisible() && wallNut.hasCustomName())) {
            int R, G, B;
            Vec3i color = getColor(wallNut.getName().getString());
            if (wallNut.hasSkill(this, 0) || color != null) {
                float healthPercent = wallNut.getHealth() / wallNut.getMaxHealth();
                R = color != null ? color.getX() : 256-0xFF;
                G = color != null ? color.getY() : wallNut.getEntityData().get(EXPLODE_COUNT) % 10 > 6 ? 256-0xFF : 256-0x44;
                B = color != null ? color.getZ() : wallNut.getEntityData().get(EXPLODE_COUNT) % 10 > 6 ? 256-0xFF : 256-0x33;
                coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model,
                        healthPercent > 0.67 ? STATE0 : (healthPercent > 0.33 ? STATE1 : STATE2),
                        stack, bufferSource, p_117351_, wallNut, p_117353_, p_117354_, p_117355_, p_117356_, p_117357_, p_117358_,
                        R, G, B);
            }
        }
    }

    private Vec3i getColor(String name) {
        char[] str = name.toUpperCase().toCharArray();
        int[] bytes = new int[6];
        final String compare = "0123456789ABCDEF";
        if (str.length == 6) {
            for (int i = 0; i < 6; i ++) {
                int tmp = compare.indexOf(str[i]);
                if (tmp == -1) {
                    return null;
                } else {
                    bytes[i] = tmp;
                }
            }
            return new Vec3i(256 - bytes[0]*16 - bytes[1], 256 - bytes[2]*16 - bytes[3], 256 - bytes[4]*16 - bytes[5]);
        }
        return null;
    }
}
