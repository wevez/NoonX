package tech.mania.core.features.event;

import tech.mania.core.types.event.EventArgument;
import tech.mania.core.types.event.EventListener;

public class KeyPressEvent extends EventArgument {
  private final int KEY_CODE;
  private final int SCAN_CODE;
  
  public KeyPressEvent(int KEY_CODE, int SCAN_CODE) {
    this.SCAN_CODE = SCAN_CODE;
    this.KEY_CODE = KEY_CODE;
  }
  
  public final int getKeyCode() {
/* 15 */     return this.KEY_CODE;
  }

  public final int getScanCode() {
    return this.SCAN_CODE;
  }

  @Override
  public void call(EventListener listener) {
    listener.onKeyPress(this);
  }
}