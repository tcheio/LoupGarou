package fr.leomelki.loupgarou.scoreboard;

import java.util.Arrays;
import java.util.Optional;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;

import fr.leomelki.loupgarou.utils.PacketUtils;
import fr.leomelki.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;

public class CustomScoreboardEntry {
	private static String nullComponent = "";

	//setter car flemme de modifier le systeme pour le rendre plus logique
	@Getter @Setter private int score;
	private final String name;
	private final CustomScoreboard scoreboard;
	private String prefix, suffix;

	public CustomScoreboardEntry(int score, CustomScoreboard scoreboard) {
		this.score = score;
		this.scoreboard = scoreboard;
		this.name = "§"+VariousUtils.toHex(score);
	}

	public void show() {
		if(prefix != null) {
			PacketUtils.sendTeam(scoreboard.getPlayer().getPlayer(), name, TeamMode.CREATE, prefix, suffix, Arrays.asList(name));

			PacketUtils.send(scoreboard.getPlayer().getPlayer(), new WrapperPlayServerUpdateScore(scoreboard.getName(), WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM, name, Optional.of(this.score)));
		}
	}

	public void setDisplayName(String displayName) {
		boolean spawn = prefix == null;
		if(displayName.length() > 16) {
			char colorCode = 'f';
			int limit = displayName.charAt(14) == '§' && displayName.charAt(13) != '§' ? 14 : displayName.charAt(15) == '§' ? 15 : 16;
			String prefixStr = displayName.substring(0, limit);

			prefix = prefixStr;

			if(limit == 16) {
				boolean storeColorCode = false;
				for(char c : prefixStr.toCharArray())
					if(storeColorCode) {
						storeColorCode = false;
						colorCode = c;
					}else
						if(c == '§')
							storeColorCode = true;
				suffix = "§"+colorCode+displayName.substring(limit);
			}else
				suffix = displayName.substring(limit);
		} else {
			prefix = displayName;
			suffix = nullComponent;
		}

		if(scoreboard.isShown()) {
			if(spawn)
				show();
			else
				PacketUtils.sendTeam(scoreboard.getPlayer().getPlayer(), name, TeamMode.UPDATE, prefix, suffix, Arrays.asList(name));
		}
	}
	public void delete() {
		hide();
		prefix = null;
	}
	public void hide() {
		if(prefix != null && scoreboard.isShown()) {
			PacketUtils.send(scoreboard.getPlayer().getPlayer(), new WrapperPlayServerResetScore(name, scoreboard.getName()));

			PacketUtils.removeTeam(scoreboard.getPlayer().getPlayer(), name);
		}
	}

}
