package tech.mania.core.features.command;

import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import tech.mania.MCHook;
import tech.mania.Mania;
import tech.mania.core.types.command.Command;

public class BindCommand extends Command implements MCHook {

    public BindCommand() {
        super("Bind", ".bind <module name> <key name>");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length != 2) {
            return true;
        }

        if (Mania.getModuleManager().getModules().stream().noneMatch(c -> c.getName().equalsIgnoreCase(args[1]))) {
            mc.inGameHud.getChatHud().addMessage(Text.literal(String.format("Module '%S' not found", args[1])));
            return false;
        }

        Mania.getModuleManager().getModules().stream()
                .filter(m -> m.getName().equalsIgnoreCase(args[0]))
                .forEach(m -> {
                    InputUtil.Key key = InputUtil.fromTranslationKey(String.format("key.keyboard.%s", args[1].toLowerCase()));
                    m.keyCode = key.getCode();
                    mc.inGameHud.getChatHud().addMessage(Text.literal(String.format("Module %s is now bound with %s", m.getName(), key.getTranslationKey())));
                });

        return false;
    }
}
