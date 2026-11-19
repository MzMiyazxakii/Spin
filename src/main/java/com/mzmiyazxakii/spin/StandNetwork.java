package com.mzmiyazxakii.spin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = SpinMod.MOD_ID)
public final class StandNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SpinMod.MOD_ID, "stand_channel"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    private StandNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, UnlockPacket.class,
                (packet, buffer) -> buffer.writeBoolean(packet.unlocked),
                buffer -> new UnlockPacket(buffer.readBoolean()),
                (packet, context) -> {
                    context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                            () -> () -> com.mzmiyazxakii.spin.client.VerityClientState.setUnlocked(packet.unlocked)));
                    context.get().setPacketHandled(true);
                });
    }

    public static void syncUnlocked(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UnlockPacket(true));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.getPersistentData().getBoolean("SpinHasVerity")) {
            syncUnlocked(player);
        }
    }

    public record UnlockPacket(boolean unlocked) {}
}
