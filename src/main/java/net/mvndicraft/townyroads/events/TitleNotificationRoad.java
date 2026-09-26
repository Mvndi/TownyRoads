package net.mvndicraft.townyroads.events;

import com.palmergames.bukkit.towny.object.notification.TitleNotification;
import net.mvndicraft.townyroads.ChunkCoord;
import net.mvndicraft.townyroads.Road;

public class TitleNotificationRoad extends TitleNotification {
    protected Road road;

    public TitleNotificationRoad(Road road, ChunkCoord chunkCoord) {
        super(null, chunkCoord.toWorldCoord());
        this.road = road;
        makeRoadTitles();
    }

    private void makeRoadTitles() {
		String title = "";
		String subtitle = road.getShortName();
		
		setTitleNotification(title);
		setSubtitleNotification(subtitle);
	}
    
}
