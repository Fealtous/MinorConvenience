package mod.fealtous.minorconvenience.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardUtil {

    //Had to custom write this, don't know what's wrong
    public static String cleanInput(ITextComponent scoreboard) {
        char[] nvString = scoreboard.getString().toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        boolean flag = false;
        for (char c : nvString) {
            if ((int) c == 167) {
                flag = true;
                continue;
            }
            if (flag) {
                flag = false;
                continue;
            }
            if ((int) c > 20 && (int) c < 127) {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }
    /*
    Based off of Dankers, might clean up later if needed
     */
    public static List<ITextComponent> getSidebars() {
        List<ITextComponent> bars = new ArrayList<>();
        if (Minecraft.getInstance().world == null) return bars;
        Scoreboard scoreboard = Minecraft.getInstance().world.getScoreboard();
        if (scoreboard == null) return bars;
        ScoreObjective obj = scoreboard.getObjectiveInDisplaySlot(1);
        if (obj == null) return bars;
        Collection<Score> scores = scoreboard.getSortedScores(obj);
        List<Score> list = scores.stream().filter(i -> i != null &&
                i.getPlayerName() != null &&
                !i.getPlayerName().startsWith("#")
        ).collect(Collectors.toList());
        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() -15));
        }
        else {
            scores = list;
        }
        for (Score s : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(s.getPlayerName());
            ITextComponent pname = new StringTextComponent(s.getPlayerName());
            bars.add((ITextComponent) ScorePlayerTeam.func_237500_a_(team, pname));
        }
        return bars;



    }
}
