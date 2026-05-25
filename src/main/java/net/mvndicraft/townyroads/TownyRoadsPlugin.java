package net.mvndicraft.townyroads;

import co.aikar.commands.PaperCommandManager;
import java.util.function.Supplier;
import java.util.logging.Level;
import net.mvndicraft.townyroads.commands.TownyRoadCommandCompleter;
import net.mvndicraft.townyroads.commands.TownyRoadsAdminCommand;
import net.mvndicraft.townyroads.commands.TownyRoadsCommand;
import net.mvndicraft.townyroads.data.RoadStorage;
import net.mvndicraft.townyroads.data.RoadStorageFile;
import net.mvndicraft.townyroads.listeners.TownyRoadPlayersListener;
import net.mvndicraft.townyroads.settings.Settings;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyRoadsPlugin extends JavaPlugin {
    public static final String ADMIN_PERMISSION = "townyroads.admin";
    private RoadManager roadManager;
    private PlayerCooldownManager playerCooldownManager;
    private RoadStorage roadStorage;


    @Override
    public void onEnable() {
        Settings.loadConfigAndLang();
        roadStorage = new RoadStorageFile();
        roadManager = new RoadManager();
        playerCooldownManager = new PlayerCooldownManager();
        roadManager.addRoads(roadStorage.loadAll());

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
    public PlayerCooldownManager getPlayerCooldownManager() {
        return playerCooldownManager;
    }
    public RoadStorage getRoadStorage() {
        return roadStorage;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Settings.loadConfigAndLang();
        getPlayerCooldownManager().reload();
    }

    // Usual log with debug level
    // @formatter:off
    public static void log(Level level, String message) { getInstance().getLogger().log(level, message); }
    public static void log(Level level, Supplier<String> messageProvider) { getInstance().getLogger().log(level, messageProvider); }
    public static void log(Level level, String message, Throwable e) { getInstance().getLogger().log(level, message, e); }
    public static void debug(String message) {
        if (getInstance().getConfig().getBoolean("debug", false)) {
            log(Level.INFO, message);
        }
    }
    public static void debug(Supplier<String> messageProvider) {
        if (getInstance().getConfig().getBoolean("debug", false)) {
            log(Level.INFO, messageProvider);
        }
    }
    public static void info(String message) { log(Level.INFO, message); }
    public static void info(String message, Throwable e) { log(Level.INFO, message, e); }
    public static void warning(String message) { log(Level.WARNING, message); }
    public static void warning(String message, Throwable e) { log(Level.WARNING, message, e); }
    public static void error(String message) { log(Level.SEVERE, message); }
    public static void error(String message, Throwable e) { log(Level.SEVERE, message, e); }
    // @formatter:on
}
