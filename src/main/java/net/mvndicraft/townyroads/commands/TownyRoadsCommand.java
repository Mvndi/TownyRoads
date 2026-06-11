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
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.mvndicraft.townyroads.ChunkCoord;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.permissions.TownyRoadsPermissionNodes;
import net.mvndicraft.townyroads.util.Messaging;
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
            Messaging.sendMessage(commandSender, road.getDescription(isAdmin(commandSender)));
        }
    }

    @Subcommand("here")
    @Description("See road here")
    public static void onHere(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(player.getLocation());
            if (road != null) {
                Messaging.sendMessage(commandSender, road.getDescription(isAdmin(commandSender)));
            } else {
                notInRoad(commandSender);
            }
        } else {
            notAPlayer(commandSender);
        }
    }

    private static boolean isAdmin(CommandSender commandSender) {
        return TownyUniverse.getInstance().getPermissionSource().testPermission(commandSender,
                TownyRoadsPlugin.ADMIN_PERMISSION);
    }

    @Subcommand("list")
    @Description("List roads")
    @Syntax("<page_number>")
    public static void onList(CommandSender commandSender, int page) {
        Messaging.sendMessage(commandSender,
                TownyRoadsPlugin.getInstance().getRoadManager().listRoad(page, isAdmin(commandSender)));
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
                notInTown(commandSender);
                return;
            }
            Town town2 = TownyUtil.getTownFromNameOrNull(commandSender, townName2);
            if (town2 == null)
                return;
            if (playerTown.equals(town2)) {
                Messaging.sendError(commandSender, "err_same_town");
                return;
            }
            if (!TownyUniverse.getInstance().getPermissionSource().testPermission(commandSender,
                    TownyRoadsPermissionNodes.TOWNYROADS_CREATE.getNode())) {
                Messaging.sendError(commandSender, "err_no_permission_to_create_road");
                return;
            }
            List<Town> towns = List.of(playerTown, town2);
            if (TownyRoadsPlugin.getInstance().getRoadManager().getRoadWithEveryTown(towns) != null) {
                Messaging.sendError(commandSender, "err_road_exists");
                return;
            }
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().createRoad(towns, List.of(town2));
            commandSender
                    .sendMessage("Created road " + road.getName() + " (" + town2.getName() + " needs to confirm).");
        } else {
            notAPlayer(commandSender);
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
                notInTown(commandSender);
                return;
            }
            Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
            if (road == null) {
                roadNotFound(commandSender, roadName);
                return;
            }

            if (TownyUniverse.getInstance().getPermissionSource().testPermission(player,
                    TownyRoadsPermissionNodes.TOWNYROADS_ACCEPT.getNode())) {
                road.confirm(playerTown);
                Messaging.sendAccept(commandSender, Component.translatable("success_road_join",
                        Argument.component("road", Component.text(road.getName()))));
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
                notInTown(commandSender);
                return;
            }
            Road road = TownyUtil.getRoadFromNameOrNull(commandSender, roadName);
            if (road == null) {
                roadNotFound(commandSender, roadName);
                return;
            }

            if (TownyUniverse.getInstance().getPermissionSource().testPermission(player,
                    TownyRoadsPermissionNodes.TOWNYROADS_ACCEPT.getNode())) {
                road.deny(playerTown);
                Messaging.sendDeny(commandSender, Component.translatable("denied_road_join",
                        Argument.component("road", Component.text(road.getName()))));
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
        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(commandSender,
                TownyRoadsPermissionNodes.TOWNYROADS_LEAVE.getNode())) {
            Messaging.sendError(commandSender, "err_no_permission_to_delete_road");
            return;
        }
        if (commandSender instanceof Player player) {
            Town playerTown = TownyAPI.getInstance().getTown(player);
            if (playerTown == null || playerTown.isRuined()) {
                notInTown(commandSender);
                return;
            }
            if (road.getTownsView().stream().noneMatch(playerTown::equals)) {
                Messaging.sendError(commandSender, "err_not_in_road_towns");
                return;
            }
            road.removeTown(playerTown);
            Messaging.sendSuccess(player,
                    Component.translatable("success_leave_road", Argument.component("road", Component.text(roadName))));
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
                roadNotFound(commandSender, roadName);
                return;
            }
            if (!road.isAPlayerOfTheRoad(player)) {
                Messaging.sendError(commandSender, "err_not_in_road_towns");
                return;
            }
            if (!road.canClaimMore()) {
                Messaging.sendError(commandSender, "err_road_can_t_claim_more");
                return;
            }
            if (!road.canClaimHere(ChunkCoord.from(player.getLocation()))) {
                Messaging.sendError(commandSender, "err_road_cant_claim_here");
                return;
            }
            if (TownyRoadsPlugin.getInstance().getRoadManager().claimRoad(road, player)) {
                Messaging.sendSuccess(commandSender,
                        Component.translatable("success_road_claim",
                                Argument.component("number_of_claim", Component.text(1)),
                                Argument.component("road", Component.text(roadName))));
                if (road.isValid()) {
                    road.unvalidate();
                    Messaging.sendMessage(commandSender, Component.translatable("info_road_need_to_be_revalidated",
                            Argument.component("road", Component.text(roadName))));
                }
            } else {
                Messaging.sendError(commandSender, Component.translatable("err_road_claim_failed",
                        Argument.component("road", Component.text(roadName))));
            }
        } else {
            notAPlayer(commandSender);
        }
    }

    @Subcommand("unclaim")
    @Description("Unclaim a chunk of a road")
    public static void onunclaim(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(player.getLocation());
            if (road == null) {
                notInRoad(commandSender);
                return;
            }
            if (!road.isAPlayerOfTheRoad(player)) {
                Messaging.sendError(commandSender, "err_not_in_road_towns");
                return;
            }
            if (!road.canUnclaimHere(ChunkCoord.from(player.getLocation()))) {
                Messaging.sendError(commandSender, Component.translatable("err_road_unclaim_failed_because_split",
                        Argument.component("road", (Component.text(road.getName())))));
                return;
            }
            TownyRoadsPlugin.getInstance().getRoadManager().unclaimRoad(road, player);
            Messaging.sendAccept(commandSender,
                    Component.translatable("success_road_unclaim",
                            Argument.component("number_of_unclaim", Component.text(1)),
                            Argument.component("road", Component.text(road.getName()))));

        } else {
            notAPlayer(commandSender);
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
        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(commandSender,
                TownyRoadsPermissionNodes.TOWNYROADS_VALIDATE.getNode())) {
            Messaging.sendError(commandSender, "err_no_permission_to_validate_road");
            return;
        }
        Optional<Component> error = road.validate();
        if (error.isPresent()) {
            Messaging.sendError(commandSender, Component
                    .translatable("err_road_invalid", Argument.component("road", Component.text(road.getName())))
                    .appendSpace().append(error.get()));
        } else {
            Messaging.sendSuccess(commandSender, Component.translatable("success_road_validate",
                    Argument.component("road", Component.text(road.getName()))));
        }
    }

    @Subcommand("merge")
    @Description("Merge 2 roads")
    @CommandCompletion("@road_player_town_is_in @road @empty")
    @Syntax("<road> <road>")
    public static void onMerge(CommandSender commandSender, String roadName1, String roadName2) {
        if (commandSender instanceof Player player) {
            Road road1 = TownyUtil.getRoadFromNameOrNull(commandSender, roadName1);
            if (road1 == null)
                return;
            Road road2 = TownyUtil.getRoadFromNameOrNull(commandSender, roadName2);
            if (road2 == null)
                return;

            if (road1.equals(road2)) {
                Messaging.sendError(commandSender, "err_same_road");
                return;
            }

            if (!TownyUniverse.getInstance().getPermissionSource().testPermission(commandSender,
                    TownyRoadsPermissionNodes.TOWNYROADS_MERGE.getNode())) {
                Messaging.sendError(commandSender, "err_no_permission_to_merge_road");
                return;
            }

            if (!road1.isAPlayerOfTheRoad(player) || !road2.isAPlayerOfTheRoad(player)) {
                Messaging.sendError(commandSender, "err_not_in_both_roads");
                return;
            }

            Optional<Component> error = road1.merge(road2);
            if (error.isPresent()) {
                Messaging.sendError(commandSender,
                        Component.translatable("err_merge_failed").appendSpace().append(error.get()));
            } else {
                Messaging.sendSuccess(commandSender,
                        Component.translatable("success_road_merge",
                                Argument.component("road1", Component.text(road1.getName())),
                                Argument.component("road2", Component.text(road2.getName()))));
            }
        } else {
            notAPlayer(commandSender);
        }
    }

    public static void notAPlayer(CommandSender commandSender) {
        Messaging.sendError(commandSender, "err_command_sender_not_a_player");
    }

    public static void notInTown(CommandSender commandSender) {
        Messaging.sendError(commandSender, "err_player_not_in_town");
    }

    public static void notInRoad(CommandSender commandSender) {
        Messaging.sendError(commandSender, "err_player_not_in_road");
    }

    public static void roadNotFound(CommandSender commandSender, String roadName) {
        Messaging.sendError(commandSender,
                Component.translatable("err_road_not_found", Argument.component("road", Component.text(roadName))));
    }
}
