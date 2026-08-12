package com.hungteen.pvz.client.model.zombie;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.zombie.animation.ChorusTerminatorModelAnimation;
import com.hungteen.pvz.common.entity.zombies.ChorusTerminatorBoss;
import com.hungteen.pvz.common.entity.zombies.ChorusTerminatorPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;

public class ChorusTerminatorModel<T extends ChorusTerminatorBoss> extends HierarchicalModel<T> {
	private final ModelPart total;
	private final ModelPart shell;
	private final ModelPart down;
	private final ModelPart up;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart capsule;
	private final ModelPart zomboss;
	private final ModelPart flag;
	private final ModelPart bone;
	private final ModelPart mouth;
	private final ModelPart eye;
	private final ModelPart eyeball;
	private final ModelPart eyeblackup;
	private final ModelPart eyeblackdown;
	private final ModelPart shulker;
	private final ModelPart gun;
	private final ModelPart chorus_left;
	private final ModelPart chorus_left_render;
	private final ModelPart chorus_right;
	private final ModelPart chorus_right_render;
	private final ModelPart leg_lf;
	private final ModelPart leg_lfm;
	private final ModelPart leg_lfb;
	private final ModelPart leg_rf;
	private final ModelPart leg_rfm;
	private final ModelPart leg_rfb;
	private final ModelPart leg_lb;
	private final ModelPart leg_lbm;
	private final ModelPart leg_lbb;
	private final ModelPart leg_rb;
	private final ModelPart leg_rbm;
	private final ModelPart leg_rbb;
	private final Map<ModelPart, ModelPart> legs;

	public ChorusTerminatorModel(ModelPart root) {
		this.total = root.getChild("total");
		this.shell = this.total.getChild("shell");
		this.down = this.shell.getChild("down");
		this.up = this.shell.getChild("up");
		this.body = this.total.getChild("body");
		this.head = this.body.getChild("head");
		this.capsule = this.head.getChild("capsule");
		this.zomboss = this.capsule.getChild("zomboss");
		this.flag = this.head.getChild("flag");
		this.bone = this.flag.getChild("bone");
		this.mouth = this.head.getChild("mouth");
		this.eye = this.head.getChild("eye");
		this.eyeball = this.eye.getChild("eyeball");
		this.eyeblackup = this.eyeball.getChild("eyeblackup");
		this.eyeblackdown = this.eyeball.getChild("eyeblackdown");
		this.shulker = this.eye.getChild("shulker");
		this.gun = this.eye.getChild("gun");
		this.chorus_left = this.head.getChild("chorus_left");
		this.chorus_left_render = this.chorus_left.getChild("chorus_left_render");
		this.chorus_right = this.head.getChild("chorus_right");
		this.chorus_right_render = this.chorus_right.getChild("chorus_right_render");
		this.leg_lf = this.body.getChild("leg_lf");
		this.leg_lfm = this.leg_lf.getChild("leg_lfm");
		this.leg_lfb = this.leg_lfm.getChild("leg_lfb");
		this.leg_rf = this.body.getChild("leg_rf");
		this.leg_rfm = this.leg_rf.getChild("leg_rfm");
		this.leg_rfb = this.leg_rfm.getChild("leg_rfb");
		this.leg_lb = this.body.getChild("leg_lb");
		this.leg_lbm = this.leg_lb.getChild("leg_lbm");
		this.leg_lbb = this.leg_lbm.getChild("leg_lbb");
		this.leg_rb = this.body.getChild("leg_rb");
		this.leg_rbm = this.leg_rb.getChild("leg_rbm");
		this.leg_rbb = this.leg_rbm.getChild("leg_rbb");
		legs = Map.of(leg_lf, leg_lfm, leg_rf, leg_rfm, leg_lb, leg_lbm, leg_rb, leg_rbm);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(1.0F, 24.0F, 0.0F));

