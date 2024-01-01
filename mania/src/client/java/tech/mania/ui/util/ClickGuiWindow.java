package tech.mania.ui.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.opengl.GL11;
import tech.mania.MCHook;
import tech.mania.Mania;
import tech.mania.core.features.setting.*;
import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;
import tech.mania.core.types.setting.Setting;
import tech.mania.ui.font.TTFFontRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.x150.renderer.render.Renderer2d.renderQuad;

public class ClickGuiWindow implements MCHook {
    private static final Color color = new Color(0xff2C508A);
    private static final Color backColor = new Color(0xff373737);
    private static final Color blackColor = new Color(0xff202020);
    private static final Color settingBackColor = new Color(0xff252525);
    private static final Color settingColor = new Color(0xfff0f0f0);

    private static TTFFontRenderer font = TTFFontRenderer.of("Roboto-Regular", 8);
    private static TTFFontRenderer font2 = TTFFontRenderer.of("Roboto-Regular", 6);

    private double x, y, lastX, lastY;
    private boolean dragging = false, expand = false;

    private final ModuleCategory category;
    private final List<Module> modules;
    private final boolean[] mExpand;

    public ClickGuiWindow(double x, double y, ModuleCategory category) {
        this.x = x;
        this.y = y;
        this.category = category;
        modules = Mania.getModuleManager()
                .getModules()
                .stream()
                .filter(m -> m.getCategory() == category)
                .toList();
        mExpand = new boolean[modules.size()];
    }

    public void init() {
        font = TTFFontRenderer.of("Roboto-Regular", 8);
        font2 = TTFFontRenderer.of("Roboto-Regular", 8);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (dragging) {
            x = mouseX + lastX;
            y = mouseY + lastY;
        }
        renderQuad(context.getMatrices(), color, x - 2, y - 2, x + 122, y + 20);
        renderQuad(context.getMatrices(), blackColor, x - 1, y - 1, x + 121, y + 19);
        renderQuad(context.getMatrices(), new Color(0xff262626), x, y, x + 120, y + 18);
        renderQuad(context.getMatrices(), blackColor, x - 1, y + 17, x + 121, y + 18);
        font.drawString(context.getMatrices(), category.name(), x + 4, y + 4, -1);

        if (!expand) {
            return;
        }

        double currentY = y + 18;
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            renderQuad(context.getMatrices(), color, x - 2, currentY, x + 122, currentY + 20);
            renderQuad(context.getMatrices(), blackColor, x - 1, currentY, x + 121, currentY + 19);
            renderQuad(context.getMatrices(), m.isEnabled() ? color : backColor, x, currentY, x + 120, currentY + 18);
            font2.drawString(context.getMatrices(), m.getName(), x + 116 - font2.getStringWidth(m.getName()), currentY + 4, -1);
            currentY += 18;

            if (!mExpand[i]) {
                continue;
            }

            for (int j = 0; j < m.getSettings().size(); j++) {
                final Setting s = m.getSettings().get(j);
                renderQuad(context.getMatrices(), color, x - 2, currentY, x + 122, currentY + 20);
                renderQuad(context.getMatrices(), blackColor, x - 1, currentY, x + 121, currentY + 19);
                if (s instanceof DoubleSetting) {
                    renderQuad(context.getMatrices(), new Color(0xff313131), x, currentY, x + 120, currentY + 18);
                    font2.drawString(context.getMatrices(), s.getName(), x + 4, currentY + 4, 0xffd0d0d0);
                } else if (s instanceof ModeSetting) {
                    final ModeSetting ms = (ModeSetting) s;
                    renderQuad(context.getMatrices(), new Color(0xff313131), x, currentY, x + 120, currentY + 18);
                    font2.drawString(context.getMatrices(), s.getName(), x + 4, currentY + 4, 0xffd0d0d0);
                    font2.drawString(context.getMatrices(), ms.getValue(), x + 116 - font2.getStringWidth(ms.getValue()), currentY + 4, -1);
                } else if (s instanceof BooleanSetting) {
                    final BooleanSetting bs = (BooleanSetting) s;
                    renderQuad(context.getMatrices(), bs.getValue() ? color : new Color(0xff313131), x, currentY, x + 120, currentY + 18);
                    font2.drawString(context.getMatrices(), s.getName(), x + 4, currentY + 4, bs.getValue() ? -1 : 0xffd0d0d0);
                }
                currentY += 18;
            }
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (ClickUtil.isHovered(x, y, 140, 18, mouseX, mouseY)) {
            if (button == 0) {
                lastX = x - mouseX;
                lastY = y - mouseY;
                dragging = true;
            } else {
                expand = !expand;
            }
            return;
        }

        if (!expand) {
            return;
        }

        double currentY = y + 18;
        for (int i = 0; i < modules.size(); i++) {
            final Module m = modules.get(i);
            if (ClickUtil.isHovered(x, currentY, 120,  18, mouseX, mouseY)) {
                if (button == 0) {
                    m.toggle();
                } else {
                    mExpand[i] = !mExpand[i];
                }
                return;
            }
            currentY += 18;
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {

    }
}
