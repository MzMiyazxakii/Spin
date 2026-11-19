package com.mzmiyazxakii.spin.registry;

import com.mzmiyazxakii.spin.SpinMod;
import com.mzmiyazxakii.spin.entity.SteelBallProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SpinEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SpinMod.MOD_ID);
    public static final RegistryObject<EntityType<SteelBallProjectile>> STEEL_BALL = ENTITIES.register("steel_ball",
        () -> EntityType.Builder.<SteelBallProjectile>of(SteelBallProjectile::new, MobCategory.MISC)
            .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("steel_ball"));
    private SpinEntities() {}
}
