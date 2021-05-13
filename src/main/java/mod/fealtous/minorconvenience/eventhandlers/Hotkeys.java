package mod.fealtous.minorconvenience.eventhandlers;

import mod.fealtous.minorconvenience.MinorConvenience;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class Hotkeys {
    private static final List<Pair<KeyBinding, Runnable>> KEYBINDS = new LinkedList<>();
    //Cadiboo method.
    private static Pair<KeyBinding, Runnable> makeKeybind(String name, int key, Runnable act) {
        KeyBinding keyBinding = new KeyBinding(MinorConvenience.modid + ".key." + name, key, MinorConvenience.modid + ".keycategory");
        ClientRegistry.registerKeyBinding(keyBinding);
        return Pair.of(keyBinding, act);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        for (Pair<KeyBinding, Runnable> kb: KEYBINDS) {
            if (kb.getKey().isPressed()) {
                kb.getValue().run();
            }
        }
    }
    static {
        KEYBINDS.add(makeKeybind("openechest", GLFW.GLFW_KEY_C, Hotkeys::openEnderChest));
        KEYBINDS.add(makeKeybind("openpets", GLFW.GLFW_KEY_P, Hotkeys::openPets));
        KEYBINDS.add(makeKeybind("openwd", GLFW.GLFW_KEY_O, Hotkeys::openWD));
    }
    public static void openPets() {
        if (Minecraft.getInstance().currentScreen != null) return;
        Minecraft.getInstance().player.sendChatMessage("/pets");
    }
    public static void openWD() {
        if (Minecraft.getInstance().currentScreen != null) return;
        Minecraft.getInstance().player.sendChatMessage("/wd");
    }
    public static void openEnderChest() {
        if (Minecraft.getInstance().currentScreen != null) return;
        Minecraft.getInstance().player.sendChatMessage("/ec");
    }
}
