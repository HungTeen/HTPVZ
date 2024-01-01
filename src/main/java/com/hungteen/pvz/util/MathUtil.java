package com.hungteen.pvz.util;

import net.minecraft.util.RandomSource;

public class MathUtil {
    /**
     * get random from - range to range.
     */
    public static int getRandomInRange(RandomSource rand, int range) {
        return rand.nextInt(range << 1 | 1) - range;
    }
}
