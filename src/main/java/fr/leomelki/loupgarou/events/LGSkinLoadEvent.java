package fr.leomelki.loupgarou.events;

import com.github.retrooper.packetevents.protocol.player.UserProfile;

import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.Setter;

public class LGSkinLoadEvent extends LGEvent {

	@Getter private final LGPlayer player, to;
	@Getter @Setter private UserProfile profile;
	public LGSkinLoadEvent(LGGame game, LGPlayer player, LGPlayer to, UserProfile profile) {
		super(game);
		this.player = player;
		this.to = to;
		this.profile = profile;
	}

}
