package main.java.net.mvndicraft.townyroads.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import main.java.net.mvndicraft.townyroads.Road;
import main.java.net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.command.CommandSender;

@CommandAlias("townyroadsadmin|troadsadmin|tra")
@CommandPermission(TownyRoadsPlugin.ADMIN_PERMISSION)
public class TownyRoadsAdminCommand extends BaseCommand {

    @Default
    @Description("Lists the version of the plugin")
    public static void onTownyRoadsAdmin(CommandSender commandSender) {
        commandSender.sendMessage(TownyRoadsPlugin.getInstance().toString());
    }

    @Subcommand("reload")
    @Description("Reloads the plugin")
    public static void onReload(CommandSender commandSender) { TownyRoadsPlugin.getInstance().reloadConfig(); }

    @Subcommand("create")
    @Description("Creates a road")
    public static void onCreate(CommandSender commandSender, String townName1, String townName2) {
        Town town1 = getTownFromNameOrNull(commandSender, townName2);
        if (town1 == null)
            return;
        Town town2 = getTownFromNameOrNull(commandSender, townName2);
        if (town2 == null)
            return;
        Road road = new Road(List.of(town1, town2));
        road.save();
    }

    private static Town getTownFromNameOrNull(CommandSender commandSender, String townName) {
        Town town = TownyAPI.getInstance().getTown(townName);
        if (town == null || town.isRuined()) {
            commandSender.sendMessage("Town " + townName + " does not exist or is ruined.");
        }
        return town;
    }

}
