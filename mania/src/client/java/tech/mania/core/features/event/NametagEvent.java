package tech.mania.core.features.event;

import net.minecraft.entity.LivingEntity;
import tech.mania.core.types.event.EventArgument;
import tech.mania.core.types.event.EventListener;

public class NametagEvent extends EventArgument {
  private final LivingEntity ENTITY;
  
  public NametagEvent(LivingEntity ENTITY) {
/* 12 */     this.ENTITY = ENTITY;
  }
  
  public final LivingEntity EntityLivingBase() {
/* 16 */     return this.ENTITY;
  }

  @Override
  public void call(EventListener listener) {
    listener.onNametag(this);
  }
}