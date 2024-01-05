package tech.mania.core.util;

import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec3d;
import tech.mania.MCHook;

public class MoveUtil implements MCHook {

    public static Vec3d getDir(double dist) {
        double rad = Math.toRadians(mc.player.getYaw() + 90.0f);
        Input input = mc.player.input;
        return new Vec3d(
                (input.movementForward * 0.45 * Math.cos(rad)
                        + input.movementSideways * 0.45 * Math.sin(rad)) * dist,
                0,
                (input.movementForward * 0.45 * Math.sin(rad)
                        - input.movementSideways * 0.45 * Math.cos(rad)) * dist
        );
    }
}
