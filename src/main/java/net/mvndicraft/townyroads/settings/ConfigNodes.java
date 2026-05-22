package net.mvndicraft.townyroads.settings;

public enum ConfigNodes {
    // @formatter:off
    VERSION(
            "version",
            "",
            "# This is the current version. Please do not edit."),
    ROADS("roads", "", ""),
    ROADS_RESTRICTIONS(
            "roads.restrictions",
            "",
            "",
            "############################################################",
            "# +------------------------------------------------------+ #",
            "# |                   Restrictions                       | #",
            "# +------------------------------------------------------+ #",
            "############################################################",
            ""),
    ROADS_RESTRICTIONS_MAX_CLAIMS(
            "roads.restrictions.max_claims",
            "1000",
            "",
            "# The maximum number of claims a road can have.",
            "# Disabled with value of -1"),
    ROADS_RESTRICTIONS_MAX_CLAIMS_MULTIPLIER(
            "roads.restrictions.max_claims_multiplier",
            "2.0",
            "",
            "# The maximum number of claims a road can have, multiplied by the distance between the 2 farthest towns.",
            "# Less than 1.0 will make the road impossible to claim.",
            "# Disabled with value of -1"),
    ROADS_COOLDOWN(
            "roads.cooldown",
            "",
            "",
            "############################################################",
            "# +------------------------------------------------------+ #",
            "# |                      Cooldown                        | #",
            "# +------------------------------------------------------+ #",
            "############################################################",
            "",
            "",
            "# How many time a player that does not have perms to build on the road will have to wait before building again in the road.",
            "# This settings are used to avoid major grief, but so that roads can still be griefed.",
            "# Else roads would be almost as protected as towns."),
    ROADS_COOLDOWN_MAX_VALUE(
            "roads.cooldown.max_value",
            "10",
            "",
            "# How many block can be builded on a road by a player that does not have perms to build on the road before it have to wait for the next cooldown.",
            "# Set this value at -1 to disable the ability of players without perms to build on the road."),
    ROADS_COOLDOWN_REDUCE_TIMER(
            "roads.cooldown.reduce_timer",
            "60",
            "",
            "# Every reduce_timer seconds the cooldown get reduced by reduce_value.",
            "# reduce_timer need to be at least 1, else the cooldown won't be reduced."),
    ROADS_COOLDOWN_REDUCE_VALUE(
            "roads.cooldown.reduce_value",
            "1",
            "",
            "# Every reduce_timer seconds the cooldown get reduced by reduce_value.",
            "# reduce_value need to be at least 1, else the cooldown won't be reduced.");
    // @formatter:on

    private final String root;
    private final String def;
    private final String[] comments;

    ConfigNodes(String root, String def, String... comments) {

        this.root = root;
        this.def = def;
        this.comments = comments;
    }

    /**
     * Retrieves the root for a config option
     *
     * @return The root for a config option
     */
    public String getRoot() {

        return root;
    }

    /**
     * Retrieves the default value for a config path
     *
     * @return The default value for a config path
     */
    public String getDefault() {

        return def;
    }

    /**
     * Retrieves the comment for a config path
     *
     * @return The comments for a config path
     */
    public String[] getComments() {
        if (comments != null) {
            return comments;
        }

        String[] comments = new String[1];
        comments[0] = "";
        return comments;
    }
}
