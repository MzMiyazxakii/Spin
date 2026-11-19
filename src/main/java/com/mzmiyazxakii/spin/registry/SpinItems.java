package com.mzmiyazxakii.spin.registry;

import com.mzmiyazxakii.spin.SpinMod;
import com.mzmiyazxakii.spin.item.SteelBallItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SpinItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SpinMod.MOD_ID);
    public static final RegistryObject<Item> STEEL_BALL = ITEMS.register("steel_ball", () -> new SteelBallItem(new Item.Properties().stacksTo(16)));
    private SpinItems() {}
}
