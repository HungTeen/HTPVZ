package com.hungteen.pvz.client.model.plants.animation;// Save this class in your mod and generate all required imports

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Made with Blockbench 4.9.4
 * Exported for Minecraft version 1.19 or later with Mojang mappings
 * @author Author
 */
public class TorchWoodModelAnimation {
	public static final AnimationDefinition animation = AnimationDefinition.Builder.withLength(3.16F).looping()
		.addAnimation("in", new AnimationChannel(AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.16F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.52F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.16F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("eyebrow", new AnimationChannel(AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.28F, KeyframeAnimations.posVec(0.0F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(3.16F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();
}