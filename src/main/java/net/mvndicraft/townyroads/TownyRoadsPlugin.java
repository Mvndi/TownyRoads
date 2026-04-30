package net.mvndicraft.townyroads;

import co.aikar.commands.PaperCommandManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.mvndicraft.townyroads.commands.TownyRoadsAdminCommand;
import net.mvndicraft.townyroads.commands.TownyRoadsCommand;
import net.mvndicraft.townyroads.listeners.TownyRoadPlayersListener;
import net.mvndicraft.townyroads.permissions.TownyRoadsPermissionNodes;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyRoadsPlugin extends JavaPlugin {
    public static final String ADMIN_PERMISSION = "townyroads.admin";
    private RoadManager roadManager;


    @Override
    public void onEnable() {
        roadManager = new RoadManager();

        getServer().getPluginManager().registerEvents(new TownyRoadPlayersListener(), this);

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new TownyRoadsCommand());
        manager.registerCommand(new TownyRoadsAdminCommand());
        manager.getCommandCompletions().registerAsyncCompletion("reachable_road_towns", c -> {
            // Player player = c.getContextValue(Player.class, 0);
            return TownyAPI.getInstance().getTowns().stream().filter(t -> !t.isRuined()).map(t -> t.getName()).toList();
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
            return roadManager.getRoads().stream().map(Road::getName).toList();
        });
        manager.getCommandCompletions().registerAsyncCompletion("town_in_road", c -> {
            String roadName = c.getContextValue(String.class, 1);
            Road road = roadManager.getRoadByName(roadName);
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
                    return roadManager.getRoadsByTown(playerTown).stream().map(Road::getName).toList();
                }
            }
            return List.of();
        });
        manager.getCommandCompletions().registerCompletion("force", c -> List.of("true", "false"));
    }

    public static TownyRoadsPlugin getInstance() {
        return getPlugin(TownyRoadsPlugin.class);
    }
    public RoadManager getRoadManager() {
        return roadManager;
    }


    private List<String> nextByRoadsThenEmpty(CommandSender commandSender, boolean adminCommand) {
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

    private List<Road> nextByRoads(ChunkCoord chunkCoord) {
        return chunkCoord.getNearby(1).stream().map(roadManager::getRoadAt).filter(Objects::nonNull).toList();
    }

    private List<Road> emptyRoads() {
        return roadManager.getRoads().stream().filter(r -> r.getChunksCoordsView().isEmpty()).toList();
    }

    private boolean playerCanClaim(Player player) {
        return player.isOp() || player.hasPermission(TownyRoadsPermissionNodes.TOWNY_ROADS_CLAIM.getNode());
    }
}
