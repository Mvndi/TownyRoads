package net.mvndicraft.townyroads.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.object.Town;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TownyRoadTownAndNationListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onTownDeleted(DeleteTownEvent event) {
        Town town = TownyAPI.getInstance().getTown(event.getTownUUID());
        for (Road road : TownyRoadsPlugin.getInstance().getRoadManager().getRoadsByTown(town)) {
            road.removeTown(town);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onNewDay(NewDayEvent event) {
        TownyRoadsPlugin.getInstance().getRoadManager().revalidateAllValidatedRoads();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTownNameChange(RenameTownEvent event) {
        TownyRoadsPlugin.getInstance().getRoadManager().getRoadsByTown(event.getTown()).forEach(Road::updateRoadName);
    }
}
