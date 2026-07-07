package net.mvndicraft.townyroads.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.util.Messaging;
import net.mvndicraft.townyroads.util.TownyUtil;
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
    public static void onReload(CommandSender commandSender) {
        TownyRoadsPlugin.getInstance().reloadConfig();
    }

    @Subcommand("create")
    @Description("Create a road")
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
            Messaging.sendError(commandSender, "err_same_town");
            return;
        }
        List<Town> towns = List.of(town1, town2);
        if (TownyRoadsPlugin.getInstance().getRoadManager().getRoadWithEveryTown(towns) != null) {
            commandSender.sendMessage("A road already exists connecting both towns.");
            return;
        }
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().createRoad(towns, List.of());
        commandSender.sendMessage("Created road " + road.getName());
    }

    @Subcommand("acceptall")
    @Description("Accept for all towns to join a road")
    @CommandCompletion("@any_acceptable_road @empty")
    @Syntax("<road>")
    public static void onAccept(CommandSender commandSender, String roadName) {
        Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
        if (road == null)
            return;

        road.confirmAll();
        commandSender.sendMessage("Confirmed all town for road " + road.getName());
    }

    @Subcommand("claim")
    @Description("Claim a chunk of a road")
    @CommandCompletion("@next_by_roads_then_empty @empty")
    @Syntax("<road>")
    public static void onClaim(CommandSender commandSender, String roadName) {
        if (commandSender instanceof Player player) {
            Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
            if (road == null)
                return;
            if (TownyRoadsPlugin.getInstance().getRoadManager().claimRoad(road, player)) {
                commandSender.sendMessage("Claimed road " + roadName);
            } else {
                commandSender.sendMessage("failed to claim road " + roadName);
            }
        } else {
            TownyRoadsCommand.notAPlayer(commandSender);
        }
    }

    @Subcommand("unclaim")
    @Description("Unclaim a chunk of a road")
    public static void onUnclaim(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(player.getLocation());
            if (road == null) {
                commandSender.sendMessage("You are not on a road.");
                return;
            }
            TownyRoadsPlugin.getInstance().getRoadManager().unclaimRoad(road, player);
        } else {
            TownyRoadsCommand.notAPlayer(commandSender);
        }
    }

    @Subcommand("delete")
    @Description("Delete a road")
    @CommandCompletion("@road @empty")
    @Syntax("<road>")
    public static void onCreate(CommandSender commandSender, String roadName) {
        Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
        if (road == null)
            return;
        TownyRoadsPlugin.getInstance().getRoadManager().deleteRoad(road);
        commandSender.sendMessage("Deleted road " + roadName);
    }

    @Subcommand("kick")
    @Description("Kick a town from a road")
    @CommandCompletion("@road @town_in_road @empty")
    @Syntax("<road> <town>")
    public static void onKick(CommandSender commandSender, String roadName, String townName) {
        Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
        if (road == null)
            return;
        TownyRoadsPlugin.getInstance().getRoadManager().deleteRoad(road);
        commandSender.sendMessage("Deleted road " + roadName);
    }

    @Subcommand("validate")
    @Description("Validate a road")
    @CommandCompletion("@road @force @empty")
    @Syntax("<road> <force>")
    public static void onValidate(CommandSender commandSender, String roadName, boolean force) {
        Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
        if (road == null)
            return;

        Optional<Component> error = road.validate(force);
        if (error.isPresent()) {
            commandSender.sendMessage(Component.text("Road " + road.getName() + " is invalid. ").append(error.get()));
        } else {
            commandSender.sendMessage("Road " + road.getName() + " have been validated.");
        }
    }
    @Subcommand("validate")
    @Description("Validate a road")
    @CommandCompletion("@road @force @empty")
    @Syntax("<road> <force>")
    public static void onValidate(CommandSender commandSender, String roadName) {
        onValidate(commandSender, roadName, false);
    }

    @Subcommand("merge")
    @Description("Merge 2 roads")
    @CommandCompletion("@road @road @force @empty")
    @Syntax("<road> <road> <force>")
    public static void onMerge(CommandSender commandSender, String roadName1, String roadName2, boolean force) {
        Road road1 = TownyUtil.getRoadFromNameOrNull(commandSender, roadName1);
        if (road1 == null)
            return;
        Road road2 = TownyUtil.getRoadFromNameOrNull(commandSender, roadName2);
        if (road2 == null)
            return;

        Optional<Component> error = road1.merge(road2, force);
        if (error.isPresent()) {
            commandSender.sendMessage(Component.text("Merge failed. ").append(error.get()));
        } else {
            commandSender.sendMessage("Road " + road2.getName() + " have been merged into " + road1.getName());
        }
    }
    @Subcommand("merge")
    @Description("Merge 2 roads")
    @CommandCompletion("@road @road @force @empty")
    @Syntax("<road> <road> <force>")
    public static void onMerge(CommandSender commandSender, String roadName1, String roadName2) {
        onMerge(commandSender, roadName1, roadName2, false);
    }

    @Subcommand("listCooldowns")
    @Description("List all road cooldowns")
    public static void onListCooldowns(CommandSender commandSender) {
        commandSender.sendMessage(TownyRoadsPlugin.getInstance().getPlayerCooldownManager().toString());
    }

    @Subcommand("revalidateAllValidatedRoads")
    @Description("Revalidate all validated roads")
    public static void onRevalidateAllValidatedRoads(CommandSender commandSender) {
        TownyRoadsPlugin.getInstance().getRoadManager().revalidateAllValidatedRoads();
    }
}
