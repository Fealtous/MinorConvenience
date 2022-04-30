package mod.fealtous.minorconvenience.utils;

/*
public class ScoreboardUtil {

    //Had to custom write this, don't know what's wrong
    public static String cleanInput(TextComponent scoreboard) {
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
     *//*
    public static List<TextComponent> getSidebars() {
        List<TextComponent> bars = new ArrayList<>();
        if (Minecraft.getInstance().level == null) return bars;
        Scoreboard scoreboard = Minecraft.getInstance().level.getScoreboard();
        if (scoreboard == null) return bars;
        Collection<Objective> obj = scoreboard.getObjectives();

        if (obj == null) return bars;
        Objective[] scores = scoreboard.displayObjectives;
        //if score is not null and playername is not null and is not an info thing
        //then add it to the list
        List<Score> list = Arrays.stream(scores).filter(i -> i != null &&
                i.getName() != null &&
                !i.getFormattedDisplayName().getString().startsWith("#")
        ).collect(Collectors.toList());
        //for a list greater than 15, skip a few, otherwise it's a good list.
        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() -15));
        }
        else {
            scores = list;
        }
        for (Score s : scores) {
            PlayerTeam team = scoreboard.getPlayersTeam(s.getOwner());
            TextComponent pname = new TextComponent(s.getOwner());
            bars.add((TextComponent) PlayerTeam.formatNameForTeam(team, pname));
        }
        return bars;



    }
}
*/