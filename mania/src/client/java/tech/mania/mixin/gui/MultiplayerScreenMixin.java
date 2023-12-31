package tech.mania.mixin.gui;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.mania.MCHook;
import tech.mania.Mania;
import tech.mania.api.LoginApi;
import tech.mania.core.util.RandomUtil;
import tech.mania.mixin.client.MinecraftClientAccessor;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin implements MCHook {

    @Inject(
            method = "init",
            at = @At(
                    value = "TAIL"
            )
    )
    public void injectInit(CallbackInfo ci) {
        final MultiplayerScreen casted = (MultiplayerScreen) (Object) this;
        final ScreenAccessor access = (ScreenAccessor) this;
        final MinecraftClientAccessor clientAccessor = (MinecraftClientAccessor) mc;

        access.accessAddDrawableChild(new ButtonWidget.Builder(Text.literal("Clipboard"), button -> {
            new Thread(() -> {
                final String[] sp = mc.keyboard.getClipboard()
                        .trim()
                        .split(":");
                if (sp.length != 2) {
                    Mania.LOGGER.info("Clipboard login failed: sp.length != 2");
                    return;
                }
                final Session loginResult = LoginApi.msLogin(sp[0], sp[1]);

                if (loginResult == null) return;
                clientAccessor.setSession(loginResult);

                Mania.LOGGER.info(String.format("Successfully login with account %s", loginResult.getUsername()));
            }).start();
        }).dimensions(4, casted.height - 28, 70, 20).build());

        access.accessAddDrawableChild(new ButtonWidget.Builder(Text.literal("Random Cracked"), button -> {
            clientAccessor.setSession(new Session(RandomUtil.nextString(5), UUID.randomUUID(), "", Optional.empty(), Optional.empty(), Session.AccountType.LEGACY));
        }).dimensions(4, casted.height - 42, 70, 20).build());
    }
}
