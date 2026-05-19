package net.mvndicraft.townyroads;

import co.aikar.commands.PaperCommandManager;
import net.mvndicraft.townyroads.commands.TownyRoadCommandCompleter;
import net.mvndicraft.townyroads.commands.TownyRoadsAdminCommand;
import net.mvndicraft.townyroads.commands.TownyRoadsCommand;
import net.mvndicraft.townyroads.listeners.TownyRoadPlayersListener;
import net.mvndicraft.townyroads.settings.Settings;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyRoadsPlugin extends JavaPlugin {
    public static final String ADMIN_PERMISSION = "townyroads.admin";
    private RoadManager roadManager;


    @Override
    public void onEnable() {
        roadManager = new RoadManager();

        Settings.loadConfigAndLang();

        getServer().getPluginManager().registerEvents(new TownyRoadPlayersListener(), this);

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new TownyRoadsCommand());
        manager.registerCommand(new TownyRoadsAdminCommand());

        TownyRoadCommandCompleter.registerCommandCompletion(manager);
    }

    public static TownyRoadsPlugin getInstance() {
        return getPlugin(TownyRoadsPlugin.class);
    }
    public RoadManager getRoadManager() {
        return roadManager;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Settings.loadConfigAndLang();
    }
}
