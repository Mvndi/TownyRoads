package net.mvndicraft.townyroads;

import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChunkCoordTest extends Assertions {
    @ParameterizedTest
    // @formatter:off
    @CsvSource({
        "-1, 0, 0, 1, 1, false",
        "0, 0, 0, 1, 1, false",
        "1, 0, 0, 1, 1, false",
        "2, 0, 0, 1, 1, true",
        "1, 0, 0, 0, 1, true",
        "1, 0, 0, 1, 0, true",
        "2, 0, 0, 0, 1, true",
        "2, 4, 6, 3, 5, true",
        "2, 4, 6, 3, 4, false",
        "1, 100, 100, 100, 100, false",
        "1, 100, 100, 101, 100, true",
        "1, 100, 100, 99, 100, true",
        "1, 100, 100, 100, 101, true",
        "1, 100, 100, 100, 99, true",
    })
    // @formatter:on
    void getNearby(int radius, int x, int z, int expectedX, int expectedZ, boolean expected) {
        assertEquals(expected,
                new ChunkCoord(UUID.randomUUID(), x, z).getNearby(radius).stream().anyMatch(c -> c.x() == expectedX && c.z() == expectedZ));
    }

    @ParameterizedTest
    // @formatter:off
    @CsvSource({
        "-1, 0, 0, 0",
        "0, 0, 0, 0",
        "1, 0, 0, 4",
        "1, 100, 100, 4",
        "2, 100, 100, 12",
    })
    // @formatter:on
    void getNearbyCount(int radius, int x, int z, int expected) {
        assertEquals(expected, new ChunkCoord(UUID.randomUUID(), x, z).getNearby(radius).size());
    }
}
