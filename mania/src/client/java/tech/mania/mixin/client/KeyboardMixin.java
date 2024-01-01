package tech.mania.mixin.client;

import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.mania.MCHook;
import tech.mania.Mania;
import tech.mania.core.features.event.KeyPressEvent;
import tech.mania.core.types.module.Module;

@Mixin(Keyboard.class)
public class KeyboardMixin implements MCHook {

    @Inject(
            method = "onKey",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void injectOnKe(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (mc.currentScreen == null && action == 1) {
            if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                mc.setScreen(Mania.getClickGui());
            }

            Mania.getModuleManager().getModules().stream()
                    .filter(m -> m.keyCode == key)
                    .forEach(Module::toggle);
        }

        final KeyPressEvent event = new KeyPressEvent(key, action);
        Mania.getEventManager().call(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
