package com.hungteen.pvz.util;

import net.minecraft.util.Mth;

public class AnimationUtil {
    private static final float PI = 3.1415926535F;
    private static final float ANGLE_TO = PI / 180;
    public static float upDownUpDown(float x, float t, float scale) {
        final float sita = 2 * PI / t;
        return Mth.sin(sita * x) * scale;
    }
    public static float getUpDownUpDown(float x, float t, float maxAngle) {
        return upDownUpDown(x, t, maxAngle * ANGLE_TO);
    }
}
