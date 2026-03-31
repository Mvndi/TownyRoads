package net.mvndicraft.townyroads;

import co.aikar.commands.PaperCommandManager;
import com.palmergames.bukkit.towny.TownyAPI;
import java.util.LinkedList;
import java.util.List;
import net.mvndicraft.townyroads.commands.TownyRoadsAdminCommand;
import net.mvndicraft.townyroads.commands.TownyRoadsCommand;
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
            Player player = c.getContextValue(Player.class, 0);
            List<String> result = new LinkedList<>();
            ChunkCoord chunkCoord = ChunkCoord.from(player.getLocation());
            for (ChunkCoord chunk : chunkCoord.getNearby(1)) {
                Road road = roadManager.getRoadAt(chunk);
                if (road != null) {
                    result.add(road.getName());
                }
            }
            return result;
        });
    }

    public static TownyRoadsPlugin getInstance() { return getPlugin(TownyRoadsPlugin.class); }
    public RoadManager getRoadManager() { return roadManager; }
}
