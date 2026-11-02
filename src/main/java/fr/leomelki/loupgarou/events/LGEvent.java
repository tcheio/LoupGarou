package fr.leomelki.loupgarou.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.leomelki.loupgarou.classes.LGGame;
import lombok.Getter;

public class LGEvent extends Event{
	@Getter final LGGame game;

	public LGEvent(LGGame game) {
		super(!Bukkit.isPrimaryThread());
		this.game = game;
	}

    private static final HandlerList handlers = new HandlerList();
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
