package tech.mania.core.features.module.player;

import tech.mania.core.features.event.PreUpdateEvent;
import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;

public class ChestStealer extends Module {

    public ChestStealer() {
        super("ChestStealer", "", ModuleCategory.Player);
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.currentScreen == null) return;
        super.onPreUpdate(event);
    }
}
