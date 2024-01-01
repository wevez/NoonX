package tech.mania.ui.click;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import tech.mania.core.types.module.ModuleCategory;
import tech.mania.ui.util.ClickGuiWindow;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {

    private final List<ClickGuiWindow> windows = new ArrayList<>();

    public ClickGui() {
        super(Text.literal(""));
        double currentX = 50;
        for (ModuleCategory c : ModuleCategory.values()) {
            windows.add(new ClickGuiWindow(currentX, 30, c));
            currentX += 150;
        }
    }

    @Override
    protected void init() {
        windows.forEach(ClickGuiWindow::init);
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        windows.forEach(m -> m.render(context, mouseX, mouseY, delta));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        windows.forEach(m -> m.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        windows.forEach(m -> m.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        windows.forEach(m -> m.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
