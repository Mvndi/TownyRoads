package net.mvndicraft.townyroads.permissions;

import net.mvndicraft.townyroads.Road;
import org.bukkit.entity.Player;

public class RoadPermissionHandler {
    public boolean canBuild(Player player, Road road) {
        return true;
    }

    public boolean canBreak(Player player, Road road) {
        return true;
    }

    public boolean canUse(Player player, Road road) {
        return true;
    }
}
