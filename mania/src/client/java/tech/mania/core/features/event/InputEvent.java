package tech.mania.core.features.event;

import net.minecraft.client.input.Input;
import tech.mania.core.types.event.EventArgument;
import tech.mania.core.types.event.EventListener;

public class InputEvent extends EventArgument
{
  private final Input INPUT;
  public boolean moveFix = false;
  
  public InputEvent(Input input) {
    this.INPUT = input;
  }
  
  public Input getInput() {
/* 20 */     return this.INPUT;
  }

  @Override
  public void call(EventListener listener) {
    listener.onInput(this);
  }
}