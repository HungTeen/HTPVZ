package com.hungteen.pvz.util;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class MathUtil {
    public static double horizontalDistSqrOf(Vec3 vec3) {
        return horizontalDistSqrBetween(vec3, Vec3.ZERO);
    }
    public static double horizontalDistSqrBetween(Vec3 from, Vec3 to) {
        return (from.x - to.x) * (from.x - to.x) + (from.z - to.z) * (from.z - to.z);
    }

    public static double horizontalDistSqrOf(Vec3i vec3) {
        return horizontalDistSqrBetween(vec3, Vec3i.ZERO);
    }
    public static double horizontalDistSqrBetween(Vec3i from, Vec3i to) {
        return (from.getX() - to.getX()) * (from.getX() - to.getX()) + (from.getZ() - to.getZ()) * (from.getZ() - to.getZ());
    }
}
