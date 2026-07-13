package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.util.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import net.minecraftforge.registries.RegistryObject;

public class SoundGen extends SoundDefinitionsProvider {

    protected SoundGen(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, PVZMod.MODID, helper);
    }

    @Override
    public void registerSounds() {
        for (RegistryObject<SoundEvent> obj: PVZSoundEvents.generationSet) {
            float volume = PVZSoundEvents.volumeMap.containsKey(obj) ? PVZSoundEvents.volumeMap.get(obj) : 1;
            SoundDefinition definition = SoundDefinition.definition().subtitle("subtitles.pvz." + obj.getId().getPath());
            if (PVZSoundEvents.locationMap.containsKey(obj)) {
                var map = PVZSoundEvents.locationMap.get(obj);
                map.forEach((loc, weight) -> definition.with(SoundDefinition.Sound
                        .sound(Util.prefix(loc), SoundDefinition.SoundType.SOUND).volume(volume).weight(weight)));
            } else {
                definition.with(SoundDefinition.Sound
                        .sound(Util.prefix(obj.get().getLocation().getPath().replaceAll("\\.", "/")), SoundDefinition.SoundType.SOUND).volume(volume).weight(5));
            }
            add(obj, definition);
        }
    }
}
