package com.mzmiyazxakii.spin;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import com.mzmiyazxakii.spin.network.SpinNetwork;
import org.joml.Vector3f;

public class SteelBallProjectile extends ThrowableItemProjectile {
    private static final DustParticleOptions SPIN_DUST =
            new DustParticleOptions(new Vector3f(0.08F, 0.95F, 0.16F), 0.78F);

    public enum Mode { NORMAL, FAST, CURVE, BOUNCY, DRILL, DRILL_FAST, DRILL_CURVE }

    private Mode mode = Mode.NORMAL;
    private float damage = 3.0F;
    private boolean returning = false;
    private int returnTicks = 0;
    private int bounceCount = 0;

    public SteelBallProjectile(EntityType<? extends SteelBallProjectile> type, Level level) {
        super(type, level);
    }

    public SteelBallProjectile(Level level, LivingEntity owner) {
        super(SpinMod.STEEL_BALL_PROJECTILE.get(), owner, level);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return SpinMod.STEEL_BALL.get();
    }

    /**
     * Give the Steel Ball a flatter, longer-lasting arc than a vanilla snowball.
     */
    @Override
    protected float getGravity() {
        return 0.018F;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnSpinParticles();
            return;
        }

        if ((mode == Mode.CURVE || mode == Mode.DRILL_CURVE || mode == Mode.BOUNCY) && !returning && tickCount < 28) {
            Vec3 side = getDeltaMovement().cross(new Vec3(0, 1, 0)).normalize().scale(0.022);
            setDeltaMovement(getDeltaMovement().add(side));
        }

        if (mode == Mode.BOUNCY && !returning && tickCount > 65) returning = true;
        if (mode != Mode.BOUNCY && !returning && tickCount > (isDrillMode() ? 24 : 38)) returning = true;

        if (returning) {
            Entity owner = getOwner();
            if (owner == null || !owner.isAlive()) {
                discard();
                return;
            }

            returnTicks++;
            Vec3 delta = owner.position().add(0, owner.getBbHeight() * 0.65, 0).subtract(position());
            if (delta.lengthSqr() < 1.25) {
                if (owner instanceof Player player) {
                    ItemStack returned = new ItemStack(SpinMod.STEEL_BALL.get());
                    if (!player.getInventory().add(returned)) player.drop(returned, false);
                    level().playSound(null, player.blockPosition(), SpinMod.CATCH.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
                }
                discard();
                return;
            }

            noPhysics = true;
            setDeltaMovement(delta.normalize().scale(Math.min(1.45, 0.45 + returnTicks * 0.035)));
            hasImpulse = true;
        }

        if (tickCount > 180) discard();
    }

    /**
     * Emit a rotating ring of green energy particles around the ball.
     * The ring is built from the projectile's current flight direction, so it
     * stays around the ball even when the player throws it upward or downward.
     */
    private void spawnSpinParticles() {
        Vec3 movement = getDeltaMovement();
        Vec3 forward = movement.lengthSqr() > 1.0E-4 ? movement.normalize() : new Vec3(0, 0, 1);
        Vec3 reference = Math.abs(forward.y) > 0.92 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);

        Vec3 right = forward.cross(reference).normalize();
        Vec3 up = right.cross(forward).normalize();
        double phase = tickCount * 0.42D;

        for (int i = 0; i < 3; i++) {
            double angle = phase + i * (Math.PI * 2.0D / 3.0D);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            Vec3 radial = right.scale(cos).add(up.scale(sin));
            Vec3 offset = radial.scale(0.25D);
            Vec3 tangent = right.scale(-sin).add(up.scale(cos)).scale(0.025D);
            Vec3 particleVelocity = forward.scale(0.035D).add(tangent);

            level().addParticle(
                    SPIN_DUST,
                    getX() + offset.x,
                    getY() + offset.y,
                    getZ() + offset.z,
                    particleVelocity.x,
                    particleVelocity.y,
                    particleVelocity.z
            );
        }
    }

    private boolean isDrillMode() {
        return mode == Mode.DRILL || mode == Mode.DRILL_FAST || mode == Mode.DRILL_CURVE;
    }

    @Override
    protected void onHit(HitResult result) {
        if (level().isClientSide || returning) return;

        if (result.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) result).getEntity();
            Entity owner = getOwner();
            if (target != owner && target instanceof LivingEntity living) {
                living.hurt(damageSources().thrown(this, owner), damage);
                if (isDrillMode()) {
                    living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 50, 1));
                }
                if (owner instanceof Player player) {
                    player.getPersistentData().putFloat(
                            "spin_charge",
                            Math.min(100, player.getPersistentData().getFloat("spin_charge") + 8)
                    );
                    SpinNetwork.syncCharge(player);
                }
            }

            if (mode == Mode.BOUNCY && bounceCount++ < 3) {
                setDeltaMovement(getDeltaMovement().scale(-0.82).add(0, 0.08, 0));
                hasImpulse = true;
            } else {
                returning = true;
            }
        } else if (result.getType() == HitResult.Type.BLOCK) {
            if (mode == Mode.BOUNCY && bounceCount++ < 5) {
                BlockHitResult hit = (BlockHitResult) result;
                Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
                Vec3 reflected = getDeltaMovement()
                        .subtract(normal.scale(2 * getDeltaMovement().dot(normal)))
                        .scale(0.82);
                setDeltaMovement(reflected);
                setPos(
                        getX() + normal.x * 0.08,
                        getY() + normal.y * 0.08,
                        getZ() + normal.z * 0.08
                );
                hasImpulse = true;
            } else {
                returning = true;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SpinMode", mode.name());
        tag.putFloat("SpinDamage", damage);
        tag.putBoolean("Returning", returning);
        tag.putInt("BounceCount", bounceCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        try {
            mode = Mode.valueOf(tag.getString("SpinMode"));
        } catch (Exception ignored) {
            mode = Mode.NORMAL;
        }
        damage = tag.getFloat("SpinDamage");
        returning = tag.getBoolean("Returning");
        bounceCount = tag.getInt("BounceCount");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
