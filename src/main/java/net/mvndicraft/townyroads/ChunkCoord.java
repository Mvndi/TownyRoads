package net.mvndicraft.townyroads;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Location;

public record ChunkCoord(UUID worldUuid, int x, int z) implements Comparable<ChunkCoord> {

    public static ChunkCoord from(Chunk chunk) { return new ChunkCoord(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ()); }

    public static ChunkCoord from(Location loc) {
        return new ChunkCoord(loc.getWorld().getUID(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public List<ChunkCoord> getNearby(int radius) {
        if (radius < 1) {
            return new LinkedList<>();
        }
        List<ChunkCoord> chunks = new LinkedList<>();
        for (int x = -radius; x <= radius; x++) {
            int remaining = radius - Math.abs(x);
            for (int z = -remaining; z <= remaining; z++) {
                if (!(x == 0 && z == 0)) {
                    chunks.add(new ChunkCoord(worldUuid, this.x + x, this.z + z));
                }
            }
        }
        return chunks;
    }

    public static ChunkCoord fromString(String string) {
        String[] split = string.split(";");
        return new ChunkCoord(UUID.fromString(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }
    @Override
    public String toString() { return worldUuid + ";" + x + ";" + z; }

    public Location toLocation() {
        return new Location(TownyRoadsPlugin.getInstance().getServer().getWorld(worldUuid), x << 4, 0, z << 4);
    }

    @Override
    public int compareTo(ChunkCoord o) {
        if(this.worldUuid().hashCode() != o.worldUuid().hashCode()) {
            return this.worldUuid.hashCode() - o.worldUuid.hashCode();
        }
        if(this.x != o.x) {
            return this.x - o.x;
        }
        if(this.z != o.z) {
            return this.z - o.z;
        }
        return 0;
    }
}
