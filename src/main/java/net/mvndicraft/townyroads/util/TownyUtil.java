package net.mvndicraft.townyroads.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.command.CommandSender;

public class TownyUtil {
    private TownyUtil() {}
    public static Town getTownFromNameOrNull(CommandSender commandSender, String townName) {
        Town town = TownyAPI.getInstance().getTown(townName);
        if (town == null || town.isRuined()) {
            commandSender.sendMessage("Town " + townName + " does not exist or is ruined.");
        }
        return town;
    }

    public static Road getRoadFromNameOrNull(CommandSender commandSender, String roadName) {
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadByName(roadName);
        if (road == null) {
            commandSender.sendMessage("Road " + roadName + " does not exist.");
        }
        return road;
    }
}
