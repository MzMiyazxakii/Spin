package com.mzmiyazxakii.spin.network;

import com.mzmiyazxakii.spin.SpinMod;
import com.mzmiyazxakii.spin.SteelBallProjectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.Comparator;
import java.util.function.Supplier;

public final class SpinNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(SpinMod.MOD_ID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);
    private static int packetId = 0;
    private SpinNetwork() {}
    public static void register() {
        CHANNEL.messageBuilder(ActionC2S.class, packetId++).encoder(ActionC2S::encode).decoder(ActionC2S::decode).consumerMainThread(ActionC2S::handle).add();
        CHANNEL.messageBuilder(ChargeS2C.class, packetId++).encoder(ChargeS2C::encode).decoder(ChargeS2C::decode).consumerMainThread(ChargeS2C::handle).add();
    }
    public static void sendAction(int action) { CHANNEL.sendToServer(new ActionC2S(action)); }
    public static void syncCharge(Player player) {
        if (player instanceof ServerPlayer serverPlayer) CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ChargeS2C(serverPlayer.getPersistentData().getFloat("spin_charge")));
    }
    private record ActionC2S(int action) {
        static void encode(ActionC2S msg, FriendlyByteBuf buf) { buf.writeVarInt(msg.action); }
        static ActionC2S decode(FriendlyByteBuf buf) { return new ActionC2S(buf.readVarInt()); }
        static void handle(ActionC2S msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender(); if (player == null) return;
                switch (msg.action) {
                    case 0 -> player.getPersistentData().putBoolean("spin_charging", true);
                    case 1 -> {
                        player.getPersistentData().putBoolean("spin_charging", false);
                        launch(player, SteelBallProjectile.Mode.NORMAL, 1.55F, 3.5F, SpinMod.THROW.get());
                    }
                    case 2 -> punch(player);
                    case 3 -> launch(player, SteelBallProjectile.Mode.FAST, 2.15F, 5.0F, SpinMod.FAST_BALL.get());
                    case 4 -> launch(player, SteelBallProjectile.Mode.CURVE, 1.65F, 4.0F, SpinMod.CURVEBALL.get());
                    case 5 -> launch(player, SteelBallProjectile.Mode.BOUNCY, 1.9F, 5.5F, SpinMod.BOUNCY_BALL.get());
                    case 6 -> launch(player, SteelBallProjectile.Mode.DRILL, 1.5F, 6.0F, SpinMod.DRILL.get());
                    default -> { }
                }
                syncCharge(player);
            });
            ctx.setPacketHandled(true);
        }
    }
    private record ChargeS2C(float charge) {
        static void encode(ChargeS2C msg, FriendlyByteBuf buf) { buf.writeFloat(msg.charge); }
        static ChargeS2C decode(FriendlyByteBuf buf) { return new ChargeS2C(buf.readFloat()); }
        static void handle(ChargeS2C msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ctx.enqueueWork(() -> com.mzmiyazxakii.spin.client.SpinClientEvents.setCharge(msg.charge));
            ctx.setPacketHandled(true);
        }
    }
    private static void punch(ServerPlayer player) {
        float charge = player.getPersistentData().getFloat("spin_charge"); if (charge < 15) return;
        player.getPersistentData().putFloat("spin_charge", Math.max(0, charge - 15));
        Vec3 look = player.getLookAngle(); AABB box = player.getBoundingBox().inflate(3.0, 1.5, 3.0);
        LivingEntity target = player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive() && e.position().subtract(player.position()).normalize().dot(look) > 0.35)
                .stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
        player.level().playSound(null, player.blockPosition(), SpinMod.PUNCH.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
        if (target != null) {
            target.hurt(player.damageSources().playerAttack(player), 2.0F + 3.0F * charge / 100.0F);
            target.knockback(0.65, -look.x, -look.z);
            player.level().playSound(null, target.blockPosition(), SpinMod.PUNCH_HIT.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
            for (int i = 0; i < 8; i++) player.level().addParticle(ParticleTypes.CRIT, target.getX(), target.getY() + 0.8, target.getZ(), (player.getRandom().nextDouble() - 0.5) * 0.25, 0.1, (player.getRandom().nextDouble() - 0.5) * 0.25);
        }
    }
    private static void launch(ServerPlayer player, SteelBallProjectile.Mode mode, float speed, float damage, net.minecraft.sounds.SoundEvent sound) {
        ItemStack held = player.getMainHandItem();
        if (!held.is(SpinMod.STEEL_BALL.get())) held = player.getOffhandItem();
        if (!held.is(SpinMod.STEEL_BALL.get())) return;
        if (player.isCrouching() && mode == SteelBallProjectile.Mode.NORMAL) mode = SteelBallProjectile.Mode.DRILL;
        if (player.isCrouching() && mode == SteelBallProjectile.Mode.FAST) { mode = SteelBallProjectile.Mode.DRILL_FAST; speed = 2.4F; }
        if (player.isCrouching() && mode == SteelBallProjectile.Mode.CURVE) { mode = SteelBallProjectile.Mode.DRILL_CURVE; speed = 1.8F; }
        if (!player.getAbilities().instabuild) held.shrink(1);
        SteelBallProjectile ball = new SteelBallProjectile(player.level(), player);
        ball.setItem(new ItemStack(SpinMod.STEEL_BALL.get())); ball.setMode(mode);
        float charge = player.getPersistentData().getFloat("spin_charge");
        ball.setDamage(damage + charge * 0.025F);
        ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, speed + charge * 0.004F, 0.65F);
        player.level().addFreshEntity(ball); player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 0.9F, 1.0F);
        player.getPersistentData().putFloat("spin_charge", Math.max(0, charge - 12));
    }
}
