package mod.fealtous.minorconvenience.utils;

import mod.fealtous.minorconvenience.MinorConvenience;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.logging.log4j.Level;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DungeonUtils {
    public static LinkedList<BlazeEntity> checkForBlaze() {
        LinkedList<BlazeEntity> bOrdered = new LinkedList<>();
        ClientWorld cw = Minecraft.getInstance().world;

        if (cw != null) {
            cw.getAllEntities().forEach((entity -> {
                if (entity instanceof BlazeEntity) {
                    bOrdered.add((BlazeEntity) entity);
                }
            }));
            int i = 0;
            boolean flag = false;
            for (BlazeEntity b : bOrdered) {
                List<Entity> stands1 = cw.getEntitiesInAABBexcluding(b, new AxisAlignedBB((b.getPosition())).grow(1.25), null);
                String[] s = stands1.get(0).getName().getString().split(" ");
                if (!s[s.length-1].split("/")[0].matches("[0-9]+")) {
                    flag = true;
                    break;
                }
                i++;
            }
            if (flag) {
                bOrdered.remove(i);
            }
            bOrdered.sort((blaze1, blaze2) -> {
                int hp1, hp2;
                List<Entity> stands1 = cw.getEntitiesInAABBexcluding(blaze1, new AxisAlignedBB(blaze1.getPosition()).grow(1.25), null);
                List<Entity> stands2 = cw.getEntitiesInAABBexcluding(blaze2, new AxisAlignedBB(blaze2.getPosition()).grow(1.25), null);
                String[] s1 = stands1.get(0).getName().getString().split(" ");
                String[] s2 = stands2.get(0).getName().getString().split(" ");
                try { //I don't know why, but occasionally it grabs random ass armor stands with non-hp related names so we're going with this
                    hp1 = Integer.parseInt(s1[s1.length - 1].split("/")[0]);
                    hp2 = Integer.parseInt(s2[s2.length - 1].split("/")[0]);
                } catch (Exception e) {
                    hp1 = 0;
                    hp2 = 1;
                    MinorConvenience.logger.log(Level.INFO, "Invalid blaze found");
                }
                if (hp1 > hp2) {
                    return 1;
                }
                return hp1 == hp2 ? 0 : -1;
            });
        }

        return bOrdered;
    }
}
