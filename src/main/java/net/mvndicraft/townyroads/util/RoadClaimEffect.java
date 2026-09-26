package net.mvndicraft.townyroads.util;

import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.util.BukkitParticle;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class RoadClaimEffect {

    private RoadClaimEffect() {}

    public static void playClaimEffect(Player player, WorldCoord worldCoord, Road road) {
        World world = worldCoord.getBukkitWorld();
        if (world == null) return;

        Particle particle = BukkitParticle.getBorderParticle();
        Color color = road.isValid()
            ? Color.fromRGB(0xFF, 0xA5, 0x00)
            : Color.fromRGB(0x8B, 0x45, 0x13);
        Particle.DustOptions dust = new Particle.DustOptions(color, 3.0f);

        int chunkX = worldCoord.getX();
        int chunkZ = worldCoord.getZ();
        int blockX = chunkX << 4;
        int blockZ = chunkZ << 4;
        int y = player.getLocation().getBlockY();

        TownyRoadsPlugin.debug("RoadClaimEffect: spawning at blockX=" + blockX
            + " blockZ=" + blockZ + " y=" + y + " color=" + color);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 60 || !player.isOnline()) {
                    cancel();
                    return;
                }
                for (int i = 0; i <= 16; i += 2) {
                    Location edge1 = new Location(world, blockX + i, y, blockZ);
                    Location edge2 = new Location(world, blockX + i, y, blockZ + 16);
                    Location edge3 = new Location(world, blockX, y, blockZ + i);
                    Location edge4 = new Location(world, blockX + 16, y, blockZ + i);
                    world.spawnParticle(particle, edge1, 3, 0.1, 0.3, 0.1, 0, dust);
                    world.spawnParticle(particle, edge2, 3, 0.1, 0.3, 0.1, 0, dust);
                    world.spawnParticle(particle, edge3, 3, 0.1, 0.3, 0.1, 0, dust);
                    world.spawnParticle(particle, edge4, 3, 0.1, 0.3, 0.1, 0, dust);
                }
                ticks += 5;
            }
        }.runTaskTimer(TownyRoadsPlugin.getInstance(), 0L, 5L);
    }
}
