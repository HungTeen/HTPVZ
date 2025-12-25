package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.tags.PVZBannerPatternTags;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class PVZBannerPatterns {
    public static Map<RegistryObject<BannerPattern>, TagKey<BannerPattern>> bannerMap = new HashMap<>();

    public static final DeferredRegister<BannerPattern> BANNERS = DeferredRegister.create(Registry.BANNER_PATTERN_REGISTRY, PVZMod.MODID);
    public static final RegistryObject<BannerPattern> BRAIN = banner("brain", PVZBannerPatternTags.PATTERN_ITEM_BRAIN);
    public static final RegistryObject<BannerPattern> LEAF = banner("leaf", PVZBannerPatternTags.PATTERN_ITEM_LEAF);

    private static RegistryObject<BannerPattern> banner(String name, TagKey<BannerPattern> tag) {
        RegistryObject<BannerPattern> obj = BANNERS.register(name, () -> new BannerPattern(PVZMod.MODID + ":" + name));
        bannerMap.put(obj, tag);
        return obj;
    }
}
