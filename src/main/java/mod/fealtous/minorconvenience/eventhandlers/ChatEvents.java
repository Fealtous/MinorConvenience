package mod.fealtous.minorconvenience.eventhandlers;


import mod.fealtous.minorconvenience.utils.ScoreboardUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Class for any non-dungeons chat related events.
 */
public class ChatEvents {
    //Filters out:
    // - duplicate messages
    // - annoying RWT spam
    // - cooldown notifications
    // todo Add chat stacking
    static String savedMessage = "";

    @SubscribeEvent
    public static void chatMessageFilter(ClientChatReceivedEvent e) {
        Minecraft minecraft = Minecraft.getInstance();

        if (e.getType() != ChatType.CHAT) return;
        String msg = ScoreboardUtil.cleanInput(e.getMessage());
        if (msg.equals(savedMessage)) {
            List<ChatLine<ITextComponent>> messagelist = minecraft.ingameGUI.getChatGUI().chatLines;
            messagelist.set(0, new ChatLine<ITextComponent>(0, new StringTextComponent("thisisatest"), 0));

            e.setCanceled(true);
        }
        else {
            if (msg.matches("Don Expresso") || msg.startsWith("This ability is on cooldown") ||
            msg.startsWith("There blocks in the way")) {
                e.setCanceled(true);
                return;
            }
            savedMessage = msg;
        }

    }
}
