package net.mvndicraft.townyroads.permissions;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.permissions.TownyPermissionSource;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
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
        if (road.isAPlayerOfTheRoad(player)) {
            // true if the player has the permission
            return permissionSource.testPermission(player, permissionNode.getNode());
        }
        // player not on the road have a cooldown
        return TownyRoadsPlugin.getInstance().getPlayerCooldownManager().canActThenIncreaseCooldown(player);
    }
}
