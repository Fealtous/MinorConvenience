package mod.fealtous.minorconvenience.eventhandlers;

import mod.fealtous.minorconvenience.utils.ScoreboardUtil;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (e.getType() != ChatType.CHAT) return;
        String msg = ScoreboardUtil.cleanInput(e.getMessage());
        if (msg.equals(savedMessage)) {
            e.setCanceled(true);
        }
        else {

            if (msg.matches(".*[fF][rR][eE]{2}\\s\\d+.*[mM][iI][lL]{2}[iI][oO][nN].*") || msg.startsWith("This ability is on cooldown") ||
            msg.startsWith("There blocks in the way")) {
                e.setCanceled(true);
                return;
            }
            savedMessage = msg;
        }

    }
}
