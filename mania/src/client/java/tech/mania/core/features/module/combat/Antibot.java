package tech.mania.core.features.module.combat;

import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;

public class Antibot extends Module {

    public Antibot() {
        super("Antibot", "Prevent this client from attacking bots", ModuleCategory.Combat);
    }
}
