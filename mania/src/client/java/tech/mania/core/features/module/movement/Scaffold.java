package tech.mania.core.features.module.movement;

import net.minecraft.util.math.Vec3d;
import tech.mania.core.features.event.ClickTickEvent;
import tech.mania.core.features.event.RotationEvent;
import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;

public class Scaffold extends Module {

    public Scaffold() {
        super("Scaffold", "Place block at your feet", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onRotation(RotationEvent event) {
        final Vec3d eye = mc.player.getEyePos();
        super.onRotation(event);
    }

    @Override
    public void onClickTick(ClickTickEvent event) {
        super.onClickTick(event);
    }
}
