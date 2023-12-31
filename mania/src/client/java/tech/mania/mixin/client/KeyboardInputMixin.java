package tech.mania.mixin.client;

import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import tech.mania.core.features.event.InputEvent;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    @Inject(
            method = "tick",
            at = @At(
                    value = "TAIL"
            )
    )
    public void injectTick(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        final KeyboardInput casted = (KeyboardInput) (Object) this;
        final InputEvent event = new InputEvent(casted);
        if (event.moveFix) {
            // TODO
        }
    }
}
