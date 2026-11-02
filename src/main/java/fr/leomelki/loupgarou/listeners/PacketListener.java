package fr.leomelki.loupgarou.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.protocol.world.clock.ClockNetworkState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.Action;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.PlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.ScoreBoardTeamInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTimeUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;

import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.events.LGSkinLoadEvent;
import fr.leomelki.loupgarou.events.LGUpdatePrefixEvent;
import fr.leomelki.loupgarou.utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PacketListener extends PacketListenerAbstract {

	public PacketListener() {
		super(PacketListenerPriority.NORMAL);
	}

	@Override
	public void onUserDisconnect(UserDisconnectEvent event) {
		if(event.getUser().getUUID() != null)
			PacketUtils.forget(event.getUser().getUUID());
	}

	@Override
	public void onPacketSend(PacketSendEvent event) {
		PacketTypeCommon type = event.getPacketType();
		if(type == PacketType.Play.Server.JOIN_GAME) {
			if(event.getUser().getUUID() != null)
				PacketUtils.storeSpawnInfo(event.getUser().getUUID(), new WrapperPlayServerJoinGame(event));
			return;
		}
		if(type == PacketType.Play.Server.RESPAWN) {
			if(event.getUser().getUUID() != null)
				PacketUtils.storeSpawnInfo(event.getUser().getUUID(), new WrapperPlayServerRespawn(event));
			return;
		}

		Player bukkitPlayer = event.getPlayer();
		if(bukkitPlayer == null)
			return;

		if(type == PacketType.Play.Server.TIME_UPDATE)
			onTimeUpdate(event, bukkitPlayer);
		else if(type == PacketType.Play.Server.SOUND_EFFECT)
			onSound(event);
		else if(type == PacketType.Play.Server.PLAYER_INFO_UPDATE)
			onPlayerInfo(event, bukkitPlayer);
		else if(type == PacketType.Play.Server.UPDATE_HEALTH)
			onHealth(event, bukkitPlayer);
		else if(type == PacketType.Play.Server.TEAMS)
			onTeam(event, bukkitPlayer);
		else if(type == PacketType.Play.Server.ENTITY_EQUIPMENT)
			onEquipment(event, bukkitPlayer);
	}

	private void onTimeUpdate(PacketSendEvent event, Player bukkitPlayer) {
		WrapperPlayServerTimeUpdate time = new WrapperPlayServerTimeUpdate(event);
		ClockNetworkState state = time.getClockState();
		LGPlayer lgp = LGPlayer.thePlayer(bukkitPlayer);
		if(state != null && lgp.getGame() != null && lgp.getGame().getTime() != state.getTotalTicks())
			event.setCancelled(true);
	}

	private void onSound(PacketSendEvent event) {
		WrapperPlayServerSoundEffect sound = new WrapperPlayServerSoundEffect(event);
		if(sound.getSound() != null && Sounds.ENTITY_PLAYER_ATTACK_NODAMAGE.getSoundId().equals(sound.getSound().getSoundId()))
			event.setCancelled(true);
	}

	private void onPlayerInfo(PacketSendEvent event, Player bukkitPlayer) {
		WrapperPlayServerPlayerInfoUpdate info = new WrapperPlayServerPlayerInfoUpdate(event);
		boolean addPlayer = info.getActions().contains(Action.ADD_PLAYER);
		if(!addPlayer && !info.getActions().contains(Action.UPDATE_DISPLAY_NAME))
			return;
		LGPlayer player = LGPlayer.thePlayer(bukkitPlayer);
		boolean changed = false;
		boolean displayNameChanged = false;
		for(PlayerInfo data : info.getEntries()) {
			if(addPlayer)
				PacketUtils.cacheProfile(data.getGameProfile());
			Player other = Bukkit.getPlayer(data.getProfileId());
			if(other == null)
				continue;
			LGPlayer lgp = LGPlayer.thePlayer(other);
			if(player.getGame() != null && player.getGame() == lgp.getGame()) {
				LGUpdatePrefixEvent evt2 = new LGUpdatePrefixEvent(player.getGame(), lgp, player, "");
				Bukkit.getPluginManager().callEvent(evt2);
				if(evt2.getPrefix().length() > 0) {
					Component displayName = data.getDisplayName();
					String name = displayName != null ? PacketUtils.legacy(displayName) : other.getName();
					data.setDisplayName(PacketUtils.text(evt2.getPrefix()+name));
					displayNameChanged = true;
				}
				if(addPlayer) {
					LGSkinLoadEvent evt = new LGSkinLoadEvent(lgp.getGame(), lgp, player, PacketUtils.copyProfile(data.getGameProfile()));
					Bukkit.getPluginManager().callEvent(evt);
					data.setGameProfile(evt.getProfile());
				}
				changed = true;
			}
		}
		if(displayNameChanged && !info.getActions().contains(Action.UPDATE_DISPLAY_NAME))
			info.getActions().add(Action.UPDATE_DISPLAY_NAME);
		if(changed)
			event.markForReEncode(true);
	}

	private void onHealth(PacketSendEvent event, Player bukkitPlayer) {
		LGPlayer player = LGPlayer.thePlayer(bukkitPlayer);
		if(player.getGame() != null && player.getGame().isStarted()) {
			WrapperPlayServerUpdateHealth health = new WrapperPlayServerUpdateHealth(event);
			health.setFood(6);
			event.markForReEncode(true);
		}
	}

	private void onTeam(PacketSendEvent event, Player bukkitPlayer) {
		WrapperPlayServerTeams team = new WrapperPlayServerTeams(event);
		if(!team.getTeamInfo().isPresent())
			return;
		ScoreBoardTeamInfo teamInfo = team.getTeamInfo().get();
		teamInfo.setColor(NamedTextColor.WHITE);
		event.markForReEncode(true);
		Player other = Bukkit.getPlayer(team.getTeamName());
		if(other == null)
			return;
		LGPlayer player = LGPlayer.thePlayer(bukkitPlayer);
		LGPlayer lgp = LGPlayer.thePlayer(other);
		if(player.getGame() != null && player.getGame() == lgp.getGame()) {
			LGUpdatePrefixEvent evt2 = new LGUpdatePrefixEvent(player.getGame(), lgp, player, "");
			Bukkit.getPluginManager().callEvent(evt2);
			String prefix = evt2.getPrefix().length() > 0 ? evt2.getPrefix() : "§f";
			teamInfo.setPrefix(PacketUtils.text(prefix));
			teamInfo.setColor(PacketUtils.lastColor(prefix));
		}
	}

	private void onEquipment(PacketSendEvent event, Player bukkitPlayer) {
		LGPlayer player = LGPlayer.thePlayer(bukkitPlayer);
		if(player.getGame() != null) {
			WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment(event);
			if(equip.getEntityId() == bukkitPlayer.getEntityId())
				return;
			boolean changed = false;
			for(Equipment equipment : equip.getEquipment())
				if(equipment.getSlot() == EquipmentSlot.OFF_HAND) {
					equipment.setItem(ItemStack.EMPTY);
					changed = true;
				}
			if(changed)
				event.markForReEncode(true);
		}
	}
}
