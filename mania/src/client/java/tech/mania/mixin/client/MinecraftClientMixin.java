package tech.mania.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import tech.mania.Mania;
import tech.mania.core.features.event.ClickTickEvent;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@ModifyArgs(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/Window;setPhase(Ljava/lang/String;)V"
            )
    )
    public void onPostSetup(Args args) {
        if (args.get(0).equals("Post startup")) {
            Mania.init();
        }
    }

    @Inject(
            method = "handleInputEvents",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void injectHandleInputEvents(CallbackInfo ci) {
        final ClickTickEvent event = new ClickTickEvent();
        Mania.getEventManager().call(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}