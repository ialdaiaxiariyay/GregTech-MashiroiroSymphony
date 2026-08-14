package top.ialdaiaxiariyay.gtbss.api;

import java.util.function.IntFunction;

import static net.minecraft.ChatFormatting.*;

public class GTBSSValues {

    public static final String[] MN = new String[15];
    public static final String[] MNF = new String[31];
    public static final String[] MNF_EN = new String[15];
    public static final String[] MNF_CN = new String[15];

    static {
        String[][] data = {
                { "AWK", "Awakener", "觉醒者" },
                { "DRI", "Dream Initiate", "入梦学徒" },
                { "SHP", "Shaper", "幻形师" },
                { "LWA", "Lucid Walker", "清醒行者" },
                { "PWV", "Phase Weaver", "相位编织者" },
                { "CDI", "Causal Distorter", "因果扰动者" },
                { "AMA", "Alchemical Master", "炼金大师" },
                { "DRL", "Dream Lord", "梦境领主" },
                { "OBS", "Observer", "观测者" },
                { "DIE", "Divergence Engineer", "世界线操纵者" },
                { "STL", "Spacetime Lord", "时空领主" },
                { "PRA", "Primordial Alchemist", "炼金原初者" },
                { "DWV", "Dreamweaver", "幻梦编织者" },
                { "ITL", "Interpreter of the Line", "线之诠释者" },
                { "TRS", "Transcendent", "超越者" },
        };

        for (int i = 0; i < data.length; i++) {
            String abbr = data[i][0];
            String eng = data[i][1];
            String chi = data[i][2];

            MN[i] = abbr;
            MNF[i] = applyColor(abbr, i);
            MNF_EN[i] = applyColor(eng, i);
            MNF_CN[i] = applyColor(chi, i);
        }
    }

    public static final IntFunction<String> NRB_PLUS_FORMAT = (value) -> "" + RED + BOLD + "T" +
            GREEN + BOLD + "R" +
            BLUE + BOLD + "S" +
            YELLOW + BOLD + "+" +
            RED + BOLD + value;

    private static String applyColor(String name, int tier) {
        return switch (tier) {
            case 0 -> DARK_GRAY + name;
            case 1 -> GRAY + name;
            case 2 -> AQUA + name;
            case 3 -> GOLD + name;
            case 4 -> DARK_PURPLE + name;
            case 5 -> BLUE + name;
            case 6 -> LIGHT_PURPLE + name;
            case 7 -> RED + name;
            case 8 -> DARK_AQUA + name;
            case 9 -> DARK_RED + name;
            case 10 -> GREEN + name;
            case 11 -> DARK_GREEN + name;
            case 12 -> YELLOW + name;
            case 13 -> BLUE.toString() + BOLD + name;
            case 14 -> RED.toString() + BOLD + name;
            case 15 -> NRB_PLUS_FORMAT.apply(1);
            case 16 -> NRB_PLUS_FORMAT.apply(2);
            case 17 -> NRB_PLUS_FORMAT.apply(3);
            case 18 -> NRB_PLUS_FORMAT.apply(4);
            case 19 -> NRB_PLUS_FORMAT.apply(5);
            case 20 -> NRB_PLUS_FORMAT.apply(6);
            case 21 -> NRB_PLUS_FORMAT.apply(7);
            case 22 -> NRB_PLUS_FORMAT.apply(8);
            case 23 -> NRB_PLUS_FORMAT.apply(9);
            case 24 -> NRB_PLUS_FORMAT.apply(10);
            case 25 -> NRB_PLUS_FORMAT.apply(11);
            case 26 -> NRB_PLUS_FORMAT.apply(12);
            case 27 -> NRB_PLUS_FORMAT.apply(13);
            case 28 -> NRB_PLUS_FORMAT.apply(14);
            case 29 -> NRB_PLUS_FORMAT.apply(15);
            case 30 -> NRB_PLUS_FORMAT.apply(16);
            default -> name;
        };
    }
}
