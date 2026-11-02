package fr.leomelki.loupgarou.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.PlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetExperience;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleSubtitle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleText;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleTimes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.OptionData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.ScoreBoardTeamInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTimeUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PacketUtils {
	private static final Map<UUID, UserProfile> profiles = new ConcurrentHashMap<>();
	private static final Map<UUID, WrapperPlayServerRespawn> spawnInfos = new ConcurrentHashMap<>();

	public static final EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> ADD_PLAYER_ACTIONS = EnumSet.of(
			WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
			WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
			WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
			WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY,
			WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME);

	public static void send(Player player, PacketWrapper<?> packet) {
		if(player != null && player.isOnline())
			PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
	}

	public static Component text(String text) {
		return AdventureSerializer.serializer().fromLegacy(text == null ? "" : text);
	}

	public static String legacy(Component component) {
		return AdventureSerializer.serializer().asLegacy(component);
	}

	public static NamedTextColor lastColor(String text) {
		NamedTextColor color = NamedTextColor.WHITE;
		for(int i = 0; i < text.length()-1; i++)
			if(text.charAt(i) == ChatColor.COLOR_CHAR) {
				ChatColor chatColor = ChatColor.getByChar(text.charAt(i+1));
				if(chatColor == ChatColor.RESET)
					color = NamedTextColor.WHITE;
				else if(chatColor != null && chatColor.isColor())
					color = NamedTextColor.NAMES.valueOr(chatColor.name().toLowerCase(Locale.ROOT), color);
			}
		return color;
	}

	public static UserProfile copyProfile(UserProfile profile) {
		List<TextureProperty> properties = new ArrayList<>();
		if(profile.getTextureProperties() != null)
			for(TextureProperty property : profile.getTextureProperties())
				properties.add(new TextureProperty(property.getName(), property.getValue(), property.getSignature()));
		return new UserProfile(profile.getUUID(), profile.getName(), properties);
	}

	public static void cacheProfile(UserProfile profile) {
		if(profile != null && profile.getUUID() != null && profile.getName() != null && profile.getTextureProperties() != null && !profile.getTextureProperties().isEmpty())
			profiles.put(profile.getUUID(), copyProfile(profile));
	}

	public static UserProfile getProfile(UUID uuid, String name) {
		UserProfile cached = profiles.get(uuid);
		return cached != null ? copyProfile(cached) : new UserProfile(uuid, name, new ArrayList<>());
	}

	public static PlayerInfo playerInfo(UUID uuid, String name, int latency, GameMode gameMode, String displayName) {
		return new PlayerInfo(getProfile(uuid, name), true, latency, gameMode, displayName == null ? null : text(displayName), null);
	}

	public static void addPlayerInfo(Player to, List<PlayerInfo> infos) {
		send(to, new WrapperPlayServerPlayerInfoUpdate(EnumSet.copyOf(ADD_PLAYER_ACTIONS), infos));
	}

	public static void addPlayerInfo(Player to, PlayerInfo info) {
		addPlayerInfo(to, new ArrayList<>(Collections.singletonList(info)));
	}

	public static void removePlayerInfo(Player to, UUID uuid) {
		send(to, new WrapperPlayServerPlayerInfoRemove(uuid));
	}

	public static ScoreBoardTeamInfo teamInfo(String name, String prefix, String suffix) {
		return new ScoreBoardTeamInfo(Component.text(name), text(prefix), text(suffix), NameTagVisibility.ALWAYS, CollisionRule.ALWAYS, NamedTextColor.WHITE, OptionData.NONE);
	}

	public static void sendTeam(Player to, String name, TeamMode mode, String prefix, String suffix, Collection<String> players) {
		ScoreBoardTeamInfo info = mode == TeamMode.CREATE || mode == TeamMode.UPDATE ? teamInfo(name, prefix, suffix) : null;
		send(to, new WrapperPlayServerTeams(name, mode, info, players == null ? new ArrayList<>() : new ArrayList<>(players)));
	}

	public static void removeTeam(Player to, String name) {
		sendTeam(to, name, TeamMode.REMOVE, null, null, null);
	}

	public static void destroyEntities(Player to, int... entityIds) {
		send(to, new WrapperPlayServerDestroyEntities(entityIds));
	}

	public static void spawnArmorStand(Player to, int entityId, double x, double y, double z, float yaw) {
		send(to, new WrapperPlayServerSpawnEntity(entityId, Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND, new Vector3d(x, y, z), 0, yaw, 0, 0, Optional.of(Vector3d.zero())));
	}

	public static void sendMetadata(Player to, int entityId, EntityData<?>... data) {
		send(to, new WrapperPlayServerEntityMetadata(entityId, new ArrayList<>(Arrays.asList(data))));
	}

	public static EntityData<Byte> entityFlags(byte flags) {
		return new EntityData<>(0, EntityDataTypes.BYTE, flags);
	}

	public static EntityData<Optional<Component>> customName(String name) {
		return new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(text(name)));
	}

	public static EntityData<Boolean> customNameVisible(boolean visible) {
		return new EntityData<>(3, EntityDataTypes.BOOLEAN, visible);
	}

	public static EntityData<Boolean> noGravity(boolean noGravity) {
		return new EntityData<>(5, EntityDataTypes.BOOLEAN, noGravity);
	}

	public static void rotateEntity(Player to, int entityId, float yaw, float pitch) {
		send(to, new WrapperPlayServerEntityRotation(entityId, yaw, pitch, false));
	}

	public static void setHelmet(Player to, int entityId, ItemType item) {
		send(to, new WrapperPlayServerEntityEquipment(entityId, new ArrayList<>(Collections.singletonList(new Equipment(EquipmentSlot.HELMET, ItemStack.builder().type(item).amount(1).build())))));
	}

	public static void setHeldSlot(Player to, int slot) {
		send(to, new WrapperPlayServerHeldItemChange(slot));
	}

	public static void sendTime(Player to, long time) {
		send(to, new WrapperPlayServerTimeUpdate(0, time, true));
	}

	public static void sendExperience(Player to, float bar, int level) {
		send(to, new WrapperPlayServerSetExperience(bar, level, 0));
	}

	public static void sendHealth(Player to, float health, int food, float saturation) {
		send(to, new WrapperPlayServerUpdateHealth(health, food, saturation));
	}

	public static void sendActionBar(Player to, String message) {
		send(to, new WrapperPlayServerActionBar(text(message)));
	}

	public static void sendTitle(Player to, String title, String subTitle, int stay) {
		send(to, new WrapperPlayServerSetTitleTimes(10, stay, 10));
		send(to, new WrapperPlayServerSetTitleText(text(title)));
		send(to, new WrapperPlayServerSetTitleSubtitle(text(subTitle)));
	}

	public static void storeSpawnInfo(UUID uuid, WrapperPlayServerJoinGame join) {
		spawnInfos.put(uuid, new WrapperPlayServerRespawn(join.getDimensionTypeRef(), join.getWorldName(), join.getDifficulty(), join.getHashedSeed(), join.getGameMode(), join.getPreviousGameMode(), join.isDebug(), join.isFlat(), WrapperPlayServerRespawn.KEEP_ALL_DATA, join.getLastDeathPosition(), join.getPortalCooldown().orElse(0), join.getSeaLevel()));
	}

	public static void storeSpawnInfo(UUID uuid, WrapperPlayServerRespawn respawn) {
		spawnInfos.put(uuid, new WrapperPlayServerRespawn(respawn.getDimensionTypeRef(), respawn.getWorldName().orElse(null), respawn.getDifficulty(), respawn.getHashedSeed(), respawn.getGameMode(), respawn.getPreviousGameMode(), respawn.isWorldDebug(), respawn.isWorldFlat(), WrapperPlayServerRespawn.KEEP_ALL_DATA, respawn.getLastDeathPosition(), respawn.getPortalCooldown().orElse(0), respawn.getSeaLevel()));
	}

	public static void forget(UUID uuid) {
		spawnInfos.remove(uuid);
		profiles.remove(uuid);
	}

	public static boolean respawnInPlace(Player player, org.bukkit.GameMode gameMode) {
		WrapperPlayServerRespawn info = spawnInfos.get(player.getUniqueId());
		if(info == null)
			return false;
		WrapperPlayServerRespawn respawn = new WrapperPlayServerRespawn(info.getDimensionTypeRef(), info.getWorldName().orElse(null), info.getDifficulty(), info.getHashedSeed(), GameMode.valueOf(gameMode.name()), info.getPreviousGameMode(), info.isWorldDebug(), info.isWorldFlat(), WrapperPlayServerRespawn.KEEP_ALL_DATA, info.getLastDeathPosition(), info.getPortalCooldown().orElse(0), info.getSeaLevel());
		send(player, respawn);
		send(player, new WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.START_LOADING_CHUNKS, 0));
		send(player, new WrapperPlayServerEntityStatus(player.getEntityId(), player.isOp() ? 28 : 24));
		return true;
	}

	public static void resendPlayerState(Player player) {
		player.sendHealthUpdate();
		player.sendExperienceChange(player.getExp(), player.getLevel());
		setHeldSlot(player, player.getInventory().getHeldItemSlot());
		for(PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
			player.removePotionEffect(effect.getType());
			player.addPotionEffect(effect);
		}
	}
}
