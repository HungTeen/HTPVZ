package com.hungteen.pvz.compat;

import com.hungteen.pvz.PVZMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = PVZMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ShaderCompatHandler {

    private static final boolean IRIS_LIKE = isModLoaded("oculus") || isModLoaded("iris");
    private static final boolean OPTIFINE = isModLoaded("optifine");

    private static Method irisIsShaderPackInUse;
    private static Object irisApiInstance;
    private static Method irisGetCurrentPackName;
    private static Method irisGetCurrentPack;

    private static Method optifineIsShaders;
    private static Field optifineShaderPackLoaded;

    private static volatile boolean shaderPackActive;
    private static boolean initialized;

    static {
        init();
    }

    private ShaderCompatHandler() {
    }

    private static boolean isModLoaded(String modId) {
        try {
            return ModList.get() != null && ModList.get().isLoaded(modId);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isShaderPackActive() {
        return shaderPackActive;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) refresh();
    }

    private static void init() {
        if (initialized) return;
        initialized = true;
        if (IRIS_LIKE) initIris();
        if (OPTIFINE) initOptiFine();
        refresh();
    }

    private static void initIris() {
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Method getInstanceMethod = apiClass.getMethod("getInstance");
            irisIsShaderPackInUse = apiClass.getMethod("isShaderPackInUse");
            irisApiInstance = getInstanceMethod.invoke(null);
        } catch (Throwable ignored) {}
        try {
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            try {
                irisGetCurrentPackName = irisClass.getMethod("getCurrentPackName");
            } catch (NoSuchMethodException ignored) {
                irisGetCurrentPackName = null;
            }
            if (irisGetCurrentPackName == null) {
                try {
                    irisGetCurrentPack = irisClass.getMethod("getCurrentPack");
                } catch (NoSuchMethodException ignored) {
                    irisGetCurrentPack = null;
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void initOptiFine() {
        try {
            optifineIsShaders = Class.forName("net.optifine.Config").getMethod("isShaders");
        } catch (Throwable ignored) {
            try {
                optifineShaderPackLoaded = Class.forName("net.optifine.shaders.Shaders")
                        .getField("shaderPackLoaded");
            } catch (Throwable ignoredToo) {}
        }
    }

    private static void refresh() {
        if (!initialized) {
            init();
            return;
        }
        boolean active = queryIris() || queryOptiFine();
        if (active != shaderPackActive) {
            shaderPackActive = active;
            PVZMod.LOGGER.debug("Shader pack active: {}", active);
        }
    }

    private static boolean queryIris() {
        if (irisApiInstance != null && irisIsShaderPackInUse != null) {
            try {
                return Boolean.TRUE.equals(irisIsShaderPackInUse.invoke(irisApiInstance));
            } catch (Throwable ignored) {}
        }
        if (irisGetCurrentPackName != null) {
            try {
                Object name = irisGetCurrentPackName.invoke(null);
                if (name instanceof Optional<?> optional) {
                    return optional.isPresent();
                }
                if (name instanceof String string) {
                    return !string.isBlank();
                }
                return name != null;
            } catch (Throwable ignored) {}
        }
        if (irisGetCurrentPack != null) {
            try {
                Object pack = irisGetCurrentPack.invoke(null);
                if (pack instanceof Optional<?> optional) {
                    return optional.isPresent();
                }
                return pack != null;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    private static boolean queryOptiFine() {
        if (optifineIsShaders != null) {
            try {
                return Boolean.TRUE.equals(optifineIsShaders.invoke(null));
            } catch (Throwable ignored) {}
        }
        if (optifineShaderPackLoaded != null) {
            try {
                return optifineShaderPackLoaded.getBoolean(null);
            } catch (Throwable ignored) {}
        }
        return false;
    }
}
