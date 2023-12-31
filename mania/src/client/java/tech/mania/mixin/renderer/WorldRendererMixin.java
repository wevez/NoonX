package tech.mania.mixin.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.mania.Mania;
import tech.mania.core.features.event.PostRender3DEvent;
import tech.mania.core.features.event.PreRender3DEvent;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(
            method = "render",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void injectRenderPre(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        final PreRender3DEvent render3DEvent = new PreRender3DEvent(tickDelta);
        Mania.getEventManager().call(render3DEvent);
        if (render3DEvent.isCanceled()) {
            ci.cancel();
        }

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "TAIL"
            )
    )
    public void injectRenderPost(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        final PostRender3DEvent postRender3DEvent = new PostRender3DEvent(tickDelta);
        Mania.getEventManager().call(postRender3DEvent);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }
}
