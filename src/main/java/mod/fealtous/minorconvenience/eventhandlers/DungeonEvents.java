package mod.fealtous.minorconvenience.eventhandlers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.fealtous.minorconvenience.MinorConvenience;
import mod.fealtous.minorconvenience.utils.ScoreboardUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedList;
import java.util.List;

import static mod.fealtous.minorconvenience.utils.DungeonUtils.checkForBlaze;

@Mod.EventBusSubscriber(modid=MinorConvenience.modid)
public class DungeonEvents {

    private static int counter = 0;
    private static LinkedList<BlazeEntity> bOrdered = new LinkedList<>();
    private static boolean inDungeons = false;

    @SubscribeEvent
    public static void regularCheck(TickEvent.ClientTickEvent e) {
        counter++;
        boolean flag = false; //Yes, I know it's ugly...
        if (counter % 200 == 0) {
            List<ITextComponent> scoreboard = ScoreboardUtil.getSidebars();
            for (ITextComponent s : scoreboard) {
                if (ScoreboardUtil.cleanInput(s).contains("Catacombs")) {
                    flag = true;
                }
            }
            inDungeons = flag;
        }
        if (!inDungeons && !bOrdered.isEmpty()) {
            bOrdered = new LinkedList<>();
        }
    }

    @SubscribeEvent
    public static void bowDraw(ArrowNockEvent e) {
        if (e.getPlayer() == Minecraft.getInstance().player && bOrdered.isEmpty() && inDungeons) {
            bOrdered = checkForBlaze();
        }
    }
    @SubscribeEvent
    public static void onKillBlaze(LivingDeathEvent e) {

        if (e.getEntity() instanceof BlazeEntity) {
            if (bOrdered.isEmpty()) return;
            if (bOrdered.getFirst().getUniqueID() == (e.getEntity().getUniqueID())) {
                bOrdered.removeFirst();
            }
            else if(bOrdered.getLast().getUniqueID() == (e.getEntity().getUniqueID())) {
                bOrdered.removeLast();
            }
        }
    }

    @SubscribeEvent
    public static void renderIndicator(RenderWorldLastEvent e) {
        MatrixStack matrixStack = e.getMatrixStack();
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        if (buffer == null) return;
        IVertexBuilder iVertexBuilder = buffer.getBuffer(RenderType.getLines());
        matrixStack.push();
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        matrixStack.translate(-player.getPosX(),-player.getPosYEye(),-player.getPosZ());
        if (!bOrdered.isEmpty()) {
            if (bOrdered.size() != 1) {
                WorldRenderer.drawBoundingBox(matrixStack, iVertexBuilder, bOrdered.getFirst().getBoundingBox(), 1, 0, 0, 1);
                WorldRenderer.drawBoundingBox(matrixStack, iVertexBuilder, bOrdered.getLast().getBoundingBox(), 0, 0, 1, 1);
            }
            else {
                WorldRenderer.drawBoundingBox(matrixStack, iVertexBuilder, bOrdered.get(0).getBoundingBox(), 1, 1, 1, 1);
            }
        }
        matrixStack.pop();
        buffer.finish(RenderType.LINES);

    }
    /*
    Three wierdos solver

    NOTES:
    When changing incoming messages, typecast to a IFormattableTextComponent so you have access to #setStyle
    Otherwise it seems you're unable to change the style...
     */
    static String [] weirdoYes = {"The reward is not in my chest!", "At least one of them is lying, and the reward is not in",
            "My chest doesn't have the reward. We are all telling the truth", "My chest has the reward and I'm telling the truth",
            "The reward isn't in any of our chests", "Both of them are telling the truth."};
    @SubscribeEvent
    public static void weirdoTextMarker(ClientChatReceivedEvent e) {
        if (e.getType() != ChatType.CHAT) return;
        String message = StringUtils.stripControlCodes(e.getMessage().getString());
        if (!message.contains("NPC")) return;
        for (String s : weirdoYes) {
            if (message.contains(s)) {
                IFormattableTextComponent iTextComponent = (IFormattableTextComponent) e.getMessage();
                iTextComponent.setStyle(e.getMessage().getStyle().setFormatting(TextFormatting.GREEN));
                iTextComponent.getSiblings().forEach((i) -> {
                    ((IFormattableTextComponent) i).setStyle(e.getMessage().getStyle().setFormatting(TextFormatting.GREEN));
                });
                e.setMessage(iTextComponent);
                break;
            }
        }
    }

    /*
    Dungeon Stat Boost Tooltip
     */
    @SubscribeEvent
    public static void dungeonItemBoost(ItemTooltipEvent e) {
        addToToolTip(e.getToolTip(), e.getItemStack());
    }
    private static void addToToolTip(List<ITextComponent> tt, ItemStack stack) {
        CompoundNBT tags = stack.getTag();
        if (tags != null) {
            tags = tags.getCompound("ExtraAttributes");
            if (!tags.contains("baseStatBoostPercentage")) return;
            int statBoost = tags.getInt("baseStatBoostPercentage");
            int floor = tags.getInt("item_tier");
            tt.add(0,new StringTextComponent("Floor: ").mergeStyle(TextFormatting.GOLD).appendString(floor > 7 ? "m" + (floor - 6) : floor + " | " + statBoost + "%"));
        }
    }

}