		PartDefinition shell = total.addOrReplaceChild("shell", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition down = shell.addOrReplaceChild("down", CubeListBuilder.create().texOffs(0, 274).addBox(-43.0F, -40.0F, -42.0F, 84.0F, 40.0F, 84.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition up = shell.addOrReplaceChild("up", CubeListBuilder.create().texOffs(0, 0).addBox(-44.0F, -52.0F, -41.0F, 84.0F, 62.0F, 84.0F, new CubeDeformation(0.0F))
				.texOffs(0, 146).addBox(-46.0F, -30.0F, -43.0F, 88.0F, 40.0F, 88.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -32.0F, -1.0F));

		PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(516, 532).addBox(-6.0F, -15.0F, -5.0F, 10.0F, 28.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -25.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(336, 0).addBox(-41.0F, -50.0F, -40.0F, 80.0F, 8.0F, 80.0F, new CubeDeformation(0.0F))
				.texOffs(336, 346).addBox(-31.0F, -50.0F, -30.0F, 60.0F, 8.0F, 60.0F, new CubeDeformation(0.0F))
				.texOffs(174, 414).addBox(-22.0F, -54.0F, -28.0F, 42.0F, 10.0F, 42.0F, new CubeDeformation(0.0F))
				.texOffs(336, 274).addBox(-33.0F, -24.0F, -32.0F, 64.0F, 8.0F, 64.0F, new CubeDeformation(0.0F))
				.texOffs(352, 88).addBox(-29.0F, -42.0F, -28.0F, 56.0F, 18.0F, 56.0F, new CubeDeformation(0.0F))
				.texOffs(352, 162).addBox(-25.0F, -16.0F, -24.0F, 48.0F, 15.0F, 48.0F, new CubeDeformation(0.0F))
				.texOffs(484, 506).addBox(-10.0F, -1.0F, -9.0F, 18.0F, 8.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));

		PartDefinition capsule = head.addOrReplaceChild("capsule", CubeListBuilder.create().texOffs(342, 414).addBox(-18.0F, -22.0F, -18.0F, 36.0F, 18.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -50.0F, -7.0F));

		PartDefinition zomboss = capsule.addOrReplaceChild("zomboss", CubeListBuilder.create(), PartPose.offset(-1.0F, -2.0F, -3.0F));

		PartDefinition flag = head.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(352, 263).addBox(-7.0F, -37.0F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(336, 88).addBox(-1.0F, -35.0F, -1.0F, 2.0F, 35.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -50.0F, 27.0F));

		PartDefinition bone = flag.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 543).addBox(-7.0F, -1.0F, -2.0F, 14.0F, 26.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -36.0F, 0.0F));

		PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(0, 398).addBox(-26.0F, -5.0F, -30.0F, 52.0F, 17.0F, 35.0F, new CubeDeformation(0.0F))
				.texOffs(577, 0).addBox(-25.0F, -29.0F, -4.0F, 50.0F, 29.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -12.0F, 4.0F));

