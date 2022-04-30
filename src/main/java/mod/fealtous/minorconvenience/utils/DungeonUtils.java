package mod.fealtous.minorconvenience.utils;

import mod.fealtous.minorconvenience.MinorConvenience;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import org.apache.logging.log4j.Level;

import java.util.LinkedList;
import java.util.List;

public class DungeonUtils {
    public static LinkedList<Blaze> checkForBlaze() {
        LinkedList<Blaze> bOrdered = new LinkedList<>();
        ClientLevel cw = Minecraft.getInstance().level;

        if (cw != null) {
            cw.entitiesForRendering().forEach((entity -> {
                if (entity instanceof Blaze) {
                    bOrdered.add((Blaze) entity);
                }
            }));
            int i = 0;
            boolean flag = false;
            for (Blaze b : bOrdered) {

                List<Entity> stands1 = cw.getEntities(b, b.getBoundingBox().inflate(1.25));
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
                List<Entity> stands1 = cw.getEntities(blaze1, blaze1.getBoundingBox().inflate(1.25));
                List<Entity> stands2 = cw.getEntities(blaze2, blaze2.getBoundingBox().inflate(1.25));
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
