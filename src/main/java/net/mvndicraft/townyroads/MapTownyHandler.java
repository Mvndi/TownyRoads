package net.mvndicraft.townyroads;

import java.util.Collection;
import me.silverwolfg11.maptowny.MapTownyPlugin;
import org.bukkit.plugin.Plugin;

public class MapTownyHandler {
    public MapTownyHandler() {}

    public boolean init(Plugin mapTowny) {
        if (mapTowny instanceof MapTownyPlugin mapTownyPlugin) {
            Collection<Road> roads = TownyRoadsPlugin.getInstance().getRoadManager().getRoads();
            // TODO

            return true;
        }
        return false;
    }
}
