package tech.mania.mixin.client;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.mania.Mania;
import tech.mania.core.features.event.AttackEvent;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(
            method = "attackEntity",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void injectAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        final AttackEvent event = new AttackEvent(target);
        Mania.getEventManager().call(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
