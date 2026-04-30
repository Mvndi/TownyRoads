package net.mvndicraft.townyroads.permissions;

import net.mvndicraft.townyroads.TownyRoadsPlugin;

public enum TownyRoadsPermissionNodes {
    TOWNY_ROADS_ADMIN(TownyRoadsPlugin.ADMIN_PERMISSION), TOWNY_ROADS_CLAIM("towny.command.townyroads.claim");

    private String value;

    /**
     * Constructor
     * 
     * @param permission - Permission.
     */
    TownyRoadsPermissionNodes(String permission) {

        this.value = permission;
    }

    /**
     * Retrieves the permission node
     * 
     * @return The permission node
     */
    public String getNode() {

        return value;
    }

    /**
     * Retrieves the permission node
     * replacing the character *
     * 
     * @param replace - String
     * @return The permission node
     */
    public String getNode(String replace) {

        return value.replace("*", replace);
    }

    public String getNode(int replace) {

        return value.replace("*", replace + "");
    }
}
