package net.mvndicraft.townyroads;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ClaimSwitchManager implements Listener {
    private final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public boolean toggle(UUID uuid) {
        if (activePlayers.add(uuid)) {
            return true;
        }
        activePlayers.remove(uuid);
        return false;
    }

    public boolean isEnabled(UUID uuid) {
        return activePlayers.contains(uuid);
    }

    public void disable(UUID uuid) {
        activePlayers.remove(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        disable(event.getPlayer().getUniqueId());
    }
}
