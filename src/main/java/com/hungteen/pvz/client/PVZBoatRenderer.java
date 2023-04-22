package com.hungteen.pvz.client;

import com.hungteen.pvz.common.entity.PVZBoat;
import com.hungteen.pvz.common.entity.PVZChestBoat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.state.properties.WoodType;

public class PVZBoatRenderer extends BoatRenderer {
    private final BoatModel model;
    public PVZBoatRenderer(EntityRendererProvider.Context p_234563_, boolean p_234564_) {
        super(p_234563_, p_234564_);
        model = this.createBoatModel(p_234563_, Boat.Type.OAK, p_234564_);
    }
    private BoatModel createBoatModel(EntityRendererProvider.Context p_234569_, Boat.Type p_234570_, boolean p_234571_) {
        ModelLayerLocation modellayerlocation = p_234571_ ? ModelLayers.createChestBoatModelName(p_234570_) : ModelLayers.createBoatModelName(p_234570_);
        return new BoatModel(p_234569_.bakeLayer(modellayerlocation), p_234571_);
    }
    private static ResourceLocation getTextureLocation(WoodType woodType, boolean hasChest) {
        return new ResourceLocation(hasChest ?
                "textures/entity/chest_boat/" + woodType.name() + ".png":
                "textures/entity/boat/" + woodType.name() + ".png");
    }
    public Pair<ResourceLocation, BoatModel> getModelWithLocation(Boat boat) {
        WoodType woodtype = (boat instanceof PVZBoat) ? (((PVZBoat) boat).getWoodType()) : ((PVZChestBoat) boat).getWoodType();
        boolean hasChest = !(boat instanceof PVZBoat);
        return Pair.of(getTextureLocation(woodtype, hasChest), model);
    }
}
