package com.hungteen.pvz.api;

import com.google.common.base.Suppliers;
import com.hungteen.pvz.PVZMod;
import net.minecraft.world.entity.Entity;

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
        String getSunString();
        boolean isTeammate(Entity A, Entity B);
    }

    public static class DummyAPI implements IPVZAPI {

        public static final PVZAPI.IPVZAPI INSTANCE = new DummyAPI();

        @Override
        public String getSunString() {
            return "pvz.sun";
        }

        @Override
        public boolean isTeammate(Entity A, Entity B) {
            return false;
        }
    }
}
