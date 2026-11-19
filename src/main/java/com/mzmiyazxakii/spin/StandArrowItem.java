package com.mzmiyazxakii.spin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SpinMod.MOD_ID)
public final class StandArrowItem extends Item {
    private static final int AWAKENING_TICKS = 60;
    private static final Map<UUID, Integer> AWAKENING = new HashMap<>();

    public StandArrowItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.getPersistentData().getBoolean("SpinHasVerity")
                    && !AWAKENING.containsKey(player.getUUID())) {
                AWAKENING.put(player.getUUID(), AWAKENING_TICKS);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL,
                        SoundSource.PLAYERS, 0.8F, 0.8F);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || AWAKENING.isEmpty()) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                UUID id = player.getUUID();
                Integer remaining = AWAKENING.get(id);
                if (remaining == null) continue;

                spawnAwakeningParticles(level, player, AWAKENING_TICKS - remaining);
                if (remaining <= 1) {
                    AWAKENING.remove(id);
                    player.getPersistentData().putBoolean("SpinHasVerity", true);
                    StandNetwork.syncUnlocked(player);
                    level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                            SoundSource.PLAYERS, 0.9F, 1.25F);
                } else {
                    AWAKENING.put(id, remaining - 1);
                }
            }
        }
    }

    private static void spawnAwakeningParticles(ServerLevel level, ServerPlayer player, int age) {
        double radius = 1.15D + Math.sin(age * 0.12D) * 0.18D;
        double rotation = age * 0.22D;
        for (int i = 0; i < 10; i++) {
            double angle = rotation + Math.PI * 2.0D * i / 10.0D;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY() + 0.15D + (i % 5) * 0.38D;
            level.sendParticles(ParticleTypes.DRAGON_BREATH, x, y, z, 2, 0.08D, 0.12D, 0.08D, 0.01D);
            level.sendParticles(ParticleTypes.PORTAL, x, y, z, 2, 0.12D, 0.18D, 0.12D, 0.12D);
        }
        if (age % 4 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D,
                    player.getZ(), 6, 0.45D, 0.65D, 0.45D, 0.025D);
        }
    }
}
