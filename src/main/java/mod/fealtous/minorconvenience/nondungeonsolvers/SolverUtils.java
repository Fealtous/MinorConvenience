package mod.fealtous.minorconvenience.nondungeonsolvers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

public class SolverUtils {
    public static void drawOnSlot(MatrixStack matrixStack, ContainerScreen<?> gui, int xPos, int yPos, int color) {
       int guiLeft = gui.getGuiLeft();
       int guiTop = gui.getGuiTop();

       int x = guiLeft + xPos;
       int y = guiTop + yPos;

       matrixStack.push();
       RenderSystem.disableDepthTest();
       Screen.fill(matrixStack, x, y, x+16, y+16, color);
       matrixStack.pop();

    }
}
