package com.mzmiyazxakii.spin;

import com.mzmiyazxakii.spin.client.VerityClientState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

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
                    context.get().enqueueWork(() -> VerityClientState.setUnlocked(packet.unlocked));
                    context.get().setPacketHandled(true);
                });
    }

    public static void syncUnlocked(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UnlockPacket(true));
    }

    public record UnlockPacket(boolean unlocked) {}
}
