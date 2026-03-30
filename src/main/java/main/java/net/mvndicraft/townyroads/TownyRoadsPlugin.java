package main.java.net.mvndicraft.townyroads;

import co.aikar.commands.PaperCommandManager;
import com.palmergames.bukkit.towny.TownyAPI;
import main.java.net.mvndicraft.townyroads.commands.TownyRoadsAdminCommand;
import main.java.net.mvndicraft.townyroads.commands.TownyRoadsCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyRoadsPlugin extends JavaPlugin {
    public static final String ADMIN_PERMISSION = "townyroads.admin";


    @Override
    public void onEnable() {
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new TownyRoadsCommand());
        manager.registerCommand(new TownyRoadsAdminCommand());
        manager.getCommandCompletions().registerAsyncCompletion("reachable_road_towns", c -> {
            // Player player = c.getContextValue(Player.class, 0);
            return TownyAPI.getInstance().getTowns().stream().filter(t -> !t.isRuined()).map(t -> t.getName()).toList();
        });
    }

    public static TownyRoadsPlugin getInstance() { return getPlugin(TownyRoadsPlugin.class); }
}
