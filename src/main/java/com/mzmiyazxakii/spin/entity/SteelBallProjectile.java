package com.mzmiyazxakii.spin.entity;

import com.mzmiyazxakii.spin.registry.SpinEntities;
import com.mzmiyazxakii.spin.registry.SpinItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class SteelBallProjectile extends ThrowableItemProjectile {
    private float spinPower = 1.0F;
    public SteelBallProjectile(EntityType<? extends SteelBallProjectile> type, Level level) { super(type, level); }
    public SteelBallProjectile(Level level, LivingEntity owner) { super(SpinEntities.STEEL_BALL.get(), owner, level); }
    public void setSpinPower(float power) { this.spinPower = Math.max(1.0F, Math.min(3.0F, power)); }
    @Override protected Item getDefaultItem() { return SpinItems.STEEL_BALL.get(); }
    @Override protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            target.hurt(damageSources().thrown(this, getOwner()), 4.0F * spinPower);
            if (spinPower >= 2.5F) target.knockback(0.7D, getX() - target.getX(), getZ() - target.getZ());
        }
    }
}
