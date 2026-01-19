package org.mounts.event;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import org.mounts.plugin.ChocoboPlugin;

public class RegisterPlayer {
    public static void onPlayerReady(PlayerReadyEvent event){
        Player player = event.getPlayer();
        ChocoboPlugin.registerPlayer(player);
    }
}
