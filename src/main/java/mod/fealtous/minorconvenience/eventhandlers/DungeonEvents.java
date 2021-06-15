package mod.fealtous.minorconvenience.eventhandlers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.fealtous.minorconvenience.MinorConvenience;
import mod.fealtous.minorconvenience.nondungeonsolvers.SolverUtils;
import mod.fealtous.minorconvenience.utils.ScoreboardUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MapItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.*;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

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
    public static void renderBlazeIndicator(RenderWorldLastEvent e) {
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
                WorldRenderer.drawBoundingBox(matrixStack, iVertexBuilder, bOrdered.getLast().getBoundingBox(), 0, 1, 0, 1);
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
            tt.add(0,new StringTextComponent("Floor: ").mergeStyle(TextFormatting.GOLD).appendString((floor > 7 ? "M" + (floor - 6) : floor) + " | " + statBoost + "%"));
        }
    }
    static String itemType;
    /*
    Terminal Solvers
     */
    @SubscribeEvent
    public static void necronTerminalSolve(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        if (mc.currentScreen instanceof ChestScreen) {
            if ( player == null) return;
            String inventoryName = mc.currentScreen.getTitle().getString().trim();
            if (inventoryName.startsWith("Select all the ")) {
                itemType = inventoryName.split(" ")[3];

            }
            if (inventoryName.startsWith("What starts with")) {
                itemType = inventoryName.split(" ")[3].substring(0,1);
            }
        }
    }

    @SubscribeEvent
    public static void necronTerminalRender(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (e.getGui() instanceof ChestScreen) {
            if (!inDungeons || itemType == null) return;
            for (Slot s : ((ChestScreen) e.getGui()).getContainer().inventorySlots) {
                //Color terminal
                if (colors(s) && !e.getGui().getTitle().getString().startsWith("What starts with")) {
                    SolverUtils.drawOnSlot(e.getMatrixStack(), (ContainerScreen<?>) e.getGui(), s.xPos, s.yPos, 0x99000000);
                    System.out.println(s.getStack().getDisplayName());
                }
                //Starts with letter terminal
                if (s.getStack().getDisplayName().getString().startsWith(itemType) && e.getGui().getTitle().getString().startsWith("What starts with")) {
                    SolverUtils.drawOnSlot(e.getMatrixStack(), (ContainerScreen<?>) e.getGui(), s.xPos , s.yPos, 0x99000000);
                }
            }
        }
    }
    @SubscribeEvent

    public static void terminalOpen(GuiOpenEvent e) {
        itemType = null;
    }
    private static boolean colors(Slot s) {
        String sName = StringUtils.stripControlCodes(s.getStack().getDisplayName().getString().toUpperCase());
        //Brute force any oddities
        if (sName.contains(itemType)) return true;
        if (itemType.equals("BLACK") && sName.contains("SAC")) return true;
        if (itemType.equals("SILVER") && sName.contains("LIGHT GRAY")) return true;
        if (itemType.equals("WHITE") && (sName.contains("MEAL") || sName.contains("WOOL"))) return true;
        if (itemType.equals("BLUE") && sName.contains("LAPIS") && !sName.contains("LIGHT")) return true;
        if (itemType.equals("BROWN") && sName.contains("BEAN")) return true;

        return false;
    }
    /*
    Minimap renderer
     */
    @SubscribeEvent
    public static void renderMinimap(RenderGameOverlayEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.player == null || mc.world == null) return;
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        if (buffer == null) return;
        ItemStack i = mc.player.inventory.mainInventory.get(8);
        if (i.getItem() instanceof FilledMapItem) {
            MapItemRenderer mir = mc.gameRenderer.getMapItemRenderer();
            MapData data = FilledMapItem.getMapData(i, mc.world);
            if (data != null) {

                MatrixStack ms = e.getMatrixStack();
                ms.push();
                ms.translate(5,5,0);
                ms.scale(.8f,.8f,0);
                mir.renderMap(ms, buffer, data, false, 15728880);
                ms.pop();
            }

        }
    }
}

