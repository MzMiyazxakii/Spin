package com.mzmiyazxakii.spin.registry;

import com.mzmiyazxakii.spin.SpinMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SpinSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SpinMod.MOD_ID);
    public static final RegistryObject<SoundEvent> THROW = register("specs.spin.throw");
    public static final RegistryObject<SoundEvent> CHARGE = register("specs.spin.charge");
    public static final RegistryObject<SoundEvent> PUNCH = register("specs.spin.spin_imbued_punch");
    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SpinMod.MOD_ID, name)));
    }
    private SpinSounds() {}
}
