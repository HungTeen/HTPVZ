package com.hungteen.pvz.api.interfaces;

/**
 * ISun can be absorbed by ISunAbsorber. It doesn't directly interact with ISunContainer but ISunContainers may call
 * {@link ISun#getAmount()}.
 */
public interface ISun extends ISunAbsorber, ISunContainer {

    @Override
    default int getCapacity() {
        return 150;
    }

    /**
     * 开始被吸收
     */
    void onAbsorbedBy(ISunAbsorber entity);

    /**
     * 是否能被吸收
     */
    boolean canAttractThis(ISunAbsorber sunAbsorber);

    /**
     * @return Can be ISunAbsorber or player.
     */
    ISunAbsorber getAttractor();

    /**
     * 被消耗一定数量
     * @param amount 阳光数量
     */
    void onSunConsume(int amount);

    /**
     * 尝试设置吸引者，给阳光收集器、太阳神僵尸用
     * @return whether succeeded in setting attractor.
     */
    default boolean trySetAttractor(ISunAbsorber candidateAbsorber) {
        return false;
    }

}
