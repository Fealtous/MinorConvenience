package mod.fealtous.minorconvenience.eventhandlers;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.fealtous.minorconvenience.MinorConvenience;
import mod.fealtous.minorconvenience.nondungeonsolvers.SolverUtils;
import mod.fealtous.minorconvenience.utils.ScoreboardUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.*;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedList;
import java.util.List;

import static mod.fealtous.minorconvenience.utils.DungeonUtils.checkForBlaze;

@Mod.EventBusSubscriber(modid=MinorConvenience.modid)
public class DungeonEvents {

    private static int counter = 0;
    private static LinkedList<Blaze> bOrdered = new LinkedList<>();
    private static boolean inDungeons = false;

    @SubscribeEvent
    public static void regularCheck(TickEvent.ClientTickEvent e) {
        counter++;
        boolean flag = false; //Yes, I know it's ugly...
        if (counter % 200 == 0) {
            List<TextComponent> scoreboard = ScoreboardUtil.getSidebars();
            for (TextComponent s : scoreboard) {
                inDungeons = (ScoreboardUtil.cleanInput(s).contains("Catacombs"));
                if (inDungeons) break;
            }
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

        if (e.getEntity() instanceof Blaze) {
            if (bOrdered.isEmpty()) return;
            if (bOrdered.getFirst().getUUID() == (e.getEntity().getUUID())) {
                bOrdered.removeFirst();
            }
            else if(bOrdered.getLast().getUUID() == (e.getEntity().getUUID())) {
                bOrdered.removeLast();
            }
        }
    }

    @SubscribeEvent
    public static void renderBlazeIndicator(RenderLevelLastEvent e) {
        LevelRenderer levelRenderer = e.getLevelRenderer();

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        if (buffer == null) return;

        IVertex iVertexBuilder = buffer.getBuffer(RenderType.lines());

        Player player = Minecraft.getInstance().player;
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
        buffer.endBatch(RenderType.LINES);

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

        String message = StringUtil.stripColor(e.getMessage().getString());
        if (!message.contains("NPC")) return;
        for (String s : weirdoYes) {
            if (message.contains(s)) {
                TextComponent textComponent = (TextComponent) e.getMessage();
                textComponent.getStyle().applyFormat(ChatFormatting.GREEN);
                textComponent.getSiblings().forEach((i) -> i.getStyle().applyFormat(ChatFormatting.GREEN));
                e.setMessage(textComponent);
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
    private static void addToToolTip(List<Component> tt, ItemStack stack) {
        CompoundTag tags = stack.getTag();
        if (tags != null) {
            tags = tags.getCompound("ExtraAttributes");
            if (!tags.contains("baseStatBoostPercentage")) return;
            int statBoost = tags.getInt("baseStatBoostPercentage");
            int floor = tags.getInt("item_tier");
            TextComponent dungeonsTooltipText = new TextComponent("Floor: ");
            dungeonsTooltipText.getStyle().applyFormat(ChatFormatting.GOLD);
            dungeonsTooltipText.append(floor > 7 ? "M" + (floor - 6) : floor + " | " + statBoost + "%");
            tt.add(dungeonsTooltipText);

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
        Player player = mc.player;

        if (mc.screen instanceof ChestMenu) {
            if ( player == null) return;
            String inventoryName = mc.screen.getTitle().getString().trim();
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
            boolean startsWith = e.getGui().getTitle().getString().contains("What starts with");
            for (Slot s : ((ChestScreen) e.getGui()).getContainer().inventorySlots) {
                //Color terminal
                if (colors(s) && !startsWith) {
                    SolverUtils.drawOnSlot(e.getMatrixStack(), (ContainerScreen<?>) e.getGui(), s.xPos, s.yPos, 0x99000000);
                    System.out.println(s.getStack().getDisplayName());
                }
                //Starts with letter terminal
                if (s.getStack().getDisplayName().getString().startsWith(itemType) && startsWith) {
                    SolverUtils.drawOnSlot(e.getMatrixStack(), (ContainerScreen<?>) e.getGui(), s.xPos , s.yPos, 0x99000000);
                }
            }
        }
    }
    @SubscribeEvent

    public static void terminalOpen(PlayerContainerEvent.Open e) {

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
    public static void renderMinimap(RenderGameOverlayEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.player == null || mc.level == null) return;
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
                ms.scale(1.1f,1.1f,0);
                mir.renderMap(ms, buffer, data, false, 15728880);
                ms.pop();
            }

        }


    }

}

