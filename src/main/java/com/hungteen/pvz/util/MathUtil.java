package com.hungteen.pvz.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

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

    public static BlockPos posFromUuid(UUID uuid) {
        return posFromUuid(uuid, 0xa975c974);
    }

    public static UUID posToUuid(BlockPos pos) {
        return posToUuid(pos, 0xa975c974);
    }

    public static BlockPos posFromUuid(UUID uuid, int prefix) {
        int[] arr = UUIDUtil.uuidToIntArray(uuid);
        if (prefix != arr[0]) return null;
        return new BlockPos(arr[1], arr[2], arr[3]);
    }
    public static UUID posToUuid(BlockPos pos, int prefix) {
        int[] arr = {prefix, pos.getX(), pos.getY(), pos.getZ()};
        return UUIDUtil.uuidFromIntArray(arr);
    }
}
