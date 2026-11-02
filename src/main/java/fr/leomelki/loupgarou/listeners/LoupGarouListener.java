package fr.leomelki.loupgarou.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;

import fr.leomelki.loupgarou.events.LGGameJoinEvent;
import fr.leomelki.loupgarou.utils.PacketUtils;

public class LoupGarouListener implements Listener {
	@EventHandler
	public void onGameJoin(LGGameJoinEvent e) {
		//Tous les loups-garous
		PacketUtils.removeTeam(e.getPlayer().getPlayer(), "loup_garou_list");

		//Loup-Garou noir
		PacketUtils.send(e.getPlayer().getPlayer(), new WrapperPlayServerResetScore("été", "lg_scoreboard"));
	}
}
