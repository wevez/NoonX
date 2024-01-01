package tech.mania.ui.util;

public class ClickUtil {

    public static boolean isHovered(double x, double y, double width, double height, double mouseX, double mouseY){
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }
}
