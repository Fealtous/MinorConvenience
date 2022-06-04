package mod.fealtous.minorconvenience;

import mod.fealtous.minorconvenience.eventhandlers.ChatEvents;
import mod.fealtous.minorconvenience.eventhandlers.DungeonEvents;
import mod.fealtous.minorconvenience.eventhandlers.Hotkeys;
import mod.fealtous.minorconvenience.nondungeonsolvers.ExperimentSolver;
import net.minecraft.block.Block;
import net.minecraft.block.StemBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file

@Mod("minorconvenience")
public class MinorConvenience
{
    public static final String modid = "minorconvenience";
    // Directly reference a log4j logger.
    public static final Logger logger = LogManager.getLogger();

    public MinorConvenience() {
        // Register ourselves for server and other game events we are interested in
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(Hotkeys.class);
        MinecraftForge.EVENT_BUS.register(DungeonEvents.class);
        MinecraftForge.EVENT_BUS.register(ChatEvents.class);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ExperimentSolver.class);

    }

    public void clientSetup (FMLClientSetupEvent e) {

    }
    //Event to stop player from breaking stems or the block below accidentally.
    //Server is not notified of action when cancelled.

    @SubscribeEvent
    public void superGigaStemProtector(InputEvent.ClickInputEvent e) {
        ClientWorld world = Minecraft.getInstance().world;

        if (!e.isAttack() || world == null) return;
        Object o = Minecraft.getInstance().objectMouseOver;
        BlockRayTraceResult blockLook = null;
        if (o instanceof BlockRayTraceResult) {
            blockLook = (BlockRayTraceResult ) o;
        }
        if (blockLook == null) return; //Safety nulls... should never occur.
        Block block = world.getBlockState(blockLook.getPos()).getBlock();
        Block blockBelow = world.getBlockState(blockLook.getPos().up(1)).getBlock();
        Block blockAbove = world.getBlockState(blockLook.getPos().down(1)).getBlock();

        if (Minecraft.getInstance().player == null) return;
        //If attempting to mine either the farmland below the stem or the stem itself.
        //Mining allowed if using shovel or hoe.
        if ((block instanceof StemBlock || blockBelow instanceof StemBlock || blockAbove instanceof StemBlock) &&
                !(Minecraft.getInstance().player.getHeldItemMainhand().getItem() instanceof HoeItem) &&
                !(Minecraft.getInstance().player.getHeldItemMainhand().getItem() instanceof ShovelItem)) {
            e.setSwingHand(false);
            e.setCanceled(true);
        }
    }
}
