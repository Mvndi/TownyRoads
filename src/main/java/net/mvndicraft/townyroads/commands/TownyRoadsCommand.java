package net.mvndicraft.townyroads.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.util.TownyUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("townyroads|troads|tr")
public class TownyRoadsCommand extends BaseCommand {

    @Default
    @Description("Lists the version of the plugin")
    public static void onTownyRoads(CommandSender commandSender) { commandSender.sendMessage(TownyRoadsPlugin.getInstance().toString()); }

    @Subcommand("here")
    @Description("List roads")
    public static void onHere(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(player.getLocation());
            if (road != null) {
                commandSender.sendMessage(road.getDescription());
            } else {
                commandSender.sendMessage("You are not on a road.");
            }
        } else {
            commandSender.sendMessage("You must be a player.");
        }
    }

    @Subcommand("list")
    @Description("List roads")
    @Syntax("<page_number>")
    public static void onList(CommandSender commandSender, int page) {
        commandSender.sendMessage(TownyRoadsPlugin.getInstance().getRoadManager().listRoad(page));
    }

    @Subcommand("list")
    @Description("List roads")
    @Syntax("<page_number>")
    public static void onList(CommandSender commandSender) { onList(commandSender, 1); }


    @Subcommand("create")
    @Description("Creates a road")
    @CommandCompletion("@reachable_road_towns @reachable_road_towns @empty")
    @Syntax("<town> <town>")
    public static void onCreate(CommandSender commandSender, String townName1, String townName2) {
        Town town1 = TownyUtil.getTownFromNameOrNull(commandSender, townName1);
        if (town1 == null)
            return;
        Town town2 = TownyUtil.getTownFromNameOrNull(commandSender, townName2);
        if (town2 == null)
            return;
        if (town1.equals(town2)) {
            commandSender.sendMessage("Towns must be different.");
            return;
        }
        if (commandSender instanceof Player player) {
            if (!player.hasPermission("townyroads.create")) {
                commandSender.sendMessage("You do not have permission to create roads.");
                return;
            }
            Town playerTown = TownyAPI.getInstance().getTown(player);
            if (playerTown == null || playerTown.isRuined()) {
                commandSender.sendMessage("You must be in a town.");
                return;
            }
            if (!playerTown.equals(town1) && !playerTown.equals(town2)) {
                commandSender.sendMessage("You must be in one of the towns.");
                return;
            }
            List<Town> toConfirmTowns = playerTown.equals(town1) ? List.of(town2) : List.of(town1);
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().createRoad(List.of(town1, town2), toConfirmTowns);
            commandSender.sendMessage("Created road " + road.getName() + " (" + toConfirmTowns.get(0).getName() + " needs to confirm).");
        } else {
            commandSender.sendMessage("You must be a player.");
        }
    }

    @Subcommand("leave")
    @Description("Leave a road")
    @CommandCompletion("@road @empty")
    @Syntax("<road>")
    public static void onCreate(CommandSender commandSender, String roadName) {
        Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
        if (road == null)
            return;
        if (!commandSender.hasPermission("townyroads.delete")) {
            commandSender.sendMessage("You do not have permission to delete roads.");
            return;
        }
        if (commandSender instanceof Player player) {
            Town playerTown = TownyAPI.getInstance().getTown(player);
            if (playerTown == null || playerTown.isRuined()) {
                commandSender.sendMessage("You must be in a town.");
                return;
            }
            if (road.getTownsView().stream().noneMatch(playerTown::equals)) {
                commandSender.sendMessage("You must be in one of the towns.");
                return;
            }
            road.removeTown(playerTown);
            commandSender.sendMessage("Leaving the road " + roadName);
        }
    }
}
