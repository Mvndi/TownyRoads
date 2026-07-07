package net.mvndicraft.townyroads;

import com.palmergames.bukkit.towny.object.Coord;
import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import me.silverwolfg11.maptowny.MapTownyPlugin;
import me.silverwolfg11.maptowny.objects.LayerOptions;
import me.silverwolfg11.maptowny.objects.MarkerOptions;
import me.silverwolfg11.maptowny.objects.Polygon;
import me.silverwolfg11.maptowny.platform.MapLayer;
import me.silverwolfg11.maptowny.platform.MapPlatform;
import me.silverwolfg11.maptowny.platform.MapWorld;
import me.silverwolfg11.maptowny.util.TownyUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.mvndicraft.townyroads.settings.TownyRoadsSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class MapTownyHandler {
    private static final String ROAD_LAYER_KEY = "townyroads_roads";
    private static final String ROAD_MARKER_PREFIX = "townyroads_road_";

    private MapPlatform mapPlatform;
    private final TownyUtil townyUtil = new TownyUtil();

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
                    roadPolygons(road, world), roadMarkerOptions(road)));

            TownyRoadsPlugin.debug(
                    () -> "Display " + roadsFiltered.size() + " roads into the dynmap for world " + world.getName());
        }
    }

    private MarkerOptions roadMarkerOptions(Road road) {
        Color color = new Color(Integer.parseInt(road.isBlocked() ? TownyRoadsSettings.getDynmapBlockedRoadColor()
                : TownyRoadsSettings.getDynmapRoadColor(), 16));
        String description = plainText(road.getDescription());
        return MarkerOptions.builder().name(road.getName()).stroke(true).strokeColor(color)
                .strokeWeight(TownyRoadsSettings.getDynmapRoadStrokeWeight()).strokeOpacity(1.0).fill(true)
                .fillColor(color).fillOpacity(TownyRoadsSettings.getDynmapRoadFillOpacity())
                .fillRule(MarkerOptions.FillRule.EVENODD).clickTooltip(description).hoverTooltip(description).build();
    }

    private String plainText(Component component) {
        Component rendered = GlobalTranslator.render(component, Locale.ENGLISH);
        return PlainTextComponentSerializer.plainText().serialize(rendered);
    }

    private List<Polygon> roadPolygons(Road road, World world) {
        return townyUtil.coordsToPolys(
                road.getChunksCoordsView().stream().filter(chunk -> chunk.worldUuid().equals(world.getUID()))
                        .map(chunk -> new Coord(chunk.x(), chunk.z())).toList(),
                false, 16);
    }
}
