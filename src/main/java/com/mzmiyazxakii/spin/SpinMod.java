package com.mzmiyazxakii.spin;

import com.mzmiyazxakii.spin.network.SpinNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(SpinMod.MOD_ID)
public class SpinMod {
    public static final String MOD_ID = "spin";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    public static final RegistryObject<Item> STEEL_BALL = ITEMS.register("steel_ball",
            () -> new SteelBallItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<EntityType<SteelBallProjectile>> STEEL_BALL_PROJECTILE = ENTITIES.register("steel_ball",
            () -> EntityType.Builder.<SteelBallProjectile>of(SteelBallProjectile::new, MobCategory.MISC)
                    .sized(0.28F, 0.28F).clientTrackingRange(8).updateInterval(1)
                    .build(new ResourceLocation(MOD_ID, "steel_ball").toString()));

    public static final RegistryObject<SoundEvent> CHARGE = sound("charge");
    public static final RegistryObject<SoundEvent> CHARGE_LOOP = sound("charge_loop");
    public static final RegistryObject<SoundEvent> PUNCH = sound("spin_imbued_punch");
    public static final RegistryObject<SoundEvent> PUNCH_HIT = sound("spin_imbued_punch_hit");
    public static final RegistryObject<SoundEvent> THROW = sound("spin_throw");
    public static final RegistryObject<SoundEvent> FAST_BALL = sound("spin_fast_ball");
    public static final RegistryObject<SoundEvent> CURVEBALL = sound("spin_curveball");
    public static final RegistryObject<SoundEvent> BOUNCY_BALL = sound("spin_bouncy_ball");
    public static final RegistryObject<SoundEvent> DRILL = sound("spin_drill");
    public static final RegistryObject<SoundEvent> CATCH = sound("catch");
    public static final RegistryObject<SoundEvent> SPEC_CHANGE = sound("spec_change");
    public static final RegistryObject<SoundEvent> STEEL_BALL_HIT = sound("steel_ball_hit");

    private static RegistryObject<SoundEvent> sound(String name) {
        ResourceLocation id = new ResourceLocation(MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public SpinMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(this::addCreative);
        SpinNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(this::playerTick);
        MinecraftForge.EVENT_BUS.addListener(this::livingHurt);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT || event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) event.accept(STEEL_BALL);
    }

    private void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        var player = event.player;
        var data = player.getPersistentData();
        if (data.getBoolean("spin_charging")) data.putFloat("spin_charge", Math.min(100.0F, data.getFloat("spin_charge") + 0.4F));
        if (player.tickCount % 5 == 0) SpinNetwork.syncCharge(player);
    }

    private void livingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player) || player.level().isClientSide) return;
        float charge = player.getPersistentData().getFloat("spin_charge");
        if (charge >= 10.0F) {
            event.setAmount(event.getAmount() + Math.min(3.0F, charge / 35.0F));
            player.getPersistentData().putFloat("spin_charge", Math.max(0.0F, charge - 2.0F));
            SpinNetwork.syncCharge(player);
        }
    }
}
