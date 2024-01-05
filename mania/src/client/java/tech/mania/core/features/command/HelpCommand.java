package tech.mania.core.features.command;

import net.minecraft.text.Text;
import tech.mania.MCHook;
import tech.mania.Mania;
import tech.mania.core.types.command.Command;

public class HelpCommand extends Command implements MCHook {
    public HelpCommand() {
        super("help", ".help");
    }

    @Override
    public boolean execute(String[] args) {
        Mania.getCommandManager().getCommands().forEach(c -> {
            mc.inGameHud.getChatHud().addMessage(Text.literal(c.getHint()));
        });

        return false;
    }
}
