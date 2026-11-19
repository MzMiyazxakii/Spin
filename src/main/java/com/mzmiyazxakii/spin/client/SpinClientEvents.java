package com.mzmiyazxakii.spin.client;

import com.mzmiyazxakii.spin.SpinMod;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = SpinMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class SpinClientEvents {
    private static final int CHARGE_SPIN_STEPS = 12;

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                SpinMod.STEEL_BALL.get(),
                new ResourceLocation(SpinMod.MOD_ID, "charge_spin"),
                (stack, level, entity, seed) -> {
                    if (entity == null || !entity.isUsingItem()
                            || !entity.getUseItem().is(SpinMod.STEEL_BALL.get())) {
                        return 0.0F;
                    }
                    int step = (entity.getTicksUsingItem() / 2) % CHARGE_SPIN_STEPS;
                    return step / (float) CHARGE_SPIN_STEPS;
                }
        ));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SpinMod.STEEL_BALL_PROJECTILE.get(), SteelBallRenderer::new);
    }
}
