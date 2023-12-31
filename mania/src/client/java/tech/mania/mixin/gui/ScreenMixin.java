package tech.mania.mixin.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.mania.Mania;
import tech.mania.core.features.event.Render2DEvent;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(
            method = "render",
            at = @At(
                    value = "TAIL"
            )
    )
    public void injectRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        final Render2DEvent render2DEvent = new Render2DEvent(delta);
        Mania.getEventManager().call(render2DEvent);
    }
}
