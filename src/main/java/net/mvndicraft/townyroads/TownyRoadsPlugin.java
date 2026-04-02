package net.mvndicraft.townyroads;

import co.aikar.commands.PaperCommandManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import java.util.LinkedList;
import java.util.List;
import net.mvndicraft.townyroads.commands.TownyRoadsAdminCommand;
import net.mvndicraft.townyroads.commands.TownyRoadsCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyRoadsPlugin extends JavaPlugin {
    public static final String ADMIN_PERMISSION = "townyroads.admin";
    private RoadManager roadManager;


    @Override
    public void onEnable() {
        roadManager = new RoadManager();

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new TownyRoadsCommand());
        manager.registerCommand(new TownyRoadsAdminCommand());
        manager.getCommandCompletions().registerAsyncCompletion("reachable_road_towns", c -> {
            // Player player = c.getContextValue(Player.class, 0);
            return TownyAPI.getInstance().getTowns().stream().filter(t -> !t.isRuined()).map(t -> t.getName()).toList();
        });
        manager.getCommandCompletions().registerAsyncCompletion("next_by_road", c -> {
            CommandSender commandSender = c.getContextValue(CommandSender.class, 0);
            List<String> result = new LinkedList<>();
            if (commandSender instanceof Player player) {
                ChunkCoord chunkCoord = ChunkCoord.from(player.getLocation());
                for (ChunkCoord chunk : chunkCoord.getNearby(1)) {
                    Road road = roadManager.getRoadAt(chunk);
                    if (road != null) {
                        result.add(road.getName());
                    }
                }
            }
            return result;
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

    public static TownyRoadsPlugin getInstance() { return getPlugin(TownyRoadsPlugin.class); }
    public RoadManager getRoadManager() { return roadManager; }
}
