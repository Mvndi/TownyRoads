package net.mvndicraft.townyroads.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.mvndicraft.townyroads.ChunkCoord;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.permissions.TownyRoadsPermissionNodes;
import net.mvndicraft.townyroads.util.TownyUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("townyroads|troads|tr")
public class TownyRoadsCommand extends BaseCommand {

    @Default
    @Description("See a road")
    @CommandCompletion("@road @empty")
    public static void onTownyRoads(CommandSender commandSender) {
        // empty
    }

    @Subcommand("road")
    @Description("See a road")
    @CommandCompletion("@road @empty")
    @Syntax("<road>")
    public static void onTownyRoads(CommandSender commandSender, String roadName) {
        Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
        if (road != null) {
            commandSender.sendMessage(road.getDescription());
        }
    }

    @Subcommand("here")
    @Description("See road here")
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
    public static void onList(CommandSender commandSender) {
        onList(commandSender, 1);
    }


    @Subcommand("create")
    @Description("Creates a road")
    @CommandCompletion("@reachable_road_towns_create @empty")
    @Syntax("<town>")
    public static void onCreate(CommandSender commandSender, String townName2) {
        if (commandSender instanceof Player player) {
            Town playerTown = TownyAPI.getInstance().getTown(player);
            if (playerTown == null || playerTown.isRuined()) {
                commandSender.sendMessage("You must be in a town.");
                return;
            }
            Town town2 = TownyUtil.getTownFromNameOrNull(commandSender, townName2);
            if (town2 == null)
                return;
            if (playerTown.equals(town2)) {
                commandSender.sendMessage("Towns must be different.");
                return;
            }
            if (!player.hasPermission("townyroads.create")) {
                commandSender.sendMessage("You do not have permission to create roads.");
                return;
            }
            List<Town> towns = List.of(playerTown, town2);
            if (TownyRoadsPlugin.getInstance().getRoadManager().getRoadWithEveryTown(towns) != null) {
                commandSender.sendMessage("A road already exists connecting both towns.");
                return;
            }
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().createRoad(towns, List.of(town2));
            commandSender
                    .sendMessage("Created road " + road.getName() + " (" + town2.getName() + " needs to confirm).");
        } else {
            commandSender.sendMessage("You must be a player.");
        }
    }

    @Subcommand("accept")
    @Description("Accept to join a road")
    @CommandCompletion("@acceptable_road @empty")
    @Syntax("<road>")
    public static void onAccept(CommandSender commandSender, String roadName) {
        if (commandSender instanceof Player player) {
            Town playerTown = TownyAPI.getInstance().getTown(player);
            if (playerTown == null || playerTown.isRuined()) {
                commandSender.sendMessage("You must be in a town.");
                return;
            }
            Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
            if (road == null) {
                commandSender.sendMessage("Road not found.");
                return;
            }

            if (TownyUniverse.getInstance().getPermissionSource().testPermission(player,
                    TownyRoadsPermissionNodes.TOWNYROADS_ACCEPT.getNode())) {
                road.confirm(playerTown);
                commandSender.sendMessage("Joined the road " + road.getName());
            }
        }
    }

    @Subcommand("deny")
    @Description("Deny to join a road")
    @CommandCompletion("@acceptable_road @empty")
    @Syntax("<road>")
    public static void onDeny(CommandSender commandSender, String roadName) {
        if (commandSender instanceof Player player) {
            Town playerTown = TownyAPI.getInstance().getTown(player);
            if (playerTown == null || playerTown.isRuined()) {
                commandSender.sendMessage("You must be in a town.");
                return;
            }
            Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
            if (road == null) {
                commandSender.sendMessage("Road not found.");
                return;
            }

            if (TownyUniverse.getInstance().getPermissionSource().testPermission(player,
                    TownyRoadsPermissionNodes.TOWNYROADS_ACCEPT.getNode())) {
                road.deny(playerTown);
                commandSender.sendMessage("Denied the road " + road.getName());
            }
        }
    }

