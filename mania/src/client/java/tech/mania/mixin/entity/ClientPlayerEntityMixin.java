package tech.mania.mixin.entity;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import tech.mania.Mania;
import tech.mania.core.features.event.MoveEvent;
import tech.mania.core.features.event.PostUpdateEvent;
import tech.mania.core.features.event.PreUpdateEvent;
import tech.mania.core.features.event.SlowdownEvent;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Shadow public abstract void move(MovementType movementType, Vec3d movement);

    @Redirect(
            method = "canStartSprinting",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
            )
    )
    private boolean hookSprintAffectStart(ClientPlayerEntity playerEntity) {
        final SlowdownEvent event = new SlowdownEvent();
        Mania.getEventManager().call(event);
        if (!event.slowdown) return false;

        return playerEntity.isUsingItem();
    }

    @ModifyArgs(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"
            )
    )
    public void injectMove(Args args) {
        final Vec3d movement = args.get(1);

        final MoveEvent event = new MoveEvent(movement.x, movement.y, movement.z);
        Mania.getEventManager().call(event);

        args.set(1, new Vec3d(event.x, event.y, event.z));
    }

    @Inject(
            method = "sendMovementPackets",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void injectSendMovementPacketsPre(CallbackInfo ci) {
        final PreUpdateEvent event = new PreUpdateEvent();
        Mania.getEventManager().call(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "sendMovementPackets",
            at = @At(
                    value = "TAIL"
            )
    )
    public void injectSendMovementPacketsPost(CallbackInfo ci) {
        final PostUpdateEvent event = new PostUpdateEvent();
        Mania.getEventManager().call(event);
    }
}
