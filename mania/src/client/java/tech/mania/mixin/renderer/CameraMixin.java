package tech.mania.mixin.renderer;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import tech.mania.core.util.RotationUtil;

@Mixin(Camera.class)
public class CameraMixin {

    @Unique
    float tickDelta;

    @ModifyArgs(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"
            )
    )
    public void injectSetRotation(Args args) {
        // TODO: fix
        args.set(0, tickDelta == 1.0F ? RotationUtil.virtualYaw : MathHelper.lerp(tickDelta, RotationUtil.virtualPrevYaw, RotationUtil.virtualYaw));
        args.set(1, tickDelta == 1.0F ? RotationUtil.virtualPitch : MathHelper.lerp(tickDelta, RotationUtil.virtualPrevPitch, RotationUtil.virtualPitch));
    }

    @Inject(
            method = "update",
            at = @At(
                    value = "HEAD"
            )
    )
    public void injectUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        this.tickDelta = tickDelta;
    }
}
