package net.mvndicraft.townyroads.commands;

import co.aikar.commands.PaperCommandManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.mvndicraft.townyroads.ChunkCoord;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.permissions.TownyRoadsPermissionNodes;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownyRoadCommandCompleter {
    private TownyRoadCommandCompleter() {}
    public static void registerCommandCompletion(PaperCommandManager manager) {
        manager.getCommandCompletions().registerAsyncCompletion("reachable_road_towns", c -> {
            // Player player = c.getContextValue(Player.class, 0);
            return TownyAPI.getInstance().getTowns().stream().filter(t -> !t.isRuined()).map(t -> t.getName()).toList();
        });

        manager.getCommandCompletions().registerAsyncCompletion("reachable_road_towns_create", c -> {
            CommandSender commandSender = c.getContextValue(CommandSender.class, 0);
            if (commandSender instanceof Player player && playerCanCreate(player)) {
                Town playerTown = TownyAPI.getInstance().getTown(player);
                if (playerTown != null) {
                    return TownyAPI.getInstance().getTowns().stream().filter(t -> !t.isRuined())
                            .filter(t -> !playerTown.equals(t)).map(t -> t.getName()).toList();
                }
            }
            return List.of();
        });

        manager.getCommandCompletions().registerAsyncCompletion("next_by_roads_then_empty", c -> {
            CommandSender commandSender = c.getContextValue(CommandSender.class, 0);
            return nextByRoadsThenEmpty(commandSender, true);
        });

        manager.getCommandCompletions().registerAsyncCompletion("next_by_roads_then_empty_with_player_town", c -> {
            CommandSender commandSender = c.getContextValue(CommandSender.class, 0);
            return nextByRoadsThenEmpty(commandSender, false);
        });

        manager.getCommandCompletions().registerAsyncCompletion("road", c -> {
            // Player player = c.getContextValue(Player.class, 0);
            return TownyRoadsPlugin.getInstance().getRoadManager().getRoads().stream().map(Road::getName).toList();
        });

        manager.getCommandCompletions().registerAsyncCompletion("acceptable_road", c -> {
            CommandSender commandSender = c.getContextValue(CommandSender.class, 0);
            if (commandSender instanceof Player player && TownyUniverse.getInstance().getPermissionSource()
                    .testPermission(player, TownyRoadsPermissionNodes.TOWNYROADS_ACCEPT.getNode())) {
                Town playerTown = TownyAPI.getInstance().getTown(player);
                if (playerTown != null) {
                    return TownyRoadsPlugin.getInstance().getRoadManager().getAcceptableRoadByTown(playerTown).stream()
                            .map(Road::getName).toList();
                }
            }
            return List.of();
        });

        manager.getCommandCompletions().registerAsyncCompletion("any_acceptable_road", c -> {
            return TownyRoadsPlugin.getInstance().getRoadManager().getAcceptableRoad().stream().map(Road::getName)
                    .toList();
        });

        manager.getCommandCompletions().registerAsyncCompletion("town_in_road", c -> {
            String roadName = c.getContextValue(String.class, 1);
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadByName(roadName);
            if (road == null) {
                return List.of();
            } else {
                return road.getTownsView().stream().map(Town::getName).toList();
            }
        });

        manager.getCommandCompletions().registerAsyncCompletion("road_player_town_is_in", c -> {
            CommandSender commandSender = c.getContextValue(CommandSender.class, 0);
            if (commandSender instanceof Player player) {
                Town playerTown = TownyAPI.getInstance().getTown(player);
                if (playerTown != null) {
                    return TownyRoadsPlugin.getInstance().getRoadManager().getRoadsByTown(playerTown).stream()
                            .map(Road::getName).toList();
                }
            }
            return List.of();
        });

        manager.getCommandCompletions().registerCompletion("force", c -> List.of("true", "false"));
    }

    private static List<String> nextByRoadsThenEmpty(CommandSender commandSender, boolean adminCommand) {
        if (commandSender instanceof Player player && playerCanClaim(player)) {
            List<Road> result = new LinkedList<>();
            ChunkCoord chunkCoord = ChunkCoord.from(player.getLocation());
            result.addAll(nextByRoads(chunkCoord));
            result.addAll(emptyRoads());
            return result.stream().filter(
                    r -> adminCommand || (r.canClaimMore() && r.isAPlayerOfTheRoad(player) && playerCanClaim(player)))
                    .map(Road::getName).toList();
        } else {
            return List.of();
        }
    }

    private static List<Road> nextByRoads(ChunkCoord chunkCoord) {
        return chunkCoord.getNearby(1).stream().map(TownyRoadsPlugin.getInstance().getRoadManager()::getRoadAt)
                .filter(Objects::nonNull).toList();
    }

    private static List<Road> emptyRoads() {
        return TownyRoadsPlugin.getInstance().getRoadManager().getRoads().stream()
                .filter(r -> r.getChunksCoordsView().isEmpty()).toList();
    }

    private static boolean playerCanClaim(Player player) {
        return TownyUniverse.getInstance().getPermissionSource().testPermission(player,
                TownyRoadsPermissionNodes.TOWNYROADS_CLAIM.getNode());
    }
    private static boolean playerCanCreate(Player player) {
        return TownyUniverse.getInstance().getPermissionSource().testPermission(player,
                TownyRoadsPermissionNodes.TOWNYROADS_CREATE.getNode());
    }
}
