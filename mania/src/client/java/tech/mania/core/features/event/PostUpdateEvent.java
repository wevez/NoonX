package tech.mania.core.features.event;

import tech.mania.core.types.event.EventArgument;
import tech.mania.core.types.event.EventListener;

public class PostUpdateEvent extends EventArgument
{
  @Override
  public void call(EventListener listener) {
    listener.onPostUpdate(this);
  }
}