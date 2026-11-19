package com.mzmiyazxakii.spin;

import com.mzmiyazxakii.spin.registry.SpinEntities;
import com.mzmiyazxakii.spin.registry.SpinItems;
import com.mzmiyazxakii.spin.registry.SpinSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SpinMod.MOD_ID)
public class SpinMod {
    public static final String MOD_ID = "spin";
    public SpinMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        SpinItems.ITEMS.register(bus);
        SpinEntities.ENTITIES.register(bus);
        SpinSounds.SOUNDS.register(bus);
    }
}
