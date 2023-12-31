package tech.mania.core.util.legit;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.mania.MCHook;
import tech.mania.core.util.RandomUtil;
import tech.mania.core.util.RotationUtil;

public class LegitEntityRotation implements MCHook {

    private Entity entity;

    private float aYaw, aPitch;
    private long next;

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public float[] calcRotation() {
        Vec3d eye = mc.player.getEyePos();
        Box bb = entity.getBoundingBox();
        final Vec3d eyeA = mc.player.getRotationVec(1f).multiply(6);
        if (bb.intersects(eye, eye.add(eyeA))) {
            if (System.currentTimeMillis() > next) {
                final float[] center = RotationUtil.rotation(entity.getEyePos(), eye);
                next = System.currentTimeMillis() + RandomUtil.nextInt(100);
                aYaw = RandomUtil.nextFloat(0.1f) * MathHelper.wrapDegrees(
                        center[0] - mc.player.getYaw()
                );
                aPitch = RandomUtil.nextFloat(0.1f) * MathHelper.wrapDegrees(
                        center[1] - mc.player.getPitch()
                );
            }
            return new float[] {
                    mc.player.getYaw() + aYaw * RandomUtil.nextFloat(3),
                    mc.player.getPitch() + aPitch * RandomUtil.nextFloat(3)
            };
        }
        final float[] z = RotationUtil.rotation(entity.getEyePos().add(
                RandomUtil.nextDouble(-0.5, 0.5),
                RandomUtil.nextDouble(-0.5, 0.1),
                RandomUtil.nextDouble(-0.5, 0.5)
        ), eye);
        z[0] = RotationUtil.smoothRot(mc.player.getYaw(), z[0], RandomUtil.nextFloat(25, 50));
        z[1] = RotationUtil.smoothRot(mc.player.getPitch(), z[1], RandomUtil.nextFloat(25, 50));
        z[1] += (float) (Math.sin(MathHelper.wrapDegrees(mc.player.getYaw() - z[0]) / 5) * 5);
        return z;
    }
}
