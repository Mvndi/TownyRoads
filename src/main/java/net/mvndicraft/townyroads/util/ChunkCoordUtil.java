package net.mvndicraft.townyroads.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.mvndicraft.townyroads.ChunkCoord;

public class ChunkCoordUtil {
    public static boolean areAllConnected(Collection<ChunkCoord> chunks) {
        if (chunks.isEmpty())
            return true;

        UUID world = chunks.iterator().next().worldUuid();
        boolean sameWorld = chunks.stream().allMatch(c -> c.worldUuid().equals(world));
        if (!sameWorld) {
            return false;
        }

        Set<ChunkCoord> visited = new HashSet<>();
        Queue<ChunkCoord> queue = new ArrayDeque<>();

        ChunkCoord start = chunks.iterator().next();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            ChunkCoord current = queue.poll();

            for (ChunkCoord neighbor : current.getNearby(1)) {
                if (chunks.contains(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited.size() == chunks.size();
    }
}
