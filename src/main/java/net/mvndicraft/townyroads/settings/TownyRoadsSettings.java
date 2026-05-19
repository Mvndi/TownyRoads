package net.mvndicraft.townyroads.settings;

public class TownyRoadsSettings {
    private TownyRoadsSettings() {}

    public static int getMaxclaims() {
        return Settings.getInt(ConfigNodes.ROADS_RESTRICTIONS_MAX_CLAIMS);
    }

    public static double getMaxClaimsMultiplier() {
        return Settings.getDouble(ConfigNodes.ROADS_RESTRICTIONS_MAX_CLAIMS_MULTIPLIER);
    }

}
