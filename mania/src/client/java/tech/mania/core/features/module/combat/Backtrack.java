package tech.mania.core.features.module.combat;

import tech.mania.core.features.event.GetPacketEvent;
import tech.mania.core.features.event.SendPacketEvent;
import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;

public class Backtrack extends Module {

    public Backtrack() {
        super("Backtrack", "A", ModuleCategory.Combat);
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        super.onSendPacket(event);
    }

    @Override
    public void onGetPacket(GetPacketEvent event) {
        super.onGetPacket(event);
    }
}
