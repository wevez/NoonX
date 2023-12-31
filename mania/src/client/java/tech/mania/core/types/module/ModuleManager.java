package tech.mania.core.types.module;

import tech.mania.core.features.module.combat.*;
import tech.mania.core.features.module.player.*;

import java.util.Arrays;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules;

    public ModuleManager() {
        modules = Arrays.asList(
                new KillAura(),
                new AutoSprint()
        );
    }

    public List<Module> getModules() {
        return this.modules;
    }
}
