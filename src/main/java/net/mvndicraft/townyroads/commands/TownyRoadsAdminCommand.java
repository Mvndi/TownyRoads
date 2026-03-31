package net.mvndicraft.townyroads.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    @CommandCompletion("@reachable_road_towns @reachable_road_towns @empty")
    @Syntax("<town> <town>")
    public static void onCreate(CommandSender commandSender, String townName1, String townName2) {
        Town town1 = getTownFromNameOrNull(commandSender, townName1);
        if (town1 == null)
            return;
        Town town2 = getTownFromNameOrNull(commandSender, townName2);
        if (town2 == null)
            return;
        if (town1.equals(town2)) {
            commandSender.sendMessage("Towns must be different.");
            return;
        }
        Road road = new Road(List.of(town1, town2));
        road.save();
        commandSender.sendMessage("Created road " + road.getName());
    }

    @Subcommand("claim")
    @Description("Creates a road")
    @CommandCompletion("@next_by_road @empty")
    @Syntax("<road>")
    public static void onClaim(CommandSender commandSender, String roadName) {
        if (commandSender instanceof Player player) {
            Road road = getRoadFromNameOrNull(commandSender, roadName);
            if (road == null)
                return;
            if (road.claim(player)) {
                commandSender.sendMessage("Claimed road " + roadName);
            } else {
                commandSender.sendMessage("failed to claim road " + roadName);
            }
        }
    }


    private static Town getTownFromNameOrNull(CommandSender commandSender, String townName) {
        Town town = TownyAPI.getInstance().getTown(townName);
        if (town == null || town.isRuined()) {
            commandSender.sendMessage("Town " + townName + " does not exist or is ruined.");
        }
        return town;
    }

    private static Road getRoadFromNameOrNull(CommandSender commandSender, String roadName) {
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadByName(roadName);
        if (road == null) {
            commandSender.sendMessage("Road " + roadName + " does not exist.");
        }
        return road;
    }

}
