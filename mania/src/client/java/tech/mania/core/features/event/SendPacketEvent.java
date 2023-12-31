package tech.mania.core.features.event;

import net.minecraft.network.packet.Packet;
import tech.mania.core.types.event.EventArgument;
import tech.mania.core.types.event.EventListener;

public class SendPacketEvent extends EventArgument {
  private final Packet<?> PACKET;
  
  public SendPacketEvent(Packet<?> PACKET) {
/* 13 */     this.PACKET = PACKET;
  }
  
  public final Packet<?> getPacket() {
/* 17 */     return this.PACKET;
  }

  @Override
  public void call(EventListener listener) {
    listener.onSendPacket(this);
  }
}