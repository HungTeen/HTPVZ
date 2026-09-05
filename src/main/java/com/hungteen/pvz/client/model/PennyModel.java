package com.hungteen.pvz.client.model;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.entity.npcs.Penny;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class PennyModel<T extends Penny> extends HierarchicalModel<T> {
	private final ModelPart all;
	private final ModelPart body;
	private final ModelPart Wing;
	private final ModelPart RightWing;
	private final ModelPart LeftWing;
	private final ModelPart Escape_Pipe;
	private final ModelPart Left;
	private final ModelPart Right;
	private final ModelPart Door;
	private final ModelPart LeftDoor;
	private final ModelPart RightDoor;
	private final ModelPart Radar;
	private final ModelPart bone;
	private final ModelPart Antenna;
	private final ModelPart Coil;
	private final ModelPart FireExtinguisher;
	private final ModelPart car_front;
	private final ModelPart other;
	private final ModelPart Food;
	private final ModelPart HarpoonGun;
	private final ModelPart Wire;
	private final ModelPart RearviewMirror;
	private final ModelPart bottom;
	private final ModelPart top;
	private final ModelPart WarningLight;
	private final ModelPart Megaphone;
	private final ModelPart Skull;
	private final ModelPart back;
	private final ModelPart FrontLeftTire;
	private final ModelPart FrontRightTire;
	private final ModelPart BackLeftTire;
	private final ModelPart BackRightTire;

	public PennyModel(ModelPart root) {
		this.all = root.getChild("all");
		this.body = this.all.getChild("body");
		this.Wing = this.body.getChild("Wing");
		this.RightWing = this.Wing.getChild("RightWing");
		this.LeftWing = this.Wing.getChild("LeftWing");
		this.Escape_Pipe = this.body.getChild("Escape_Pipe");
		this.Left = this.Escape_Pipe.getChild("Left");
		this.Right = this.Escape_Pipe.getChild("Right");
		this.Door = this.body.getChild("Door");
		this.LeftDoor = this.Door.getChild("LeftDoor");
		this.RightDoor = this.Door.getChild("RightDoor");
		this.Radar = this.body.getChild("Radar");
		this.bone = this.Radar.getChild("bone");
		this.Antenna = this.body.getChild("Antenna");
		this.Coil = this.body.getChild("Coil");
		this.FireExtinguisher = this.body.getChild("FireExtinguisher");
		this.car_front = this.all.getChild("car_front");
		this.other = this.car_front.getChild("other");
		this.Food = this.other.getChild("Food");
		this.HarpoonGun = this.other.getChild("HarpoonGun");
		this.Wire = this.other.getChild("Wire");
		this.RearviewMirror = this.other.getChild("RearviewMirror");
		this.bottom = this.car_front.getChild("bottom");
		this.top = this.car_front.getChild("top");
		this.WarningLight = this.top.getChild("WarningLight");
		this.Megaphone = this.top.getChild("Megaphone");
		this.Skull = this.top.getChild("Skull");
		this.back = this.all.getChild("back");
		this.FrontLeftTire = this.all.getChild("FrontLeftTire");
		this.FrontRightTire = this.all.getChild("FrontRightTire");
		this.BackLeftTire = this.all.getChild("BackLeftTire");
		this.BackRightTire = this.all.getChild("BackRightTire");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition all = partdefinition.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(18.0239F, 24.0F, -2.2321F));

		PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -42.0F, -13.0F, 33.0F, 35.0F, 55.0F, new CubeDeformation(0.0F))
		.texOffs(0, 158).addBox(-12.0F, -38.0F, 47.0F, 25.0F, 25.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(40, 229).addBox(-7.0F, -33.0F, 42.0F, 15.0F, 15.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(246, 230).addBox(-13.0F, -47.0F, 28.0F, 2.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(246, 152).addBox(12.0F, -47.0F, 28.0F, 2.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(188, 105).addBox(-13.0F, -48.0F, 28.0F, 27.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.0622F, 0.0F, 2.2321F));

		PartDefinition Wing = body.addOrReplaceChild("Wing", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition RightWing = Wing.addOrReplaceChild("RightWing", CubeListBuilder.create().texOffs(46, 193).addBox(26.0F, -22.0F, 31.0F, 7.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(76, 209).addBox(26.0F, -22.0F, 15.0F, 7.0F, 9.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(0, 90).addBox(27.0F, -21.0F, 47.0F, 5.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(151, 134).addBox(26.0F, -22.0F, 7.0F, 7.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(79, 167).addBox(28.0F, -25.0F, 24.0F, 3.0F, 3.0F, 23.0F, new CubeDeformation(0.0F))
		.texOffs(151, 99).addBox(25.0F, -18.0F, 15.0F, 2.0F, 2.0F, 33.0F, new CubeDeformation(0.0F))
		.texOffs(24, 42).addBox(26.0F, -17.5F, 48.0F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-49.0F, 0.0F, -4.0F));

		PartDefinition cube_r1 = RightWing.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 89).addBox(0.0F, -13.0F, -37.0F, 0.0F, 13.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(30.0F, -22.0F, 50.0F, 0.0F, 0.0F, -0.6109F));

		PartDefinition LeftWing = Wing.addOrReplaceChild("LeftWing", CubeListBuilder.create().texOffs(171, 67).addBox(-21.0F, -25.0F, 25.0F, 3.0F, 3.0F, 23.0F, new CubeDeformation(0.0F))
		.texOffs(151, 136).addBox(-17.05F, -18.0F, 16.0F, 2.0F, 2.0F, 33.0F, new CubeDeformation(0.0F))
		.texOffs(30, 42).addBox(-16.05F, -17.5F, 49.0F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(200, 194).addBox(-23.0F, -22.0F, 16.0F, 7.0F, 9.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(70, 158).addBox(-23.0F, -22.0F, 8.0F, 7.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 193).addBox(-23.0F, -22.0F, 32.0F, 7.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(78, 90).addBox(-22.0F, -21.0F, 48.0F, 5.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(40.0F, 0.0F, -5.0F));

		PartDefinition cube_r2 = LeftWing.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(90, 54).addBox(0.0F, -13.0F, -36.0F, 0.0F, 13.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-20.0F, -22.0F, 51.0F, 0.0F, 0.0F, 0.6109F));

		PartDefinition Escape_Pipe = body.addOrReplaceChild("Escape_Pipe", CubeListBuilder.create(), PartPose.offset(26.0F, -31.6464F, 49.2678F));

		PartDefinition Left = Escape_Pipe.addOrReplaceChild("Left", CubeListBuilder.create().texOffs(120, 249).addBox(-8.7814F, -35.25F, -5.2537F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(130, 221).addBox(-10.7814F, -2.25F, -12.2537F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(28, 245).addBox(-9.7814F, -19.25F, -6.2537F, 4.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3491F, 0.0F));

		PartDefinition cube_r3 = Left.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(151, 134).addBox(-1.2899F, 0.1455F, 17.0009F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.4915F, -50.2028F, -14.3438F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Right = Escape_Pipe.addOrReplaceChild("Right", CubeListBuilder.create().texOffs(106, 203).addBox(-39.0644F, -35.25F, -8.4141F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(186, 194).addBox(-41.0644F, -2.25F, -15.4141F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(176, 53).addBox(-40.0644F, -19.25F, -9.4141F, 4.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, 0.0F, 18.65F, 0.0F, -0.3491F, 0.0F));

		PartDefinition cube_r4 = Right.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 146).addBox(-4.7101F, 0.1455F, 17.0009F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-34.3543F, -50.2028F, -17.5042F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Door = body.addOrReplaceChild("Door", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition LeftDoor = Door.addOrReplaceChild("LeftDoor", CubeListBuilder.create().texOffs(200, 43).addBox(17.0F, -36.0F, -12.0F, 1.0F, 29.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(151, 144).addBox(23.0622F, -35.5F, -12.0F, 0.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(77, 103).addBox(18.0F, -9.0F, -11.5F, 4.0F, 0.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r5 = LeftDoor.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(173, 134).addBox(-1.0F, -4.0F, 1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(188, 105).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(188, 142).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 193).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(19.0F, -16.0F, -5.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r6 = LeftDoor.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(26, 34).addBox(-1.0F, -2.0F, -13.0F, 7.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.8655F, -36.7671F, 1.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition RightDoor = Door.addOrReplaceChild("RightDoor", CubeListBuilder.create().texOffs(170, 194).addBox(27.0F, -36.0F, -12.0F, 1.0F, 29.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(108, 144).addBox(21.9388F, -35.5F, -12.0F, 0.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(31, 0).addBox(23.0F, -9.0F, -11.5F, 4.0F, 0.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(-44.0F, 0.0F, 0.0F));

		PartDefinition cube_r7 = RightDoor.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 157).addBox(-1.0F, -4.0F, 1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(92, 158).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(187, 33).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(188, 134).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(26.0F, -16.0F, -5.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r8 = RightDoor.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -2.0F, -13.0F, 7.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(28.1345F, -36.7671F, -11.0F, -3.1416F, 0.0F, 2.618F));

		PartDefinition Radar = body.addOrReplaceChild("Radar", CubeListBuilder.create().texOffs(188, 134).addBox(-2.0F, -45.0F, 6.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(216, 3).addBox(0.0F, -54.0F, 9.5F, 12.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone = Radar.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(6.0383F, -50.0F, 14.0F));

		PartDefinition cube_r9 = bone.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(157, 95).addBox(-2.0F, -4.0F, -4.0F, 5.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(144, 223).addBox(3.0F, -7.0F, -7.0F, 3.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(108, 221).addBox(6.0F, -7.0F, -7.0F, 4.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9617F, -2.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition cube_r10 = bone.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(128, 252).addBox(-7.0F, 5.5F, -2.0F, 7.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(128, 253).addBox(0.0F, 5.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.9617F, -12.0F, 2.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition Antenna = body.addOrReplaceChild("Antenna", CubeListBuilder.create().texOffs(178, 121).addBox(-10.5F, -52.0F, 23.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-11.0F, -68.0F, 21.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(108, 169).addBox(-10.5F, -77.0F, 23.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(98, 255).addBox(-9.75F, -78.5F, 22.25F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(167, 187).addBox(-11.0F, -43.0F, 21.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -9.0F));

		PartDefinition Coil = body.addOrReplaceChild("Coil", CubeListBuilder.create().texOffs(188, 93).addBox(-23.0F, -3.5F, -5.0F, 29.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(230, 191).addBox(-13.0F, -5.5F, -5.0F, 9.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(9.0F, -39.5F, -5.0F));

		PartDefinition cube_r11 = Coil.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(2, 0).addBox(-1.0F, -13.0F, 0.5F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(88, 203).addBox(-2.0F, -16.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 245).addBox(-4.0F, -11.0F, -3.0F, 7.0F, 9.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition cube_r12 = Coil.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -13.0F, 0.5F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(76, 203).addBox(-2.0F, -16.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(243, 21).addBox(-4.0F, -11.0F, -3.0F, 7.0F, 9.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0466F, -0.4226F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition FireExtinguisher = body.addOrReplaceChild("FireExtinguisher", CubeListBuilder.create().texOffs(121, 24).addBox(-0.5F, 1.0F, -1.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(1.5F, 0.0F, 0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(133, 143).addBox(1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(17.5F, -34.0F, 4.0F));

		PartDefinition car_front = all.addOrReplaceChild("car_front", CubeListBuilder.create().texOffs(121, 0).addBox(-16.0F, -42.0F, -26.0F, 33.0F, 11.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(0, 90).addBox(-16.0F, -31.0F, -25.0F, 33.0F, 24.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(188, 153).addBox(-15.5F, -30.5F, -25.5F, 32.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(120, 187).addBox(-16.0F, -21.0F, -45.0F, 5.0F, 14.0F, 20.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(12.0F, -21.0F, -45.0F, 5.0F, 14.0F, 20.0F, new CubeDeformation(0.0F))
		.texOffs(121, 24).addBox(-11.0F, -15.8966F, -44.3813F, 23.0F, 9.0F, 20.0F, new CubeDeformation(0.0F))
		.texOffs(216, 126).addBox(-15.5F, -20.5F, -45.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(212, 163).addBox(12.5F, -20.5F, -45.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(236, 126).addBox(-4.0F, -21.5F, -35.0F, 9.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(30, 14).addBox(-4.5F, -22.0F, -36.0F, 10.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(236, 76).addBox(-9.0F, -15.0F, -45.0F, 19.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(188, 117).addBox(-16.5F, -10.0F, -45.5F, 34.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(30, 193).addBox(-11.5F, -10.75F, -46.5F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(150, 187).addBox(8.5F, -10.75F, -46.5F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(178, 237).addBox(-18.0F, -13.0F, -39.0F, 2.0F, 6.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(150, 187).addBox(17.0F, -13.0F, -39.0F, 2.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.0622F, 0.0F, 2.2321F));

		PartDefinition cube_r13 = car_front.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(187, 24).addBox(-9.0F, -2.0F, -1.0F, 19.0F, 1.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.6F, -40.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition cube_r14 = car_front.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(90, 103).addBox(-11.0F, -2.0F, -4.0F, 23.0F, 8.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.0F, -40.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition cube_r15 = car_front.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, 0.5F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(214, 0).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.5F, -17.5F, -47.0F, 0.2835F, 0.4253F, 0.1006F));

		PartDefinition cube_r16 = car_front.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(108, 171).addBox(-16.17F, -3.05F, -5.445F, 33.0F, 8.0F, 8.0F, new CubeDeformation(-0.002F)), PartPose.offsetAndRotation(0.17F, -38.1498F, -25.65F, 0.7854F, 0.0F, 0.0F));

		PartDefinition other = car_front.addOrReplaceChild("other", CubeListBuilder.create(), PartPose.offset(18.0F, -24.0F, -29.0F));

		PartDefinition Food = other.addOrReplaceChild("Food", CubeListBuilder.create().texOffs(236, 65).addBox(-7.0F, 3.0F, -7.25F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-37.5F, -1.0F, 12.25F));

		PartDefinition cube_r17 = Food.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(47, 49).addBox(-0.5F, -4.0F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(129, 132).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

		PartDefinition cube_r18 = Food.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(120, 146).addBox(-6.0F, 0.0F, -4.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.1745F, 0.0F));

		PartDefinition cube_r19 = Food.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(121, 0).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(188, 126).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.75F, 0.0F, -0.9163F, 0.0F));

		PartDefinition cube_r20 = Food.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(17, 16).addBox(-15.25F, -16.25F, 1.875F, 0.5F, 3.0F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.75F, 12.75F, 6.0F, 0.0F, -0.9163F, 0.0F));

		PartDefinition cube_r21 = Food.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(17, 0).addBox(-15.25F, -18.25F, 1.875F, 0.5F, 1.0F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.5793F, -2.7209F, 12.2937F, 0.7654F, -0.5657F, -1.062F));

		PartDefinition cube_r22 = Food.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(24, 53).addBox(-0.5F, -1.0F, 0.75F, 10.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(24, 52).addBox(-0.5F, -1.0F, 11.25F, 10.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 4.0F, -8.25F, 0.0F, 0.0F, 0.6109F));

		PartDefinition HarpoonGun = other.addOrReplaceChild("HarpoonGun", CubeListBuilder.create().texOffs(34, 45).addBox(17.5F, -40.4205F, -3.5495F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(44, 249).addBox(17.0F, -40.9205F, -1.5495F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(18.0F, -44.4205F, -12.5495F, 0.0F, 11.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.0F, 31.9205F, 5.5495F));

		PartDefinition cube_r23 = HarpoonGun.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(206, 194).addBox(16.9F, -37.0F, -19.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6545F, 0.0F, 0.0F));

		PartDefinition Wire = other.addOrReplaceChild("Wire", CubeListBuilder.create().texOffs(187, 24).addBox(-17.0F, -19.0355F, -58.6287F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(133, 24).addBox(-16.5F, -17.0F, -44.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 201).addBox(-17.0F, -16.5F, -48.5074F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(206, 126).addBox(-17.0F, -16.5F, -64.75F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(157, 111).addBox(-16.5F, -17.0F, -65.25F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(157, 111).addBox(16.5F, -17.0F, -65.25F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(206, 126).addBox(17.0F, -16.5F, -64.75F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(187, 24).addBox(17.0F, -19.0355F, -58.6287F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 201).addBox(17.0F, -16.5F, -48.5074F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(133, 24).addBox(16.5F, -17.0F, -44.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.0F, 24.0F, 50.25F));

		PartDefinition cube_r24 = Wire.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(200, 127).addBox(0.0F, -0.5F, -4.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(200, 127).addBox(-34.0F, -0.5F, -4.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, -15.5F, -47.8003F, -0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r25 = Wire.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(203, 49).addBox(0.0F, -0.5F, -4.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(203, 49).addBox(-34.0F, -0.5F, -4.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, -19.0355F, -57.9216F, 0.7854F, 0.0F, 0.0F));

		PartDefinition RearviewMirror = other.addOrReplaceChild("RearviewMirror", CubeListBuilder.create().texOffs(32, 151).addBox(30.5944F, 5.7071F, -1.4792F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(100, 109).addBox(29.5944F, 4.7071F, -0.9792F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 151).addBox(-3.4056F, 4.7071F, -0.9792F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(157, 121).addBox(-2.4056F, 5.7071F, -1.4792F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-32.5944F, -9.7071F, -0.0208F));

		PartDefinition cube_r26 = RearviewMirror.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(16, 8).addBox(0.0F, 1.7678F, -2.4749F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.6109F, 0.0F));

		PartDefinition cube_r27 = RearviewMirror.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(16, 13).addBox(0.0F, -0.5F, -2.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4056F, 0.7071F, -0.5792F, 0.7854F, 0.6109F, 0.0F));

		PartDefinition cube_r28 = RearviewMirror.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(16, 12).addBox(0.0F, -0.5F, -2.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(30.4944F, 0.7071F, -0.5792F, 0.7854F, -0.6109F, 0.0F));

		PartDefinition cube_r29 = RearviewMirror.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(16, 3).addBox(0.0F, 1.7678F, -2.4749F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(30.0888F, 0.0F, 0.0F, 0.0F, -0.6109F, 0.0F));

		PartDefinition cube_r30 = RearviewMirror.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(120, 136).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(71, 158).addBox(-31.9F, -1.0F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(30.4944F, 0.7071F, -0.5792F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r31 = RearviewMirror.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(0, 45).addBox(4.0F, -6.5F, 1.0F, 12.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(175, 93).addBox(-3.0F, -5.0F, 0.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 52).addBox(-15.0F, -6.5F, 1.0F, 12.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.5944F, 3.7071F, 1.0208F, 0.7854F, 0.0F, 0.0F));

		PartDefinition bottom = car_front.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 48).addBox(-11.0F, -6.5F, -33.5F, 23.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(236, 139).addBox(-6.0F, -7.5F, -34.5F, 13.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 139).addBox(-16.0F, -8.0F, 17.0F, 33.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(53, 132).addBox(-11.0F, -8.0F, 2.0F, 23.0F, 5.0F, 21.0F, new CubeDeformation(0.0F))
		.texOffs(169, 171).addBox(-7.0F, -8.0F, -19.0F, 15.0F, 2.0F, 21.0F, new CubeDeformation(0.0F))
		.texOffs(108, 132).addBox(-2.0F, -8.0F, -31.0F, 5.0F, 4.0F, 33.0F, new CubeDeformation(0.0F))
		.texOffs(0, 225).addBox(11.0F, -7.25F, -21.0F, 3.0F, 3.0F, 17.0F, new CubeDeformation(0.0F))
		.texOffs(49, 199).addBox(10.5F, -7.75F, -20.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(128, 193).addBox(10.5F, -7.75F, -8.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(167, 194).addBox(-13.5F, -7.75F, -20.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(150, 194).addBox(-13.5F, -7.75F, -8.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(213, 69).addBox(-13.0F, -7.25F, -21.0F, 3.0F, 3.0F, 17.0F, new CubeDeformation(0.0F))
		.texOffs(76, 193).addBox(-11.0F, -7.0F, 31.0F, 23.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 146).addBox(-5.5F, -7.0F, 23.0F, 12.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(74, 126).addBox(12.0F, -7.0F, 32.5F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(121, 38).addBox(-16.0F, -7.0F, 32.5F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(216, 43).addBox(-16.0F, -7.0F, 35.5F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(200, 0).addBox(14.0F, -7.0F, 35.5F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(144, 258).addBox(-16.0F, -10.0F, 41.0F, 33.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(146, 255).addBox(-7.0F, -13.0F, 42.5F, 16.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r32 = bottom.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(98, 103).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.5F, -5.5F, 44.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r33 = bottom.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(121, 7).addBox(-2.0F, -2.5F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.5F, -5.0F, 44.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r34 = bottom.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(49, 193).addBox(-2.5F, -7.5F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(208, 247).addBox(-2.0F, -7.0F, -7.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.5622F, -0.25F, -1.366F, 0.0F, -1.0472F, 0.0F));

		PartDefinition cube_r35 = bottom.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(188, 163).addBox(-2.5F, -7.5F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(249, 0).addBox(-2.0F, -7.0F, -7.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0622F, -0.25F, -2.2321F, 0.0F, 1.0472F, 0.0F));

		PartDefinition cube_r36 = bottom.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(225, 232).addBox(-1.5F, -3.0F, -11.0F, 3.0F, 3.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1109F, -4.25F, -22.7678F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r37 = bottom.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(65, 234).addBox(-1.5F, -3.0F, -11.0F, 3.0F, 3.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.1109F, -4.25F, -22.7678F, 0.0F, -0.7854F, 0.0F));

		PartDefinition top = car_front.addOrReplaceChild("top", CubeListBuilder.create().texOffs(120, 132).addBox(17.0F, -41.0F, -21.75F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(91, 109).addBox(-17.0F, -41.0F, -21.75F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition WarningLight = top.addOrReplaceChild("WarningLight", CubeListBuilder.create().texOffs(246, 84).addBox(-3.0F, -44.0F, -24.0F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(157, 111).addBox(-2.5F, -48.0F, -23.5F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(128, 257).addBox(-1.5F, -47.0F, -22.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Megaphone = top.addOrReplaceChild("Megaphone", CubeListBuilder.create().texOffs(88, 239).addBox(12.5F, -45.0F, -27.0F, 3.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(30, 200).addBox(12.0F, -45.5F, -29.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(30, 193).addBox(-14.5F, -45.0F, -27.0F, 3.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(200, 163).addBox(-15.0F, -45.5F, -29.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r38 = Megaphone.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(70, 182).addBox(-14.5F, -45.0F, -17.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(60, 158).addBox(12.5F, -45.0F, -17.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.3994F, -29.9027F, -0.6981F, 0.0F, 0.0F));

		PartDefinition Skull = top.addOrReplaceChild("Skull", CubeListBuilder.create(), PartPose.offset(0.0F, -43.0F, -32.0F));

		PartDefinition cube_r39 = Skull.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(86, 182).addBox(-1.5F, 2.0F, -5.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(27, 146).addBox(-8.5F, 4.75F, -1.0F, 18.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(23, 225).addBox(-2.5F, 1.5F, -1.0F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition back = all.addOrReplaceChild("back", CubeListBuilder.create().texOffs(44, 260).addBox(-0.5F, -41.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(44, 260).addBox(-4.5F, -41.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(44, 260).addBox(3.5F, -41.0F, 42.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(44, 264).addBox(13.0F, -41.0F, 42.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(44, 264).addBox(-15.0F, -41.0F, 42.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 252).addBox(9.0F, -26.0F, 43.0F, 7.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(82, 255).addBox(-15.0F, -26.0F, 43.0F, 7.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 260).addBox(10.5F, -15.5F, 42.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 265).addBox(-13.5F, -15.5F, 42.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.0622F, 0.0F, 2.2321F));

		PartDefinition FrontLeftTire = all.addOrReplaceChild("FrontLeftTire", CubeListBuilder.create().texOffs(235, 208).addBox(0.0F, -5.5F, -5.5F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.5239F, -5.5F, -30.2679F));

		PartDefinition FrontRightTire = all.addOrReplaceChild("FrontRightTire", CubeListBuilder.create().texOffs(235, 208).mirror().addBox(-7.0F, -5.5F, -5.5F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-28.5239F, -5.5F, -30.2679F));

		PartDefinition BackLeftTire = all.addOrReplaceChild("BackLeftTire", CubeListBuilder.create().texOffs(200, 219).addBox(31.2F, -7.0F, -7.0F, 6.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(-33.0239F, -7.0F, 21.2321F));

		PartDefinition BackRightTire = all.addOrReplaceChild("BackRightTire", CubeListBuilder.create().texOffs(200, 219).mirror().addBox(-6.2F, -7.0F, -7.0F, 6.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-34.0239F, -7.0F, 21.2321F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(T penny, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(penny.idleAnimationState, PennyModelAnimation.idle, ageInTicks);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		all.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return all;
	}
}