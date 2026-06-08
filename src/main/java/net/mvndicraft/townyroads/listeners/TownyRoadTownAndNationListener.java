package net.mvndicraft.townyroads.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NationUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.event.TownUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import java.util.Collection;
import java.util.Set;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.settings.TownyRoadsSettings;
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onTownUpkeepCalculationEvent(TownUpkeepCalculationEvent event) {
        if (!TownyRoadsSettings.getUpkeepEnabled()) {
            return;
        }
        Town town = event.getTown();
        Collection<Town> townsThatShouldBeConnected;
        if (town.hasNation()) {
            try {
                if (town.isCapital()) {
                    townsThatShouldBeConnected = town.getNation().getTowns().stream().filter(t -> !t.equals(town))
                            .toList();
                } else {
                    townsThatShouldBeConnected = Set.of(town.getNation().getCapital());
                }
            } catch (NotRegisteredException e) {
                TownyRoadsPlugin.warning("onTownUpkeepCalculationEvent " + e.getMessage());
                townsThatShouldBeConnected = Set.of();
            }
            double connectionScore = TownyRoadsPlugin.getInstance().getRoadManager().countConnected(town,
                    townsThatShouldBeConnected) / ((double) townsThatShouldBeConnected.size());
            double reduction = 1.0D
                    - (connectionScore * TownyRoadsSettings.getTownUpkeepReductionForNationConnectedTowns());
            double newUpkeed = event.getUpkeep() * reduction;
            TownyRoadsPlugin.debug("Town Upkeep was " + event.getUpkeep() + " reduced to " + newUpkeed
                    + " with a reduction of " + reduction + " and a connection score of " + connectionScore);
            event.setUpkeep(newUpkeed);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onTownUpkeepCalculationEvent(NationUpkeepCalculationEvent event) {
        if (!TownyRoadsSettings.getUpkeepEnabled()) {
            return;
        }
        Town town = event.getNation().getCapital();
        Collection<Town> townsThatShouldBeConnected = event.getNation().getTowns().stream().filter(t -> !t.equals(town))
                .toList();
        double connectionScore = TownyRoadsPlugin.getInstance().getRoadManager().countConnected(town,
                townsThatShouldBeConnected) / ((double) townsThatShouldBeConnected.size());
        double reduction = 1.0D
                - (connectionScore * TownyRoadsSettings.getNationUpkeepReductionForNationConnectedTowns());
        double newUpkeed = event.getUpkeep() * reduction;
        TownyRoadsPlugin.debug("Nation Upkeep was " + event.getUpkeep() + " reduced to " + newUpkeed
                + " with a reduction of " + reduction + " and a connection score of " + connectionScore);
        event.setUpkeep(newUpkeed);
    }
}
