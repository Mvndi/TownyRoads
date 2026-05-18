package net.mvndicraft.townyroads.permissions;

import net.mvndicraft.townyroads.TownyRoadsPlugin;

public enum TownyRoadsPermissionNodes {
    TOWNYROADS_ADMIN(TownyRoadsPlugin.ADMIN_PERMISSION), TOWNYROADS_CLAIM("townyroads.command.townyroads.claim"),
    TOWNYROADS_UNCLAIM("townyroads.command.townyroads.unclaim"), TOWNYROADS_CREATE("townyroads.command.townyroads.create"),
    TOWNYROADS_DELETE("townyroads.command.townyroads.delete"), TOWNYROADS_ACCEPT("townyroads.command.townyroads.accept"),
    TOWNYROADS_CLAIMED_OWNROAD_ALL("townyroads.claimed.ownroad.*"),
    TOWNYROADS_CLAIMED_OWNROAD_BLOCK_BUILD("townyroads.claimed.ownroad.build.*"),
    TOWNYROADS_CLAIMED_OWNROAD_BLOCK_DESTROY("townyroads.claimed.ownroad.destroy.*"),
    TOWNYROADS_CLAIMED_OWNROAD_BLOCK_SWITCH("townyroads.claimed.ownroad.switch.*"),
    TOWNYROADS_CLAIMED_OWNROAD_BLOCK_ITEM_USE("townyroads.claimed.ownroad.item_use.*");

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
