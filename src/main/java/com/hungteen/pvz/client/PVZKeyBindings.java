package com.hungteen.pvz.client;

import com.hungteen.pvz.common.network.ClientProxy;
import net.minecraft.client.KeyMapping;
import org.apache.commons.lang3.ArrayUtils;

public class PVZKeyBindings {

    //key bindings
    public static final KeyMapping keyEnderSeedBundle = keyMapping("key.pvz.ender_seed_bundle", 90, "key.categories.inventory");

    private static KeyMapping keyMapping(String name, int value, String category) {
        KeyMapping result = new KeyMapping(name, value, category);
        ClientProxy.MC.options.keyMappings = ArrayUtils.addAll(ClientProxy.MC.options.keyMappings, result);
        return result;
    }
    public static void init() {
    }
}
