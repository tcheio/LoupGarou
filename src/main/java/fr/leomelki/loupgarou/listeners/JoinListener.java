package fr.leomelki.loupgarou.listeners;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.potion.PotionEffectType;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;

import fr.leomelki.loupgarou.MainLg;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.leomelki.loupgarou.utils.PacketUtils;

public class JoinListener implements Listener{
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		boolean noSpec = p.getGameMode() != GameMode.SPECTATOR;
		for(Player player : Bukkit.getOnlinePlayers())
			if(player != p) {
				if(player.getGameMode() != GameMode.SPECTATOR)
					player.hidePlayer(p);
				PacketUtils.sendTeam(p, player.getName(), TeamMode.CREATE, "", "", Arrays.asList(player.getName()));
				PacketUtils.sendTeam(player, p.getName(), TeamMode.CREATE, "", "", Arrays.asList(p.getName()));
			}
		p.setFoodLevel(6);
		if(e.getJoinMessage() == null || !e.getJoinMessage().equals("joinall"))
			p.getPlayer().setResourcePack("http://leomelki.fr/mcgames/ressourcepacks/v32/loup_garou.zip");
		else {
			LGPlayer lgp = LGPlayer.thePlayer(e.getPlayer());
			lgp.showView();
			lgp.join(MainLg.getInstance().getCurrentGame());
		}
		if(noSpec)
			p.setGameMode(GameMode.ADVENTURE);
		e.setJoinMessage("");
		p.removePotionEffect(PotionEffectType.JUMP_BOOST);
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		p.setWalkSpeed(0.2f);
	}
	@EventHandler
	public void onResoucePack(PlayerResourcePackStatusEvent e) {
		if(e.getStatus() == Status.SUCCESSFULLY_LOADED) {
			Player p = e.getPlayer();
			LGPlayer lgp = LGPlayer.thePlayer(p);
			lgp.showView();
			lgp.join(MainLg.getInstance().getCurrentGame());
		}else if(e.getStatus() == Status.DECLINED || e.getStatus() == Status.FAILED_DOWNLOAD)
			e.getPlayer().kickPlayer(MainLg.getPrefix()+"§cIl vous faut le resourcepack pour jouer ! ("+e.getStatus()+")");
	}
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		LGPlayer lgp = LGPlayer.thePlayer(p);
		if(lgp.getGame() != null) {
			lgp.leaveChat();
			if(lgp.getRole() != null && !lgp.isDead())
				lgp.getGame().kill(lgp, Reason.DISCONNECTED, true);
			lgp.getGame().getInGame().remove(lgp);
			lgp.getGame().checkLeave();
		}
		LGPlayer.removePlayer(p);
		lgp.remove();
	}
	
}
