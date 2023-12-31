package tech.mania.mixin.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.mania.Mania;
import tech.mania.core.features.event.GetPacketEvent;
import tech.mania.core.features.event.SendPacketEvent;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(
            method = "send(Lnet/minecraft/network/packet/Packet;)V",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void injectSend(Packet<?> packet, CallbackInfo ci) {
        final SendPacketEvent event = new SendPacketEvent(packet);
        Mania.getEventManager().call(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void injectChannelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        final GetPacketEvent event = new GetPacketEvent(packet);
        Mania.getEventManager().call(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
