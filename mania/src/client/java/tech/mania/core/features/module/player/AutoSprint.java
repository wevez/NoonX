package tech.mania.core.features.module.player;

import org.lwjgl.glfw.GLFW;
import tech.mania.core.features.event.PreUpdateEvent;
import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;

public class AutoSprint extends Module {

    public AutoSprint() {
        super("AutoSprint", "Makes you sprinting", ModuleCategory.Player);
        this.keyCode = GLFW.GLFW_KEY_O;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        mc.options.sprintKey.setPressed(true);
        super.onPreUpdate(event);
    }
}