    @Subcommand("leave")
    @Description("Leave a road")
    @CommandCompletion("@road_player_town_is_in @empty")
    @Syntax("<road>")
    public static void onLeave(CommandSender commandSender, String roadName) {
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

    @Subcommand("claim")
    @Description("Claim a chunk of a road")
    @CommandCompletion("@next_by_roads_then_empty_with_player_town @empty")
    @Syntax("<road>")
    public static void onClaim(CommandSender commandSender, String roadName) {
        if (commandSender instanceof Player player) {
            Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
            if (road == null) {
                commandSender.sendMessage("Road not found.");
                return;
            }
            if (!road.isAPlayerOfTheRoad(player)) {
                commandSender.sendMessage("You must be in one of the towns.");
                return;
            }
            if (!road.canClaimMore()) {
                commandSender.sendMessage("Can't claim more chunks.");
                return;
            }
            if (!road.canClaimHere(ChunkCoord.from(player.getLocation()))) {
                commandSender
                        .sendMessage("Can't claim here. This chunks is already claimed or to far away from the road.");
                return;
            }
            if (TownyRoadsPlugin.getInstance().getRoadManager().claimRoad(road, player)) {
                commandSender.sendMessage("Claimed road " + roadName);
            } else {
                commandSender.sendMessage("failed to claim road " + roadName);
            }
        } else {
            commandSender.sendMessage("You must be a player.");
        }
    }

    @Subcommand("unclaim")
    @Description("Unclaim a chunk of a road")
    public static void onunclaim(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(player.getLocation());
            if (road == null) {
                commandSender.sendMessage("Road not found.");
                return;
            }
            if (!road.isAPlayerOfTheRoad(player)) {
                commandSender.sendMessage("You must be in one of the towns.");
                return;
            }
            if (!road.canUnclaimHere(ChunkCoord.from(player.getLocation()))) {
                commandSender.sendMessage("Can't unclaim here. The road would be split in two.");
                return;
            }
            TownyRoadsPlugin.getInstance().getRoadManager().unclaimRoad(road, player);
            commandSender.sendMessage("Unclaimed road " + road.getName());

        } else {
            commandSender.sendMessage("You must be a player.");
        }
    }

    @Subcommand("validate")
    @Description("Validate a road")
    @CommandCompletion("@road_player_town_is_in @empty")
    @Syntax("<road>")
    public static void onValidate(CommandSender commandSender, String roadName) {
        Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
        if (road == null)
            return;
        if (!commandSender.hasPermission("townyroads.validate")) {
            commandSender.sendMessage("You do not have permission to validate roads.");
            return;
        }
        Optional<Component> error = road.validate();
        if (error.isPresent()) {
            commandSender.sendMessage(Component.text("Road " + road.getName() + " is invalid. ").append(error.get()));
        } else {
            commandSender.sendMessage("Road " + road.getName() + " have been validated.");
        }
    }

    @Subcommand("merge")
    @Description("Merge 2 roads")
    @CommandCompletion("@road_player_town_is_in @roat @empty")
    @Syntax("<road> <road>")
    public static void onMerge(CommandSender commandSender, String roadName1, String roadName2) {
        if (commandSender instanceof Player player) {
            Road road1 = TownyUtil.getRoadFromNameOrNull(commandSender, roadName1);
            if (road1 == null)
                return;
            Road road2 = TownyUtil.getRoadFromNameOrNull(commandSender, roadName2);
            if (road2 == null)
                return;

            if (!commandSender.hasPermission("townyroads.merge")) {
                commandSender.sendMessage("You do not have permission to merge roads.");
                return;
            }

            if (!road1.isAPlayerOfTheRoad(player) || !road2.isAPlayerOfTheRoad(player)) {
                commandSender.sendMessage("You must be in one of the towns of both roads.");
                return;
            }

            Optional<Component> error = road1.merge(road2);
            if (error.isPresent()) {
                commandSender.sendMessage(Component.text("Merge failed. ").append(error.get()));
            } else {
                commandSender.sendMessage("Road " + road2.getName() + " have been merged into " + road1.getName());
            }
        } else {
            commandSender.sendMessage("You must be a player.");
        }
    }
}
