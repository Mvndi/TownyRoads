package net.mvndicraft.townyroads;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.mvndicraft.townyroads.settings.TownyRoadsSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerCooldownManager {
    private final Map<UUID, Integer> cooldowns;
    private int delay;
    private ScheduledTask task;

    public PlayerCooldownManager() {
        cooldowns = new ConcurrentHashMap<>();

        startDecreaseAllCooldownTask();
    }

    public void reload() {
        if (delay != TownyRoadsSettings.getCooldownReduceTimer()) {
            task.cancel();
            startDecreaseAllCooldownTask();
        }
    }
    private void startDecreaseAllCooldownTask() {
        delay = TownyRoadsSettings.getCooldownReduceTimer();
        task = Bukkit.getAsyncScheduler().runAtFixedRate(TownyRoadsPlugin.getInstance(), t -> {
            try {
                decreaseAllCooldown();
            } catch (Exception e) {
                TownyRoadsPlugin.getInstance().getLogger()
                        .severe("Failed to decrease all cooldowns! Canceling the task. Exception:" + e.getMessage());
                task.cancel();
            }
        }, 0L, delay, TimeUnit.SECONDS);
    }

    public boolean canActThenIncreaseCooldown(Player player) {
        TownyRoadsPlugin.getInstance().getLogger().info("Cooldowns before compute: " + cooldowns);
        return cooldowns.compute(player.getUniqueId(),
                (uuid, value) -> value == null ? 1 : value + 1) <= getMaxAction();
    }

    public int getMaxAction() {
        return TownyRoadsSettings.getCooldownMaxValue();
    }
    public int getReduceValue() {
        return TownyRoadsSettings.getCooldownReduceValue();
    }

    private void decreaseAllCooldown() {
        for (UUID uuid : cooldowns.keySet()) {
            // decrease up to 0.
            if (cooldowns.compute(uuid,
                    (key, value) -> value == null ? 0 : Math.max(value - getReduceValue(), 0)) == 0) {
                cooldowns.remove(uuid, 0); // remove only if we are still at 0, it might have been increase by
                                           // canActThenIncreaseCooldown since last line.
            }
        }
    }

    @Override
    public String toString() {
        return "maxAction" + getMaxAction() + ", cooldowns=" + cooldowns;
    }
}