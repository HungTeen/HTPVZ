package com.hungteen.pvz.api;

import com.google.common.base.Suppliers;
import com.hungteen.pvz.PVZMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
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

    public interface IPVZAPI {
        default boolean isDummy() {return true;}
        //player
        /**The key for sun in playerStats and EntityCap. */
        default String getSunResourceName() {
            return "pvz.sun";
        }
        /**The key for invasion threat in Invasion and EntityCap.*/
        default String getInvasionThreatResourceName() {
            return "pvz.invasion_threat";
        }
        /**The sun a player have. For {@link com.hungteen.pvz.api.interfaces.ISunContainer ISunAbsorber}, use {@link com.hungteen.pvz.api.interfaces.ISunContainer#getAmount() getContainingSun()}.*/
        default int getSun(Player player) {
            return 0;
        }
        /**Whether it costs player's sun when planting.*/
        default boolean plantHaveCost(Player player) {
            return true;
        }
        /**Whether player need cd after planting.*/
        default boolean plantHaveCD(Player player) {
            return true;
        }
        /**Whether {@link IPVZAPI#plantHaveCost(Player)} and {@link IPVZAPI#plantHaveCD(Player)} is automatically set depending on player's game mode.*/
        default boolean autoSetCostAndCD(Player player) {
            return true;
        }
        /**Set the above three values.*/
        default void setPlantHaveCost(Player player, boolean value) {}
        default void setPlantHaveCD(Player player, boolean value) {}
        default void setAutoSetCostAndCD(Player player, boolean value) {}
        /**the universal damage multiplier of plants according to {@link com.hungteen.pvz.PVZConfig.Common#plantDamageDatum pvz gamerule}.*/
        default float getPlantDamageDatum(Level level) {
            return 1;
        }
        /**the production speed multiplier of productive plants according to {@link com.hungteen.pvz.PVZConfig.Common#sunProductionDatum pvz gamerule}.*/
        default float getSunProductionDatum(Level level) {
            return 1;
        }
        //entity
        /**Return the attribute that PVZMod Register. Input the {@link PVZAttributes} as entry.*/
        default @Nullable Attribute getAttribute(PVZAttributes entry) {
            return null;
        }
        /**Check if entities are teammates. <b>CAN ONLY</b> call on server.
         * <br>I you want to check if an entity is attackable, use {@link IPVZAPI#canAttack(Entity, Entity)}.*/
        default boolean isTeammate(Entity A, Entity B) {
            return false;
        }
        /**
         * check can AttackGoal continue to attack target. <b>CAN ONLY</b> call on server.
         */
        default boolean canAttack(Entity attacker, Entity target) {
            return true;
        }
        /**Check if an entity is in sculk situation. The result can be modified with {@link com.hungteen.pvz.api.events.SculkJudgmentEvent SculkJudgementEvent}.*/
        default boolean isSculk(LivingEntity entity) {
            return false;
        }
        /**This returns the type of a {@link ZombieEvent} and is used by ZombieEvent is self.*/
        default ResourceLocation getZombieEventType(ZombieEvent event) {
            return new ResourceLocation("pvz", "null");
        }
        /**To prevent unwanted hypnotise from hurting hypno-shroom.*/
        default DamageSource setNotEating(DamageSource damageSource) {
            return damageSource;
        }
        /**Tell clients to remove a zombie event. Used only by PVZMod.*/
        @ApiStatus.Internal
        default void removeClientZombieEvent(ZombieEvent event) {
        }

    }
    public static class DummyAPI implements IPVZAPI {
        public static final PVZAPI.IPVZAPI INSTANCE = new DummyAPI();
    }
    public enum PVZAttributes {
        MAX_SUN, PLANT_HURT_RESISTANCE, LURE_ATTRACTING_ENEMIES, LEVEL_ATTRACTING_ENEMIES
    }
}
