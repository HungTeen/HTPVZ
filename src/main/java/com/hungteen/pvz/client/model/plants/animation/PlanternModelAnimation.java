package com.hungteen.pvz.client.model.plants.animation;// Save this class in your mod and generate all required imports

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Made with Blockbench 4.9.3
 * Exported for Minecraft version 1.19 or later with Mojang mappings
 * @author Author
 */
public class PlanternModelAnimation {
	public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(2.0F).looping()
		.addAnimation("cover", new AnimationChannel(AnimationChannel.Targets.POSITION,
				new Keyframe(0.04F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.52F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.52F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.64F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.12F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leave_nw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				new Keyframe(0.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.48F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.88F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.32F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leave_ne", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				new Keyframe(0.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.48F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.88F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.32F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leave_se", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				new Keyframe(0.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.48F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.88F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.32F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leave_sw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				new Keyframe(0.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.48F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.88F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.32F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("eyes_closed", new AnimationChannel(AnimationChannel.Targets.POSITION,
				new Keyframe(0.08F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.72F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.76F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("eyes_open", new AnimationChannel(AnimationChannel.Targets.POSITION,
				new Keyframe(0.08F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.72F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.76F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("inner_light", new AnimationChannel(AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.64F, KeyframeAnimations.posVec(0.0F, -1.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.12F, KeyframeAnimations.posVec(0.0F, -1.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();
}