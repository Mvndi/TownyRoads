package net.mvndicraft.townyroads.permissions;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.permissions.TownyPermissionSource;
import java.util.List;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.settings.TownyRoadsSettings;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RoadPermissionHandler {
    private RoadPermissionHandler() {}
    public static boolean canBuild(Player player, Road road) {
        return hasPermission(player, road, TownyRoadsPermissionNodes.TOWNYROADS_CLAIMED_OWNROAD_BLOCK_BUILD);
    }

    public static boolean canDestroy(Player player, Road road) {
        return hasPermission(player, road, TownyRoadsPermissionNodes.TOWNYROADS_CLAIMED_OWNROAD_BLOCK_DESTROY);
    }

    public static boolean canItemUse(Player player, Road road) {
        return hasPermission(player, road, TownyRoadsPermissionNodes.TOWNYROADS_CLAIMED_OWNROAD_BLOCK_ITEM_USE);
    }

    private static boolean hasPermission(Player player, Road road, TownyRoadsPermissionNodes permissionNode) {
        // everyone can build on invalid roads
        if (!road.isValid()) {
            return true;
        }

        if (player == null) {
            TownyRoadsPlugin.getInstance().getLogger().warning("Player is null when testing hasPermission!");
            return false;
        }
        TownyPermissionSource permissionSource = TownyUniverse.getInstance().getPermissionSource();
        // admin can build everywhere
        if (permissionSource.isTownyAdmin(player)) {
            return true;
        }
        Location location = player.getLocation(); // players can always mine under roads if they are low enough
        if (location != null && location.getY() < TownyRoadsSettings.getMinY()) {
            return true;
        }
        if (road.isAPlayerOfTheRoad(player)) {
            // true if the player has the permission
            return permissionSource.testPermission(player, permissionNode.getNode());
        }
        // player not on the road have a cooldown
        return TownyRoadsPlugin.getInstance().getPlayerCooldownManager().canActThenIncreaseCooldown(player);
    }

    public static boolean canAcceptRoadForTown(Player player, Town town) {
        Town playerTown = TownyAPI.getInstance().getTown(player);
        if (playerTown == null) {
            return false;
        }
        // have mayor permission
        if (town.equals(playerTown) && TownyUniverse.getInstance().getPermissionSource().testPermission(player,
                TownyRoadsPermissionNodes.TOWNYROADS_ACCEPT.getNode())) {
            return true;
        }
        Nation playerNation = playerTown.getNationOrNull();
        Nation townNation = town.getNationOrNull();
        // have king permission & is king/coking of the same nation.
        return playerNation != null && townNation != null && playerNation.equals(townNation)
                && TownyRoadsSettings.getRoadsPermissionNationLeadersCanAccept()
                && TownyUniverse.getInstance().getPermissionSource().testPermission(player,
                        TownyRoadsPermissionNodes.TOWNYROADS_NATION_ACCEPT.getNode());
    }

    public static List<Town> getAcceptableTowns(Player player, Road road) {
        return road.getToConfirmTownsView().stream().filter(town -> canAcceptRoadForTown(player, town)).toList();
    }

    public static boolean canAcceptTheRoad(Player player, Road road) {
        for (Town townInRoad : road.getToConfirmTownsView()) {
            if (canAcceptRoadForTown(player, townInRoad)) {
                return true;
            }
        }
        return false;
    }
}
