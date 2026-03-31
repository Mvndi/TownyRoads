package net.mvndicraft.townyroads;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Location;

public record ChunkCoord(UUID worldUuid, int x, int z) {

    public static ChunkCoord from(Chunk chunk) { return new ChunkCoord(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ()); }

    public static ChunkCoord from(Location loc) {
        return new ChunkCoord(loc.getWorld().getUID(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public List<ChunkCoord> getNearby(int radius) {
        if (radius < 0) {
            return new LinkedList<>();
        }
        List<ChunkCoord> chunks = new LinkedList<>();
        for (int x = -radius; x < radius; x++) {
            int remaining = radius - Math.abs(x);
            for (int z = -remaining; z < remaining; z++) {
                chunks.add(new ChunkCoord(worldUuid, this.x + x, this.z + z));
            }
        }
        return chunks;
    }
}
