package com.hungteen.pvz.api;

import com.google.common.base.Suppliers;
import com.hungteen.pvz.PVZMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class PVZAPI {

    private static final Supplier<IPVZAPI> LAZY_INSTANCE = Suppliers.memoize(() -> {
        try {
            Class<?> classes = Class.forName("com.hungteen.pvz.PVZAPI");
            Constructor<?> constructor = classes.getDeclaredConstructor();
            return (IPVZAPI) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            PVZMod.LOGGER.warn("Unable to find PVZAPIImpl, using a dummy one");
            return DummyAPI.INSTANCE;
        }
    });

    /**
     */
    public static IPVZAPI get() {
        return LAZY_INSTANCE.get();
    }

    public interface IPVZAPI{
        //player
        /**The key for sun in playerStats and EntityCap. */
        String getSunResourceName();
        /**The key for invasion threat in Invasion and EntityCap.*/
        String getInvasionThreatResourceName();
        /**The sun a player have. For {@link com.hungteen.pvz.api.interfaces.ISunContainer ISunAbsorber}, use {@link com.hungteen.pvz.api.interfaces.ISunContainer#getAmount() getContainingSun()}.*/
        int getSun(Player player);
        /**Whether it costs player's sun when planting.*/
        boolean plantHaveCost(Player player);
        /**Whether player need cd after planting.*/
        boolean plantHaveCD(Player player);
        /**Whether {@link IPVZAPI#plantHaveCost(Player)} and {@link IPVZAPI#plantHaveCD(Player)} is automatically set depending on player's game mode.*/
        boolean autoSetCostAndCD(Player player);
        /**Set the above three values.*/
        void setPlantHaveCost(Player player, boolean value);
        void setPlantHaveCD(Player player, boolean value);
        void setAutoSetCostAndCD(Player player, boolean value);

        /**The attribute of max sun of a player. */
        @Nullable
        Attribute getMaxSunAttribute();
        //entity
        /**Check if entities are teammates. <b>CAN ONLY</b> call on server.
         * <br>I you want to check if an entity is attackable, use {@link IPVZAPI#canAttack(Entity, Entity)}.*/
        boolean isTeammate(Entity A, Entity B);
        /**
         * check can AttackGoal continue to attack target. <b>CAN ONLY</b> call on server.
         */
        boolean canAttack(Entity attacker, Entity target);
        float getPlantDamageResistance(Entity target);
        /**Check if an entity is in sculk situation. The result can be modified with {@link com.hungteen.pvz.api.events.SculkJudgmentEvent SculkJudgementEvent}.*/
        boolean isSculk(LivingEntity entity);
        /**This returns the type of a {@link ZombieEvent} and is used by ZombieEvent is self.*/
        ResourceLocation getZombieEventType(ZombieEvent event);
        /**To prevent unwanted hypnotise from hurting hypno-shroom.*/
        DamageSource setNotEating(DamageSource damageSource);
        void removeClientZombieEvent(ZombieEvent event);

    }

    public static class DummyAPI implements IPVZAPI {

        public static final PVZAPI.IPVZAPI INSTANCE = new DummyAPI();

        @Override
        public String getSunResourceName() {
            return "pvz.sun";
        }
        @Override
        public String getInvasionThreatResourceName() {
            return "pvz.invasion_threat";
        }
        @Override
        public boolean plantHaveCost(Player player) {
            return true;
        }
        @Override
        public boolean plantHaveCD(Player player) {
            return true;
        }
        @Override
        public boolean autoSetCostAndCD(Player player) {
            return true;
        }
        @Override
        public void setPlantHaveCost(Player player, boolean value) {
        }
        @Override
        public void setPlantHaveCD(Player player, boolean value) {
        }
        @Override
        public void setAutoSetCostAndCD(Player player, boolean value) {
        }

        @Override
        public int getSun(Player player) {
            return 0;
        }
        @Override
        public Attribute getMaxSunAttribute() {
            return null;
        }

        @Override
        public boolean isTeammate(Entity A, Entity B) {
            return false;
        }

        @Override
        public boolean canAttack(Entity attacker, Entity target) {
            return false;
        }

        @Override
        public float getPlantDamageResistance(Entity target) {
            return 0;
        }

        @Override
        public boolean isSculk(LivingEntity entity) {
            return false;
        }

        @Override
        public ResourceLocation getZombieEventType(ZombieEvent event) {
            return new ResourceLocation("pvz", "null");
        }

        @Override
        public DamageSource setNotEating(DamageSource damageSource) {
            return damageSource;
        }

        @Override
        public void removeClientZombieEvent(ZombieEvent event) {
        }
    }
}
