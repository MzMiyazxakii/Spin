package com.mzmiyazxakii.spin.item;

import com.mzmiyazxakii.spin.entity.SteelBallProjectile;
import com.mzmiyazxakii.spin.registry.SpinSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class SteelBallItem extends Item {
    public SteelBallItem(Properties properties) { super(properties); }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public int getUseDuration(ItemStack stack) { return 72000; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (!level.isClientSide) level.playSound(null, player.blockPosition(), SpinSounds.CHARGE.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
        return InteractionResultHolder.consume(stack);
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (!(user instanceof Player player) || !(level instanceof ServerLevel server)) return;
        int ticks = getUseDuration(stack) - timeLeft;
        float power = Math.min(3.0F, 1.0F + ticks / 30.0F);
        SteelBallProjectile ball = new SteelBallProjectile(level, player);
        ball.setItem(new ItemStack(this));
        ball.setSpinPower(power);
        ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F + power * 0.65F, 0.4F);
        server.addFreshEntity(ball);
        level.playSound(null, player.blockPosition(), SpinSounds.THROW.get(), SoundSource.PLAYERS, 1.0F, 0.85F + power * 0.1F);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.getCooldowns().addCooldown(this, 12);
    }
    @Override public boolean onLeftClickEntity(ItemStack stack, Player player, net.minecraft.world.entity.Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {
            target.hurt(player.damageSources().playerAttack(player), 5.0F);
            player.level().playSound(null, player.blockPosition(), SpinSounds.PUNCH.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
            player.getCooldowns().addCooldown(this, 8);
        }
        return true;
    }
}
