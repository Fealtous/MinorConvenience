package mod.fealtous.minorconvenience.nondungeonsolvers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class ExperimentSolver {
    static Slot[] clickInOrderSlots = new Slot[36];
    static int lastClicked = 0;
    public static int NEXT;


    /*
    //Test gui overlay code
    @SubscribeEvent
    public static void testGuiRender(GuiScreenEvent.DrawScreenEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.currentScreen == null) return;
        if (!(mc.currentScreen instanceof ChestScreen)) return;
        for (Slot slot : ((ChestScreen) mc.currentScreen).getContainer().inventorySlots) {
            if (slot.getHasStack()) {
                SolverUtils.drawOnSlot(e.getMatrixStack(), (ChestScreen) mc.currentScreen, slot.xPos, slot.yPos, 0xE5000000);
            }
        }
    }
    */
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        if (mc.currentScreen instanceof ChestScreen) {
            if ( player == null) return;
            List<Slot> invSlots = ((ChestScreen) mc.currentScreen).getContainer().inventorySlots;
            String inventoryName = mc.currentScreen.getTitle().getString().trim();
            if (inventoryName.startsWith("Ultrasequencer (")) {
                if (invSlots.get(49).getHasStack() && invSlots.get(49).getStack().getDisplayName().getString().contains("Remember")) {
                    for (Slot slot : invSlots) {
                        if (!slot.getHasStack()) continue;
                        String itemname = StringUtils.stripControlCodes(slot.getStack().getDisplayName().getString());
                        if (itemname.matches("\\d+")) {
                            clickInOrderSlots[Integer.parseInt(itemname) -1] = slot;
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.currentScreen instanceof ChestScreen)) return;
        if (e.getGui().getTitle().getString().trim().startsWith("Ultrasequencer (")) {
            List<Slot> invSlots = ((ChestScreen) mc.currentScreen).getContainer().inventorySlots;
            if (invSlots.size() > 48 && invSlots.get(49).getHasStack()) {
                if (invSlots.get(49).getStack().getDisplayName().getString().contains("Timer")) {
                    lastClicked = 0;
                    for (Slot slot : clickInOrderSlots) {
                        if (slot != null && slot.getHasStack() && StringUtils.stripControlCodes(slot.getStack().getDisplayName().getString()).matches("\\d+")) {
                            int n = Integer.parseInt(StringUtils.stripControlCodes(slot.getStack().getDisplayName().getString()));
                            if (n > lastClicked) {
                                lastClicked = n;
                            }
                        }
                    }
                    if (clickInOrderSlots[lastClicked] != null) {
                        Slot nextSlot = clickInOrderSlots[lastClicked];
                        SolverUtils.drawOnSlot(e.getMatrixStack(), (ContainerScreen<?>) e.getGui(), nextSlot.xPos, nextSlot.yPos, NEXT + 0x77000000);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onUltraGuiOpen(GuiOpenEvent e) {
        clickInOrderSlots = new Slot[36];
    }

}
