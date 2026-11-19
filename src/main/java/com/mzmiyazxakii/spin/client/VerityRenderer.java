package com.mzmiyazxakii.spin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mzmiyazxakii.spin.SpinMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = SpinMod.MOD_ID, value = Dist.CLIENT)
public final class VerityRenderer {
    private VerityRenderer() {}

    @SubscribeEvent
    public static void renderVerity(RenderPlayerEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getEntity() != minecraft.player
                || !VerityClientState.isVisible()) return;

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        float time = (event.getEntity().tickCount + event.getPartialTick()) * 0.075F;
        // A small golden orb hovering behind the player's right shoulder.
        pose.translate(0.43D + Math.sin(time) * 0.035D, 1.62D + Math.sin(time * 1.7F) * 0.055D,
                -0.34D + Math.cos(time) * 0.035D);
        pose.scale(0.19F, 0.19F, 0.19F);
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
