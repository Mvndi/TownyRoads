package net.mvndicraft.townyroads.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.command.CommandSender;

public class TownyUtil {
    private TownyUtil() {}
    public static Town getTownFromNameOrNull(CommandSender commandSender, String townName) {
        Town town = TownyAPI.getInstance().getTown(townName);
        if (town == null) {
            Messaging.sendError(commandSender, Component.translatable("err_town_does_not_exist",
                    Argument.component("town", Component.text(townName))));
        } else if (town.isRuined()) {
            Messaging.sendError(commandSender,
                    Component.translatable("err_town_is_ruined", Argument.component("town", Component.text(townName))));
            return null; // we don't want to use a ruined town
        }
        return town;
    }

    public static Road getRoadFromNameOrNull(CommandSender commandSender, String roadName) {
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadByName(roadName);
        if (road == null) {
            Messaging.sendError(commandSender, Component.translatable("err_road_does_not_exist",
                    Argument.component("road", Component.text(roadName))));
        }
        return road;
    }
}
