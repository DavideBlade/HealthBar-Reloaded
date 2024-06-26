package com.gmail.davideblade99.healthbar.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class MobBarsUtil {

    /** Enforce non-instantiability with a private constructor */
    private MobBarsUtil() {
        throw new IllegalAccessError();
    }

    /**
     * Used to retrieve the array that contains the health bars from the configs
     *
     * @param barStyle Bar style
     *
     * @return An array in which each element represents the bar to be shown based on the corresponding health of the mob
     */
    @NotNull
    public static String[] getDefaultsBars(final int barStyle) {
        final String[] barArray = new String[20];

        switch (barStyle) {
            case 2:
                barArray[0] = "§c|§7|||||||||||||||||||";
                barArray[1] = "§c||§7||||||||||||||||||";
                barArray[2] = "§c|||§7|||||||||||||||||";
                barArray[3] = "§c||||§7||||||||||||||||";
                barArray[4] = "§e|||||§7|||||||||||||||";
                barArray[5] = "§e||||||§7||||||||||||||";
                barArray[6] = "§e|||||||§7|||||||||||||";
                barArray[7] = "§e||||||||§7||||||||||||";
                barArray[8] = "§e|||||||||§7|||||||||||";
                barArray[9] = "§e||||||||||§7||||||||||";
                barArray[10] = "§a|||||||||||§7|||||||||";
                barArray[11] = "§a||||||||||||§7||||||||";
                barArray[12] = "§a|||||||||||||§7|||||||";
                barArray[13] = "§a||||||||||||||§7||||||";
                barArray[14] = "§a|||||||||||||||§7|||||";
                barArray[15] = "§a||||||||||||||||§7||||";
                barArray[16] = "§a|||||||||||||||||§7|||";
                barArray[17] = "§a||||||||||||||||||§7||";
                barArray[18] = "§a|||||||||||||||||||§7|";
                barArray[19] = "§a||||||||||||||||||||";
                break;
            case 3:
                barArray[0] = "§c❤§7❤❤❤❤❤❤❤❤❤";
                barArray[1] = "§c❤§7❤❤❤❤❤❤❤❤❤";
                barArray[2] = "§c❤❤§7❤❤❤❤❤❤❤❤";
                barArray[3] = "§c❤❤§7❤❤❤❤❤❤❤❤";
                barArray[4] = "§e❤❤❤§7❤❤❤❤❤❤❤";
                barArray[5] = "§e❤❤❤§7❤❤❤❤❤❤❤";
                barArray[6] = "§e❤❤❤❤§7❤❤❤❤❤❤";
                barArray[7] = "§e❤❤❤❤§7❤❤❤❤❤❤";
                barArray[8] = "§e❤❤❤❤❤§7❤❤❤❤❤";
                barArray[9] = "§e❤❤❤❤❤§7❤❤❤❤❤";
                barArray[10] = "§a❤❤❤❤❤❤§7❤❤❤❤";
                barArray[11] = "§a❤❤❤❤❤❤§7❤❤❤❤";
                barArray[12] = "§a❤❤❤❤❤❤❤§7❤❤❤";
                barArray[13] = "§a❤❤❤❤❤❤❤§7❤❤❤";
                barArray[14] = "§a❤❤❤❤❤❤❤❤§7❤❤";
                barArray[15] = "§a❤❤❤❤❤❤❤❤§7❤❤";
                barArray[16] = "§a❤❤❤❤❤❤❤❤❤§7❤";
                barArray[17] = "§a❤❤❤❤❤❤❤❤❤§7❤";
                barArray[18] = "§a❤❤❤❤❤❤❤❤❤❤";
                barArray[19] = "§a❤❤❤❤❤❤❤❤❤❤";
                break;
            case 4:
                barArray[0] = "§a▌§8▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌";
                barArray[1] = "§a▌▌§8▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌";
                barArray[2] = "§a▌▌▌§8▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌";
                barArray[3] = "§a▌▌▌▌§8▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌";
                barArray[4] = "§a▌▌▌▌▌§8▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌";
                barArray[5] = "§a▌▌▌▌▌▌§8▌▌▌▌▌▌▌▌▌▌▌▌▌▌";
                barArray[6] = "§a▌▌▌▌▌▌▌§8▌▌▌▌▌▌▌▌▌▌▌▌▌";
                barArray[7] = "§a▌▌▌▌▌▌▌▌§8▌▌▌▌▌▌▌▌▌▌▌▌";
                barArray[8] = "§a▌▌▌▌▌▌▌▌▌§8▌▌▌▌▌▌▌▌▌▌▌";
                barArray[9] = "§a▌▌▌▌▌▌▌▌▌▌§8▌▌▌▌▌▌▌▌▌▌";
                barArray[10] = "§a▌▌▌▌▌▌▌▌▌▌▌§8▌▌▌▌▌▌▌▌▌";
                barArray[11] = "§a▌▌▌▌▌▌▌▌▌▌▌▌§8▌▌▌▌▌▌▌▌";
                barArray[12] = "§a▌▌▌▌▌▌▌▌▌▌▌▌▌§8▌▌▌▌▌▌▌";
                barArray[13] = "§a▌▌▌▌▌▌▌▌▌▌▌▌▌▌§8▌▌▌▌▌▌";
                barArray[14] = "§a▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌§8▌▌▌▌▌";
                barArray[15] = "§a▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌§8▌▌▌▌";
                barArray[16] = "§a▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌§8▌▌▌";
                barArray[17] = "§a▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌§8▌▌";
                barArray[18] = "§a▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌§8▌";
                barArray[19] = "§a▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌";
                break;
            case 5:
                barArray[0] = "§c█§0█████████";
                barArray[1] = "§c█§0█████████";
                barArray[2] = "§c██§0████████";
                barArray[3] = "§c██§0████████";
                barArray[4] = "§e███§0███████";
                barArray[5] = "§e███§0███████";
                barArray[6] = "§e████§0██████";
                barArray[7] = "§e████§0██████";
                barArray[8] = "§e█████§0█████";
                barArray[9] = "§e█████§0█████";
                barArray[10] = "§a██████§0████";
                barArray[11] = "§a██████§0████";
                barArray[12] = "§a███████§0███";
                barArray[13] = "§a███████§0███";
                barArray[14] = "§a████████§0██";
                barArray[15] = "§a████████§0██";
                barArray[16] = "§a█████████§0█";
                barArray[17] = "§a█████████§0█";
                barArray[18] = "§a██████████";
                barArray[19] = "§a██████████";
                break;
            default:
                // Default (1 or anything else)
                barArray[0] = "§c▌                   ";
                barArray[1] = "§c█                  ";
                barArray[2] = "§c█▌                 ";
                barArray[3] = "§c██                ";
                barArray[4] = "§e██▌               ";
                barArray[5] = "§e███              ";
                barArray[6] = "§e███▌             ";
                barArray[7] = "§e████            ";
                barArray[8] = "§e████▌           ";
                barArray[9] = "§e█████          ";
                barArray[10] = "§a█████▌         ";
                barArray[11] = "§a██████        ";
                barArray[12] = "§a██████▌       ";
                barArray[13] = "§a███████      ";
                barArray[14] = "§a███████▌     ";
                barArray[15] = "§a████████    ";
                barArray[16] = "§a████████▌   ";
                barArray[17] = "§a█████████  ";
                barArray[18] = "§a█████████▌ ";
                barArray[19] = "§a██████████";
                break;
        }

        return barArray;
    }


    /**
     * Load the bars from a custom file
     *
     * @param config Configuration file from which to retrieve custom bar settings
     *
     * @return An array in which each element represents the bar to be shown based on the corresponding health of the mob. If
     * the element is {@code null}, it means that no bar is set (and therefore nothing will be shown).
     */
    @NotNull
    public static String[] getCustomBars(@NotNull final FileConfiguration config) {
        final String[] barArray = new String[20];

        for (int i = 1; i < 21; i++) {
            final String cname = config.getString(i * 5 + "-percent-bar");
            if (cname != null)
                barArray[i - 1] = Utils.replaceSymbols(cname);
        }

        return barArray;
    }
}
