package tech.mania.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import tech.mania.Mania;
import tech.mania.core.features.event.RotationEvent;
import tech.mania.core.util.RotationUtil;

@Mixin(Mouse.class)
public class MouseMixin {

    @ModifyArgs(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            )
    )
    public void injectUpdateMouse(Args args) {
        float f = RotationUtil.virtualPitch;
        float f1 = RotationUtil.virtualYaw;
        RotationUtil.virtualYaw = (float)((double)RotationUtil.virtualYaw + (double)args.get(0) * 0.15D);
        RotationUtil.virtualPitch = (float)((double)RotationUtil.virtualPitch + (double)args.get(1) * 0.15D);
        RotationUtil.virtualPitch = MathHelper.clamp(RotationUtil.virtualPitch, -90.0F, 90.0F);
        RotationUtil.virtualPrevPitch += RotationUtil.virtualPitch - f;
        RotationUtil.virtualPrevYaw += RotationUtil.virtualYaw - f1;
        args.set(0, 0d);
        args.set(1, 0d);

        final RotationEvent rotationEvent = new RotationEvent(
                RotationUtil.virtualYaw,
                RotationUtil.virtualPitch
        );
        Mania.getEventManager().call(rotationEvent);

        final ClientPlayerEntity thePlayer = MinecraftClient.getInstance().player;

        thePlayer.setYaw(RotationUtil.getFixedSensitivityAngle(rotationEvent.yaw, thePlayer.getYaw()));
        thePlayer.setPitch(RotationUtil.getFixedSensitivityAngle(rotationEvent.pitch, thePlayer.getPitch()));
        thePlayer.setPitch(MathHelper.clamp(thePlayer.getPitch(), -90f, 90f));
    }
}
