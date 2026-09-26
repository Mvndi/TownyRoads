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

        String symbol;
        NamedTextColor color;
        String status;

        if (road.isBlocked()) {
            symbol = TownyRoadsPlugin.BLOCKED_SYMBOL;
            color = NamedTextColor.DARK_PURPLE;
            status = "blocked";
        } else if (road.isValid()) {
            symbol = TownyRoadsPlugin.VALID_SYMBOL;
            color = NamedTextColor.GOLD;
            status = "validated";
        } else {
            symbol = TownyRoadsPlugin.INVALID_SYMBOL;
            color = NamedTextColor.DARK_RED;
            status = "un-validated";
        }

        event.setMapSymbol(symbol);
        event.setHoverText(
            Component.text(road.getName(), color)
                .append(Component.text(" (" + status + ")", NamedTextColor.GRAY))
        );
    }
}
