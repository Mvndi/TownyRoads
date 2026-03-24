package main.java.net.mvndicraft.townyroads;

import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Location;

public record ChunkCoord(UUID worldUuid, int x, int z) {

    public static ChunkCoord from(Chunk chunk) {
        return new ChunkCoord(
            chunk.getWorld().getUID(),
            chunk.getX(),
            chunk.getZ()
        );
    }

    public static ChunkCoord from(Location loc) {
        return new ChunkCoord(
            loc.getWorld().getUID(),
            loc.getBlockX() >> 4,
            loc.getBlockZ() >> 4
        );
    }
}