		PartDefinition eye = head.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(76, 521).addBox(-9.0F, -11.0F, -1.0F, 20.0F, 20.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -21.0F, -32.0F));

		PartDefinition eyeball = eye.addOrReplaceChild("eyeball", CubeListBuilder.create().texOffs(30, 543).addBox(-8.0F, -8.0F, -3.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -1.0F, 0.0F));

		PartDefinition eyeblackup = eyeball.addOrReplaceChild("eyeblackup", CubeListBuilder.create().texOffs(336, 125).addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, -3.25F));

		PartDefinition eyeblackdown = eyeball.addOrReplaceChild("eyeblackdown", CubeListBuilder.create().texOffs(336, 130).addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.5F, -3.25F));

		PartDefinition shulker = eye.addOrReplaceChild("shulker", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, 1.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -1.0F));

		PartDefinition gun = eye.addOrReplaceChild("gun", CubeListBuilder.create().texOffs(0, 33).addBox(-7.0F, -8.0F, -6.0F, 14.0F, 14.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 6.0F));

		PartDefinition chorus_left = head.addOrReplaceChild("chorus_left", CubeListBuilder.create().texOffs(358, 523).addBox(-4.0F, -24.0F, 0.0F, 8.0F, 28.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(21.0F, -38.0F, 28.0F));

		PartDefinition chorus_left_render = chorus_left.addOrReplaceChild("chorus_left_render", CubeListBuilder.create(), PartPose.offset(0.0F, -23.0F, 4.0F));

		PartDefinition chorus_right = head.addOrReplaceChild("chorus_right", CubeListBuilder.create().texOffs(484, 532).addBox(-4.0F, -20.0F, 0.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-23.0F, -38.0F, 28.0F));

		PartDefinition chorus_right_render = chorus_right.addOrReplaceChild("chorus_right_render", CubeListBuilder.create(), PartPose.offset(0.0F, -20.0F, 4.0F));

		PartDefinition leg_lf = body.addOrReplaceChild("leg_lf", CubeListBuilder.create(), PartPose.offset(-1.0F, 3.0F, 0.0F));

		PartDefinition cube_r1 = leg_lf.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(141, 505).addBox(-7.0F, -39.0F, -29.0F, 4.0F, 39.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 8.0F, -4.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r2 = leg_lf.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(111, 475).addBox(-7.0F, -8.0F, -27.0F, 3.0F, 8.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 8.0F, -5.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition leg_lfm = leg_lf.addOrReplaceChild("leg_lfm", CubeListBuilder.create().texOffs(352, 225).addBox(-6.0F, -6.0F, -21.0F, 12.0F, 12.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 1.0F, -49.0F));

		PartDefinition leg_lfb = leg_lfm.addOrReplaceChild("leg_lfb", CubeListBuilder.create().texOffs(428, 225).addBox(-8.0F, -9.0F, -32.0F, 16.0F, 16.0F, 22.0F, new CubeDeformation(0.0F))
				.texOffs(134, 521).addBox(-10.0F, -11.0F, -10.0F, 18.0F, 18.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -21.0F));

		PartDefinition leg_rf = body.addOrReplaceChild("leg_rf", CubeListBuilder.create(), PartPose.offset(-1.0F, 3.0F, 0.0F));

		PartDefinition cube_r3 = leg_rf.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(141, 505).addBox(-7.0F, -39.0F, -29.0F, 4.0F, 39.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 8.0F, -4.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r4 = leg_rf.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(111, 475).addBox(-7.0F, -8.0F, -27.0F, 3.0F, 8.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, 8.0F, -5.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition leg_rfm = leg_rf.addOrReplaceChild("leg_rfm", CubeListBuilder.create().texOffs(408, 468).addBox(-6.0F, -6.0F, -21.0F, 12.0F, 12.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 1.0F, -49.0F));

		PartDefinition leg_rfb = leg_rfm.addOrReplaceChild("leg_rfb", CubeListBuilder.create().texOffs(484, 468).addBox(-8.0F, -9.0F, -32.0F, 16.0F, 16.0F, 22.0F, new CubeDeformation(0.0F))
				.texOffs(190, 521).addBox(-8.0F, -11.0F, -10.0F, 18.0F, 18.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -21.0F));

		PartDefinition leg_lb = body.addOrReplaceChild("leg_lb", CubeListBuilder.create(), PartPose.offset(-1.0F, 3.0F, 0.0F));

		PartDefinition cube_r5 = leg_lb.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(111, 475).addBox(-7.0F, -8.0F, -27.0F, 3.0F, 8.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 8.0F, -5.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r6 = leg_lb.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(141, 505).addBox(-7.0F, -39.0F, -29.0F, 4.0F, 39.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 8.0F, -4.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition leg_lbm = leg_lb.addOrReplaceChild("leg_lbm", CubeListBuilder.create().texOffs(352, 225).addBox(-6.0F, -6.0F, -21.0F, 12.0F, 12.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 1.0F, -49.0F));

		PartDefinition leg_lbb = leg_lbm.addOrReplaceChild("leg_lbb", CubeListBuilder.create().texOffs(428, 225).addBox(-8.0F, -9.0F, -32.0F, 16.0F, 16.0F, 22.0F, new CubeDeformation(0.0F))
				.texOffs(134, 521).addBox(-8.0F, -11.0F, -10.0F, 18.0F, 18.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -21.0F));

		PartDefinition leg_rb = body.addOrReplaceChild("leg_rb", CubeListBuilder.create(), PartPose.offset(-1.0F, 3.0F, 0.0F));

		PartDefinition cube_r7 = leg_rb.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(141, 505).addBox(-7.0F, -39.0F, -29.0F, 4.0F, 39.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 8.0F, -4.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r8 = leg_rb.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(111, 475).addBox(-7.0F, -8.0F, -27.0F, 3.0F, 8.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, 8.0F, -5.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition leg_rbm = leg_rb.addOrReplaceChild("leg_rbm", CubeListBuilder.create().texOffs(408, 468).addBox(-6.0F, -6.0F, -21.0F, 12.0F, 12.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 1.0F, -49.0F));

		PartDefinition leg_rbb = leg_rbm.addOrReplaceChild("leg_rbb", CubeListBuilder.create().texOffs(484, 468).addBox(-8.0F, -9.0F, -32.0F, 16.0F, 16.0F, 22.0F, new CubeDeformation(0.0F))
				.texOffs(190, 521).addBox(-10.0F, -11.0F, -10.0F, 18.0F, 18.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -21.0F));

		return LayerDefinition.create(meshdefinition, 1024, 1024);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.idleAnimationState, ChorusTerminatorModelAnimation.spiderIdle, ageInTicks);
		this.shell.visible = false;
		float pTick = Minecraft.getInstance().getPartialTick();
		float yRot = (Mth.rotLerp(pTick, entity.yBodyRotO, entity.yBodyRot)) / 57.3f;

		double xDiff = Mth.lerp(pTick, entity.body.xOld, entity.body.getX()) - Mth.lerp(pTick, entity.xOld, entity.getX());
		double yDiff = Mth.lerp(pTick, entity.body.yOld, entity.body.getY()) - Mth.lerp(pTick, entity.yOld, entity.getY());
		double zDiff = Mth.lerp(pTick, entity.body.zOld, entity.body.getZ()) - Mth.lerp(pTick, entity.zOld, entity.getZ());
		this.body.x += (float) (xDiff * Math.cos(yRot) + zDiff * Math.sin(yRot)) * 16;
		this.body.y -= (float) yDiff * 16 - 48;
		this.body.z += (float) (xDiff * Math.sin(yRot) - zDiff * Math.cos(yRot)) * 16;
		Map<ModelPart, ChorusTerminatorPart> map = Map.of(leg_lf, entity.legLeftFront, leg_rf, entity.legRightFront, leg_lb, entity.legLeftBack, leg_rb, entity.legRightBack);
		for (ModelPart upper : legs.keySet()) {
			ChorusTerminatorPart part = map.get(upper);
			Vec3 rot = handleLegs(new Vec3((Math.cos(yRot) * this.body.x + Math.sin(yRot) * this.body.z) / 16
							, - this.body.y / 16 - 0.5
							, (Math.sin(yRot) * this.body.x - Math.cos(yRot) * this.body.z) / 16)
					, new Vec3(
							Mth.lerp(pTick, part.xOld, part.getX()),
							Mth.lerp(pTick, part.yOld, part.getY()),
							Mth.lerp(pTick, part.zOld, part.getZ())
					).subtract(new Vec3(
							Mth.lerp(pTick, entity.xOld, entity.getX()),
							Mth.lerp(pTick, entity.yOld, entity.getY()),
							Mth.lerp(pTick, entity.zOld, entity.getZ())
							))
					, 3f);
			if (rot == null) continue;
			ModelPart lower = legs.get(upper);
			upper.xRot = (float) rot.x;
			upper.yRot = (float) rot.y - yRot;
			lower.xRot = (float) rot.z;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return total;
	}

	private static @Nullable Vec3 handleLegs(Vec3 start, Vec3 end, float bottomLen) {
		final float topLen = 3f;
		double dx = end.x - start.x;
		double dy = end.y - start.y;
		double dz = end.z - start.z;
		double lenSqr = dx * dx + dy * dy + dz * dz;
		if (lenSqr == 0) {
			dy += 0.01;
			lenSqr = dx * dx + dy * dy + dz * dz;
		}
		double maxLenSqr = (topLen + bottomLen) * (topLen + bottomLen);
		double minLenSqr = (topLen - bottomLen) * (topLen - bottomLen);
		if (lenSqr > maxLenSqr) {
			double ratey = dy / dx;
			double ratez = dz / dx;
			dx = Math.signum(dx) * Math.sqrt(maxLenSqr / (1 + ratey * ratey + ratez * ratez));
			dy = ratey * dx;
			dz = ratez * dx;
			lenSqr = maxLenSqr;
		} else if (lenSqr < minLenSqr) {
			double ratey = dy / dx;
			double ratez = dz / dx;
			dx = Math.signum(dx) * Math.sqrt(minLenSqr / (1 + ratey * ratey + ratez * ratez));
			dy = ratey * dx;
			dz = ratez * dx;
			lenSqr = minLenSqr;
		}
		float topXAngle = 1.57F + (float) (Math.acos(dy / Math.sqrt(lenSqr))
				+ Math.acos((bottomLen * bottomLen - lenSqr - topLen * topLen) / 2 / Math.sqrt(lenSqr) / bottomLen));
		float topYAngle = (float) Math.atan2(dz, dx) - 1.57F;
		float topBottomAngle = (float) Math.acos(Math.min(1, Math.max(-1, (lenSqr - topLen * topLen - bottomLen * bottomLen) / (2 * topLen * bottomLen))));

		return new Vec3(topXAngle, topYAngle, topBottomAngle);
	}
}