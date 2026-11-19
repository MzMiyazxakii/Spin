package com.mzmiyazxakii.spin.client;

import com.mzmiyazxakii.spin.SpinMod;
import com.mzmiyazxakii.spin.registry.SpinEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SpinMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SpinClient {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SpinEntities.STEEL_BALL.get(), ThrownItemRenderer::new);
    }
}
