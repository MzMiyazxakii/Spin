package com.mzmiyazxakii.spin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mzmiyazxakii.spin.SpinMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = SpinMod.MOD_ID, value = Dist.CLIENT)
public final class VerityRenderer {
    private VerityRenderer() {}

    @SubscribeEvent
    public static void renderVerity(RenderPlayerEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getEntity() != minecraft.player
                || minecraft.level == null || !VerityClientState.isVisible()) return;

        float partialTick = event.getPartialTick();
        float progress = VerityClientState.getSummonProgress(partialTick);
        float time = (event.getEntity().tickCount + partialTick) * 0.075F;

        // A soft golden aura: a few tiny, low-opacity-looking dust motes around Verity.
        if (event.getEntity().tickCount % 3 == 0) {
            double yaw = Math.toRadians(event.getEntity().getYRot());
            double rightX = Math.cos(yaw);
            double rightZ = -Math.sin(yaw);
            double backX = Math.sin(yaw);
            double backZ = -Math.cos(yaw);
            double centerX = event.getEntity().getX() + rightX * 0.43D + backX * -0.34D;
            double centerY = event.getEntity().getY() + 1.62D;
            double centerZ = event.getEntity().getZ() + rightZ * 0.43D + backZ * -0.34D;
            DustParticleOptions yellowDust = new DustParticleOptions(new Vector3f(1.0F, 0.82F, 0.22F), 0.48F);
            for (int i = 0; i < 2; i++) {
                double ox = (event.getEntity().getRandom().nextDouble() - 0.5D) * 0.48D;
                double oy = (event.getEntity().getRandom().nextDouble() - 0.5D) * 0.62D;
                double oz = (event.getEntity().getRandom().nextDouble() - 0.5D) * 0.42D;
                minecraft.level.addParticle(yellowDust, centerX + ox, centerY + oy, centerZ + oz,
                        0.0D, 0.008D, 0.0D);
            }
        }

        // Verity emerges from the player's torso, then settles behind the right shoulder.
        double startX = 0.0D;
        double startY = 1.05D;
        double startZ = -0.02D;
        double targetX = 0.43D + Math.sin(time) * 0.035D;
        double targetY = 1.62D + Math.sin(time * 1.7F) * 0.055D;
        double targetZ = -0.34D + Math.cos(time) * 0.035D;

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(
                startX + (targetX - startX) * progress,
                startY + (targetY - startY) * progress,
                startZ + (targetZ - startZ) * progress
        );
        float scale = 0.035F + 0.155F * progress;
        pose.scale(scale, scale, scale);
        drawOrb(pose, event.getMultiBufferSource().getBuffer(RenderType.lightning()));
        pose.popPose();
    }

    private static void drawOrb(PoseStack poseStack, VertexConsumer consumer) {
        Matrix4f matrix = poseStack.last().pose();
        final int rings = 10;
        final int segments = 16;
        for (int ring = 0; ring < rings; ring++) {
            double phi0 = Math.PI * ring / rings;
            double phi1 = Math.PI * (ring + 1) / rings;
            for (int segment = 0; segment < segments; segment++) {
                double theta0 = Math.PI * 2.0D * segment / segments;
                double theta1 = Math.PI * 2.0D * (segment + 1) / segments;
                vertex(consumer, matrix, point(phi0, theta0), 255, 205, 35, 245);
                vertex(consumer, matrix, point(phi1, theta0), 255, 226, 80, 245);
                vertex(consumer, matrix, point(phi1, theta1), 255, 226, 80, 245);
                vertex(consumer, matrix, point(phi0, theta1), 255, 205, 35, 245);
            }
        }
    }

    private static Vec3 point(double phi, double theta) {
        double sin = Math.sin(phi);
        return new Vec3(sin * Math.cos(theta), Math.cos(phi), sin * Math.sin(theta));
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 point,
                               int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha).endVertex();
    }
}
