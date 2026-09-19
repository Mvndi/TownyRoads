package net.mvndicraft.townyroads.listeners;

import com.palmergames.bukkit.towny.event.asciimap.WildernessMapEvent;
import com.palmergames.bukkit.towny.object.WorldCoord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mvndicraft.townyroads.ChunkCoord;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownyMapListener implements Listener {

    @EventHandler
    public void onWildernessMap(WildernessMapEvent event) {
        WorldCoord worldCoord = event.getWorldCoord();
        if (worldCoord.getBukkitWorld() == null) return;

        ChunkCoord chunkCoord = ChunkCoord.from(worldCoord);
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(chunkCoord);
        if (road == null) return;

        if (road.isValid()) {
            event.setMapSymbol("=");
        } else {
            event.setMapSymbol("~");
        }

        event.setHoverText(
            Component.text(road.getName(), road.isValid() ? NamedTextColor.GOLD : NamedTextColor.DARK_RED)
                .append(Component.text(" (" + (road.isValid() ? "validated" : "un-validated") + ")", NamedTextColor.GRAY))
        );
    }
}
