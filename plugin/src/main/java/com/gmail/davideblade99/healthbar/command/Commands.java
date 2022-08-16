package com.gmail.davideblade99.healthbar.command;

import com.gmail.davideblade99.healthbar.HealthBar;
import com.gmail.davideblade99.healthbar.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Class that handles all the plugin commands
 */
public final class Commands implements CommandExecutor {

    private final HealthBar plugin;

    public Commands(@NotNull final HealthBar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd, @NotNull final String label, @NotNull final String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission(Permissions.LIST_OF_COMMANDS)) {
                noPermissionMessage(sender);
                return true;
            }

            sendCommandList(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(Permissions.RELOAD_COMMAND)) {
                noPermissionMessage(sender);
                return true;
            }

            reloadConfigs(sender);
            return true;
        }

        sender.sendMessage(HealthBar.CHAT_PREFIX + "§eUnknown command. Type §a" + label + " §efor help.");
        return true;
    }

    /**
     * Reload configuration files and notify on completion
     *
     * @param sender Executor of the command to be notified of the reload
     */
    private void reloadConfigs(@NotNull final CommandSender sender) {
        try {
            plugin.reloadConfigFromDisk();
            sender.sendMessage("§e>>§6 HealthBar reloaded");
        } catch (final Exception e) {
            e.printStackTrace();
            sender.sendMessage("§cFailed to reload configs, take a look at the console!");
        }

    }

    /**
     * Send the main information about the plugin
     *
     * @param sender Who executed the command and should receive the information
     */
    private void sendInfo(@NotNull final CommandSender sender) {
        sender.sendMessage(HealthBar.CHAT_PREFIX);
        sender.sendMessage("§aVersion: §7" + plugin.getDescription().getVersion());
        sender.sendMessage("§aDeveloper: §7DavideBlade");
        sender.sendMessage("§aCreator: §7filoghost");
        sender.sendMessage("§aCommands: §7/hbr help");
    }

    /**
     * Send the list of commands
     *
     * @param sender Command sender to whom to send the command list
     */
    private void sendCommandList(@NotNull final CommandSender sender) {
        sender.sendMessage("§e>>§6 HealthBar commands: ");
        sender.sendMessage("§2/hbr §7- §aDisplays general plugin info");
        sender.sendMessage("§2/hbr reload §7- §aReloads the configs");
    }

    /**
     * Notify the executor that it does not have permission to run the command
     *
     * @param sender Command sender
     */
    private void noPermissionMessage(@NotNull final CommandSender sender) {
        sender.sendMessage("§cYou don't have permission.");
    }
}
