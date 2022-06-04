package mod.fealtous.minorconvenience.eventhandlers;


import mod.fealtous.minorconvenience.utils.StringUtilManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
        Minecraft minecraft = Minecraft.getInstance();

        if (e.getType() != ChatType.CHAT) return;
        String msg = StringUtilManager.cleanInput(e.getMessage());
        StringUtilManager.applyCopyText(e.getMessage());
        if (msg.equals(savedMessage)) {
            e.setCanceled(true);
        }
        else {
            if (msg.contains("you hear the sound of something")) {
                ClientPlayerEntity player = minecraft.player;
                minecraft.world.playSound(player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 1f, 1f, false);
                return;
            }
            if (msg.matches("Don Expresso") || msg.startsWith("This ability is on cooldown") ||
            msg.startsWith("There blocks in the way")) {
                e.setCanceled(true);
                return;
            }
            savedMessage = msg;
        }

    }
}
