package tech.mania.core.features.module.combat;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.hit.EntityHitResult;
import tech.mania.core.features.event.ClickTickEvent;
import tech.mania.core.features.event.RotationEvent;
import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;

public class AntiFireball extends Module {

    private FireballEntity target;

    public AntiFireball() {
        super("AntiFireball", "", ModuleCategory.Combat);
    }

    @Override
    public void onRotation(RotationEvent event) {
        super.onRotation(event);
    }

    @Override
    public void onClickTick(ClickTickEvent event) {
        if (target == null) return;
        if (mc.crosshairTarget == null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult)) return;
        super.onClickTick(event);
    }
}
