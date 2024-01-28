package tech.mania.core.features.module.combat;

import tech.mania.core.features.event.AttackEvent;
import tech.mania.core.features.event.InputEvent;
import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;

public class WTap extends Module {

    public WTap() {
        super("WTap","", ModuleCategory.Combat);
    }

    @Override
    public void onAttack(AttackEvent event) {
        super.onAttack(event);
    }

    @Override
    public void onInput(InputEvent event) {
        super.onInput(event);
    }
}
