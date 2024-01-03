package tech.mania.mixin.gui;

import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.mania.MCHook;
import tech.mania.Mania;

@Mixin(ChatScreen.class)
public class ChatScreenMixin implements MCHook {

    @Inject(
            method = "sendMessage",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void injectSendMessage(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        boolean cancel = Mania.getCommandManager().execute(chatText);

        if (cancel) {
            mc.inGameHud.getChatHud().addToMessageHistory(chatText);
            mc.setScreen(null);
            cir.cancel();
        }
    }
}
