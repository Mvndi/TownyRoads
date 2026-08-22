package net.mvndicraft.townyroads.settings;

public class TownyRoadsSettings {
    private TownyRoadsSettings() {}

    public static boolean getDebug() {
        return Settings.getBoolean(ConfigNodes.DEBUG);
    }

    public static int getMaxclaims() {
        return Settings.getInt(ConfigNodes.ROADS_RESTRICTIONS_MAX_CLAIMS);
    }

    public static double getMaxClaimsMultiplier() {
        return Settings.getDouble(ConfigNodes.ROADS_RESTRICTIONS_MAX_CLAIMS_MULTIPLIER);
    }

    public static int getCooldownMaxValue() {
        return Settings.getInt(ConfigNodes.ROADS_COOLDOWN_MAX_VALUE);
    }

    public static int getCooldownReduceTimer() {
        return Settings.getInt(ConfigNodes.ROADS_COOLDOWN_REDUCE_TIMER);
    }

    public static int getCooldownReduceValue() {
        return Settings.getInt(ConfigNodes.ROADS_COOLDOWN_REDUCE_VALUE);
    }

    public static boolean getBonusBlockEnabled() {
        return Settings.getBoolean(ConfigNodes.ROADS_BONUS_BLOCK_ENABLED);
    }

    public static double getBonusBlockSameNationMultiplier() {
        return Settings.getDouble(ConfigNodes.ROADS_BONUS_BLOCK_SAME_NATION_MULTIPLIER);
    }

    public static double getBonusBlockAllyMultiplier() {
        return Settings.getDouble(ConfigNodes.ROADS_BONUS_BLOCK_ALLY_MULTIPLIER);
    }

    public static double getBonusBlockNeutralMultiplier() {
        return Settings.getDouble(ConfigNodes.ROADS_BONUS_BLOCK_NEUTRAL_MULTIPLIER);
    }

    public static double getBonusBlockEnemyMultiplier() {
        return Settings.getDouble(ConfigNodes.ROADS_BONUS_BLOCK_ENEMY_MULTIPLIER);
    }

    public static boolean getBonusBlockMultiplyByTownLevel() {
        return Settings.getBoolean(ConfigNodes.ROADS_BONUS_BLOCK_MULIPLY_BY_TOWN_LEVEL);
    }

    public static int getBonusBlockMaxValue() {
        return Settings.getInt(ConfigNodes.ROADS_BONUS_BLOCK_MAX_VALUE);
    }

    public static double getBonusBlockMaxMultiplyValue() {
        return Settings.getDouble(ConfigNodes.ROADS_BONUS_BLOCK_MAX_MULTIPLY_VALUE);
    }

    public static boolean isDynmapValidRoadDisplayed() {
        return Settings.getBoolean(ConfigNodes.ROADS_DYNMAP_VALID_ROAD_DISPLAYED);
    }
    public static boolean isDynmapBlockedRoadDisplayed() {
        return Settings.getBoolean(ConfigNodes.ROADS_DYNMAP_BLOCKED_ROAD_DISPLAYED);
    }
    public static boolean isDynmapInvalidRoadDisplayed() {
        return Settings.getBoolean(ConfigNodes.ROADS_DYNMAP_INVALID_ROAD_DISPLAYED);
    }

    public static String getDynmapValidRoadColor() {
        return Settings.getString(ConfigNodes.ROADS_DYNMAP_VALID_ROAD_COLOR);
    }
    public static String getDynmapBlockedRoadColor() {
        return Settings.getString(ConfigNodes.ROADS_DYNMAP_BLOCKED_ROAD_COLOR);
    }
    public static String getDynmapInvalidRoadColor() {
        return Settings.getString(ConfigNodes.ROADS_DYNMAP_INVALID_ROAD_COLOR);
    }
    public static double getDynmapValidRoadStrokeOpacity() {
        return Settings.getDouble(ConfigNodes.ROADS_DYNMAP_VALID_ROAD_STROKE_OPACITY);
    }
    public static double getDynmapBlockedRoadStrokeOpacity() {
        return Settings.getDouble(ConfigNodes.ROADS_DYNMAP_BLOCKED_ROAD_STROKE_OPACITY);
    }
    public static double getDynmapInvalidRoadStrokeOpacity() {
        return Settings.getDouble(ConfigNodes.ROADS_DYNMAP_INVALID_ROAD_STROKE_OPACITY);
    }
    public static double getDynmapValidRoadFillOpacity() {
        return Settings.getDouble(ConfigNodes.ROADS_DYNMAP_VALID_ROAD_FILL_OPACITY);
    }
    public static double getDynmapBlockedRoadFillOpacity() {
        return Settings.getDouble(ConfigNodes.ROADS_DYNMAP_BLOCKED_ROAD_FILL_OPACITY);
    }
    public static double getDynmapInvalidRoadFillOpacity() {
        return Settings.getDouble(ConfigNodes.ROADS_DYNMAP_INVALID_ROAD_FILL_OPACITY);
    }

    public static int getDynmapRefreshFrequency() {
        return Settings.getInt(ConfigNodes.ROADS_DYNMAP_REFRESH_FREQUENCY);
    }

    public static int getDynmapRoadStrokeWeight() {
        return Settings.getInt(ConfigNodes.ROADS_DYNMAP_ROAD_STROKE_WEIGHT);
    }

    public static boolean getUpkeepEnabled() {
        return Settings.getBoolean(ConfigNodes.ROADS_UPKEEP_ENABLED);
    }

    public static double getTownUpkeepReductionForNationConnectedTowns() {
        return Settings.getDouble(ConfigNodes.ROADS_TOWN_UPKEEP_REDUCTION_FOR_NATION_CONNECTED_TOWNS);
    }

    public static double getNationUpkeepReductionForNationConnectedTowns() {
        return Settings.getDouble(ConfigNodes.ROADS_NATION_UPKEEP_REDUCTION_FOR_NATION_CONNECTED_TOWNS);
    }

    public static String getRoadsPermissionAutoAccept() {
        return Settings.getString(ConfigNodes.ROADS_PERMISSION_AUTO_ACCEPT).toLowerCase();
    }

    public static boolean getRoadsPermissionNationLeadersCanAccept() {
        return Settings.getBoolean(ConfigNodes.ROADS_PERMISSION_NATION_LEADERS_CAN_ACCEPT);
    }

    public static int getMinY() {
        return Settings.getInt(ConfigNodes.ROADS_OTHER_MIN_Y);
    }
}
