package net.mvndicraft.townyroads;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.silverwolfg11.maptowny.MapTownyPlugin;
import me.silverwolfg11.maptowny.objects.LayerOptions;
import me.silverwolfg11.maptowny.objects.MarkerOptions;
import me.silverwolfg11.maptowny.objects.Point2D;
import me.silverwolfg11.maptowny.objects.Polygon;
import me.silverwolfg11.maptowny.platform.MapLayer;
import me.silverwolfg11.maptowny.platform.MapPlatform;
import me.silverwolfg11.maptowny.platform.MapWorld;
import net.mvndicraft.townyroads.settings.TownyRoadsSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class MapTownyHandler {
    private static final String ROAD_LAYER_KEY = "townyroads_roads";
    private static final String ROAD_MARKER_PREFIX = "townyroads_road_";
    private static final int CHUNK_SIZE = 16;

    private MapPlatform mapPlatform;

    public boolean init(Plugin mapTowny) {
        if (mapTowny instanceof MapTownyPlugin mapTownyPlugin) {
            this.mapPlatform = mapTownyPlugin.getPlatform();

            // Refresh the roads every 10 seconds
            TownyRoadsPlugin.debug(
                    () -> "Refreshing roads every " + TownyRoadsSettings.getDynmapRefreshFrequency() + " seconds");
            Bukkit.getAsyncScheduler().runAtFixedRate(mapTowny, t -> this.refreshRoads(), 0L,
                    TownyRoadsSettings.getDynmapRefreshFrequency(), TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    public void refreshRoads() {
        if (this.mapPlatform == null) {
            TownyRoadsPlugin.debug("MapTowny not initialized");
            return;
        }

        Collection<Road> roads = TownyRoadsPlugin.getInstance().getRoadManager().getRoads();
        for (World world : Bukkit.getWorlds()) {
            if (!this.mapPlatform.isWorldEnabled(world)) {
                TownyRoadsPlugin.debug("World " + world.getName() + " is not enabled");
                continue;
            }

            MapWorld mapWorld = this.mapPlatform.getWorld(world);
            if (mapWorld == null) {
                TownyRoadsPlugin.debug("World " + world.getName() + " is not loaded");
                continue;
            }

            if (mapWorld.hasLayer(ROAD_LAYER_KEY)) {
                mapWorld.unregisterLayer(ROAD_LAYER_KEY);
            }

            MapLayer layer = mapWorld.registerLayer(ROAD_LAYER_KEY,
                    new LayerOptions("Towny Roads", true, false, 90, 90));

            Collection<Road> roadsFiltered = roads.stream().filter(Road::isValid).filter(road -> road
                    .getChunksCoordsView().stream().anyMatch(chunk -> chunk.worldUuid().equals(world.getUID())))
                    .toList();
            roadsFiltered.forEach(road -> layer.addMultiPolyMarker(ROAD_MARKER_PREFIX + road.getId(),
                    road.getChunksCoordsView().stream().filter(chunk -> chunk.worldUuid().equals(world.getUID()))
                            .map(this::chunkToPolygon).toList(),
                    roadMarkerOptions(road)));

            TownyRoadsPlugin.debug(
                    () -> "Display " + roadsFiltered.size() + " roads into the dynmap for world " + world.getName());
        }
    }

    private Polygon chunkToPolygon(ChunkCoord chunkCoord) {
        double x = chunkCoord.x() * (double) CHUNK_SIZE;
        double z = chunkCoord.z() * (double) CHUNK_SIZE;
        return new Polygon(List.of(Point2D.of(x, z), Point2D.of(x + CHUNK_SIZE, z),
                Point2D.of(x + CHUNK_SIZE, z + CHUNK_SIZE), Point2D.of(x, z + CHUNK_SIZE)), List.of());
    }

    private MarkerOptions roadMarkerOptions(Road road) {
        Color color = new Color(Integer.parseInt(TownyRoadsSettings.getDynmapRoadColor(), 16));
        return MarkerOptions.builder().name(road.getName()).stroke(true).strokeColor(color).strokeWeight(3)
                .strokeOpacity(1.0).fill(true).fillColor(color).fillOpacity(0.22)
                .fillRule(MarkerOptions.FillRule.EVENODD).clickTooltip(road.getDescription())
                .hoverTooltip(road.getDescription()).build();
    }
}
