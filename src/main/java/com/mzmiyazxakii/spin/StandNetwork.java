package com.mzmiyazxakii.spin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SpinMod.MOD_ID)
public final class StandNetwork {
    private static final String VERSION = "1";
    private static final int STRIKE_COOLDOWN_TICKS = 200;
    private static final int STRIKE_SHOTS = 4;
    private static final List<TearShot> TEAR_SHOTS = new ArrayList<>();
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
        CHANNEL.registerMessage(1, StrikeRequest.class,
                (packet, buffer) -> {},
                buffer -> new StrikeRequest(),
                (packet, context) -> {
                    context.get().enqueueWork(() -> {
                        ServerPlayer player = context.get().getSender();
                        if (player != null) performStrike(player);
                    });
                    context.get().setPacketHandled(true);
                });
    }

    public static void requestStrike() {
        CHANNEL.sendToServer(new StrikeRequest());
    }

    private static void performStrike(ServerPlayer player) {
        if (!player.getPersistentData().getBoolean("SpinHasVerity")) return;
        long now = player.level().getGameTime();
        if (now < player.getPersistentData().getLong("SpinVerityStrikeReadyAt")) return;

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(32.0D));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(32.0D)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, searchBox,
                entity -> entity instanceof LivingEntity && entity != player && entity.isPickable(),
                32.0D * 32.0D);
        if (hit == null || !(hit.getEntity() instanceof LivingEntity target)) return;

        player.getPersistentData().putLong("SpinVerityStrikeReadyAt", now + STRIKE_COOLDOWN_TICKS);
        for (int i = 0; i < STRIKE_SHOTS; i++) {
            TEAR_SHOTS.add(new TearShot(player.getUUID(), target.getId(), player.serverLevel(), i * 3));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TEAR_SHOTS.isEmpty()) return;
        MinecraftServer server = event.getServer();
        Iterator<TearShot> iterator = TEAR_SHOTS.iterator();
        while (iterator.hasNext()) {
            TearShot shot = iterator.next();
            if (shot.delay > 0) {
                shot.delay--;
                continue;
            }
            ServerPlayer owner = server.getPlayerList().getPlayer(shot.ownerId);
            Entity found = shot.level.getEntity(shot.targetId);
            if (owner != null && owner.isAlive() && found instanceof LivingEntity target && target.isAlive()) {
                Vec3 from = owner.getEyePosition().add(owner.getLookAngle().scale(0.65D));
                Vec3 to = target.getBoundingBox().getCenter();
                Vec3 direction = to.subtract(from).normalize();
                VerityTearProjectile tear = new VerityTearProjectile(shot.level, owner);
                tear.setPos(from);
                tear.shoot(direction.x, direction.y, direction.z, 1.8F, 0.0F);
                shot.level.addFreshEntity(tear);
            }
            iterator.remove();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncUnlocked(player, player.getPersistentData().getBoolean("SpinHasVerity"));
        }
    }

    public static void syncUnlocked(ServerPlayer player) {
        syncUnlocked(player, true);
    }

    public static void syncUnlocked(ServerPlayer player, boolean unlocked) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UnlockPacket(unlocked));
    }

    private static final class TearShot {
        private final UUID ownerId;
        private final int targetId;
        private final ServerLevel level;
        private int delay;

        private TearShot(UUID ownerId, int targetId, ServerLevel level, int delay) {
            this.ownerId = ownerId;
            this.targetId = targetId;
            this.level = level;
            this.delay = delay;
        }
    }

    public record UnlockPacket(boolean unlocked) {}
    public record StrikeRequest() {}
}
