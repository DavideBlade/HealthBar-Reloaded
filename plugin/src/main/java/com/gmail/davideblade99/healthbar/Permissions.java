package com.gmail.davideblade99.healthbar;

/**
 * Class containing all the various permissions of the plugin
 */
public final class Permissions {

    /** Permission required to view the list of plugin commands */
    public final static String LIST_OF_COMMANDS = "healthbar.help";

    /** Permission to use the /hbr reload command */
    public final static String RELOAD_COMMAND = "healthbar.reload";

    /** Permission to see the health bar */
    public final static String SEE_BAR = "healthbar.see";

    private Permissions() {
        throw new IllegalAccessError();
    }
}
