package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Announcements;
import com.dream.game.GameTimeController;
import com.dream.game.access.gmController;
import com.dream.game.cache.HtmCache;
import com.dream.game.communitybbs.Manager.RegionBBSManager;
import com.dream.game.datatables.GmListTable;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.datatables.xml.PartyFarmData;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.handler.VoicedCommandHandler;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.CoupleManager;
import com.dream.game.manager.CrownManager;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.manager.DimensionalRiftManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.PetitionManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.SiegeRewardManager;
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Clan.SubPledge;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2FriendList;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2PartyFarmEvent;
import com.dream.game.model.L2ShortCut;
import com.dream.game.model.L2SiegeStatus;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.entity.Couple;
import com.dream.game.model.entity.Hero;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.model.entity.events.GameEventManager;
import com.dream.game.model.entity.events.StartupSystem;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.model.world.L2World;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.Die;
import com.dream.game.network.serverpackets.EtcStatusUpdate;
import com.dream.game.network.serverpackets.ExBasicActionList;
import com.dream.game.network.serverpackets.ExStorageMaxCount;
import com.dream.game.network.serverpackets.FriendList;
import com.dream.game.network.serverpackets.HennaInfo;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.PledgeShowMemberListAll;
import com.dream.game.network.serverpackets.PledgeShowMemberListUpdate;
import com.dream.game.network.serverpackets.PledgeSkillList;
import com.dream.game.network.serverpackets.PledgeStatusChanged;
import com.dream.game.network.serverpackets.QuestList;
import com.dream.game.network.serverpackets.ShortCutInit;
import com.dream.game.network.serverpackets.ShortCutRegister;
import com.dream.game.network.serverpackets.SkillCoolTime;
import com.dream.game.network.serverpackets.SpecialCamera;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.game.util.FloodProtector;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import org.apache.log4j.Logger;

public class EnterWorld extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(EnterWorld.class.getName());
	
	long _daysleft;
	SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy");
	
	private static void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();
		
		for (Couple cl : CoupleManager.getInstance().getCouples())
			if (cl.getPlayer1Id() == _chaid || cl.getPlayer2Id() == _chaid)
			{
				if (cl.getMaried())
				{
					cha.setMaried(true);
				}
				
				cha.setCoupleId(cl.getId());
			}
	}
	
	private static void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}
	
	private static void notifyClanMembers(L2PcInstance activeChar)
	{
		final L2Clan clan = activeChar.getClan();
		
		// Refresh player instance.
		clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
		
		final PledgeShowMemberListUpdate update = new PledgeShowMemberListUpdate(activeChar);
		
		// Send packet to others members.
		for (L2PcInstance member : clan.getOnlineMembers())
		{
			if (member == activeChar)
			{
				continue;
			}
			
			member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addPcName(activeChar));
			member.sendPacket(update);
		}
		
		if (clan.isNoticeEnabled() && !clan.getNotice().isEmpty())
		{
			activeChar.addMessage("<html><title>Clan Message</title><body><br><center><font color=\"CCAA00\">" + activeChar.getClan().getName() + "</font> <font color=\"6655FF\">Clan Alert Message</font></center><br>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1><br>" + activeChar.getClan().getNotice() + "</body></html>");
		}
	}
	
	private static void notifyFriends(L2PcInstance cha)
	{
		for (String friendName : L2FriendList.getFriendListNames(cha))
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
			if (friend != null)
			{
				friend.sendPacket(new FriendList(friend));
				friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addString(cha.getName()));
			}
		}
	}
	
	private static void notifyPartner(L2PcInstance cha)
	{
		if (cha.getPartnerId() != 0)
		{
			L2PcInstance partner = L2World.getInstance().getPlayer(cha.getPartnerId());
			if (partner != null)
			{
				partner.sendMessage("Your partner " + cha.getName() + " logged in the world.");
			}
		}
	}
	
	private static void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = L2World.getInstance().getPlayer(activeChar.getSponsor());
			if (sponsor != null)
			{
				sponsor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addPcName(activeChar));
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = L2World.getInstance().getPlayer(activeChar.getApprentice());
			if (apprentice != null)
			{
				apprentice.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addPcName(activeChar));
			}
		}
	}
	
	private static void updateShortCuts(L2PcInstance activeChar)
	{
		L2ShortCut[] allShortCuts = activeChar.getAllShortCuts();
		
		for (L2ShortCut sc : allShortCuts)
		{
			activeChar.sendPacket(new ShortCutRegister(sc));
		}
	}
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			_log.warn("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}
		
		L2Object object = null;
		object = L2World.getInstance().getPlayer(activeChar.getName());
		if (object instanceof L2PcInstance)
		{
			_log.warn("EnterWorld failed! duplicate caracter");
			getClient().closeNow();
			return;
		}
		
		/* if (L2World.getInstance().findObject(activeChar.getObjectId()) != null) { getClient().closeNow(); return; } */
		
		FloodProtector.registerNewPlayer(activeChar);
		
		if (gmController.getInstance().checkPrivs(activeChar))
		{
			int objId = activeChar.getObjectId();
			if (Config.SHOW_GM_LOGIN)
			{
				Announcements.getInstance().announceToAll("Administrator: " + activeChar.getName() + " comes into play.");
			}
			if (Config.GM_STARTUP_INVULNERABLE && gmController.getInstance().hasAccess("invul", objId))
			{
				activeChar.setIsInvul(true);
			}
			if (Config.GM_STARTUP_INVISIBLE && gmController.getInstance().hasAccess("invis", objId))
			{
				activeChar.getAppearance().setInvisible();
			}
			if (Config.GM_STARTUP_SILENCE && gmController.getInstance().hasAccess("silence", objId))
			{
				activeChar.setMessageRefusal(true);
			}
			if (Config.GM_STARTUP_AUTO_LIST && gmController.getInstance().hasAccess("gmliston", objId))
			{
				GmListTable.getInstance().addGm(activeChar, false);
			}
			else
			{
				GmListTable.getInstance().addGm(activeChar, true);
			}
		}
		
		if (activeChar.getStatus().getCurrentHp() < 0.5)
		{
			activeChar.setIsDead(true);
		}
		
		activeChar.setOnlineStatus(true);
		activeChar.setRunning();
		activeChar.standUp();
		activeChar.broadcastKarma();
		activeChar.decayMe();
		
		final L2Effect[] effects = activeChar.getAllEffects();
		
		if (effects != null)
		{
			for (L2Effect e : effects)
			{
				if (e.getEffectType() == L2EffectType.HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2EffectType.HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}
				if (e.getEffectType() == L2EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2EffectType.COMBAT_POINT_HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}
			}
		}
		
		final L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			activeChar.sendPacket(new PledgeSkillList(clan));
			notifyClanMembers(activeChar);
			notifySponsorOrApprentice(activeChar);
			
			// Add message at connexion if clanHall not paid.
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if (clanHall != null)
				if (!clanHall.getPaid())
				{
					activeChar.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_TOMORROW);
				}
			
			for (Siege siege : SiegeManager.getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					L2SiegeStatus.getInstance().addStatus(activeChar.getClanId(), activeChar.getObjectId());
					activeChar.setSiegeState((byte) 1);
				}
				
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					L2SiegeStatus.getInstance().addStatus(activeChar.getClanId(), activeChar.getObjectId());
					activeChar.setSiegeState((byte) 2);
				}
			}
			
			for (FortSiege fsiege : FortSiegeManager.getInstance().getSieges())
			{
				if (!fsiege.getIsInProgress())
				{
					continue;
				}
				
				if (fsiege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
				}
				else if (fsiege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
				}
			}
			if (DevastatedCastleSiege.getInstance().getIsInProgress())
				if (DevastatedCastleSiege.getInstance().checkIsRegistered(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
				}
			if (FortressOfDeadSiege.getInstance().getIsInProgress())
				if (FortressOfDeadSiege.getInstance().checkIsRegistered(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
				}
			
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));
			
			for (SubPledge sp : clan.getAllSubPledges())
			{
				activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
			}
			
			activeChar.sendPacket(new PledgeStatusChanged(clan));
		}
		
		for (L2ItemInstance temp : activeChar.getInventory().getAugmentedItems())
			if (temp != null && temp.isEquipped())
			{
				temp.getAugmentation().applyBonus(activeChar);
			}
		
		if (activeChar.getRace().ordinal() == 2)
			if (activeChar.getSkillLevel(294) == 1)
				if (GameTimeController.isNowNight())
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NIGHT_EFFECT_APPLIES).addSkillName(294));
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NIGHT_EFFECT_DISAPPEARS).addSkillName(294));
				}
			
		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
		{
			activeChar.setHero(true);
		}
		else
		{
			activeChar.restoreHeroServiceData(activeChar);
		}
		
		if (!activeChar.isHero() && Config.ALT_STRICT_HERO_SYSTEM)
		{
			for (L2ItemInstance item : activeChar.getInventory().getItems())
				if (item.isHeroItem())
				{
					activeChar.destroyItem("RemoveHero", item, null, false);
				}
		}
		
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			int owner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE);
			if (owner != SevenSigns.CABAL_NULL)
			{
				int cabal = SevenSigns.getInstance().getPlayerCabal(activeChar);
				if (cabal == owner)
				{
					activeChar.addSkill(SkillTable.getInstance().getInfo(5074, 1), false);
				}
				else if (cabal != SevenSigns.CABAL_NULL)
				{
					activeChar.addSkill(SkillTable.getInstance().getInfo(5075, 1), false);
				}
			}
		}
		else
		{
			activeChar.removeSkill(5074);
			activeChar.removeSkill(5075);
		}
		
		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new ItemList(activeChar, false));
		activeChar.sendPacket(new ShortCutInit(activeChar));
		activeChar.sendPacket(new HennaInfo(activeChar));
		
		Quest.playerEnter(activeChar);
		if (Config.DISABLE_TUTORIAL == false)
		{
			loadTutorial(activeChar);
		}
		activeChar.sendPacket(new QuestList(activeChar));
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}
		
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		activeChar.getKnownList().updateKnownObjects();
		
		activeChar.updateEffectIcons();
		activeChar.sendEtcStatusUpdate();
		
		GameEvent evt = GameEventManager.getInstance().participantOf(activeChar);
		
		if (evt != null)
		{
			activeChar._event = evt;
			activeChar._event.onLogin(activeChar);
		}
		
		activeChar.sendPacket(ExBasicActionList.DEFAULT_ACTION_LIST);
		activeChar.regiveTemporarySkills();
		activeChar.getInventory().reloadEquippedItems();
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		activeChar.sendPacket(new FriendList(activeChar));
		activeChar.sendPacket(new UserInfo(activeChar, true));
		
		activeChar.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);
		
		ObjectRestrictions.getInstance().resumeTasks(activeChar.getObjectId());
		
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
		{
			if (!activeChar.isAio())
			{
				activeChar.checkAllowedSkills();
			}
		}
		activeChar.sendSkillList();
		
		RegionBBSManager.getInstance().changeCommunityBoard();
		activeChar.academyCheck(activeChar.getClassId().getId());
		
		if (Config.ACTIVATED_SYSTEM)
		{
			if (!SiegeRewardManager.REWARD_ACTIVE_MEMBERS_ONLY)
				SiegeRewardManager.getInstance().processWorldEnter(activeChar);
		}
		
		if (Config.PROTECTION_SECOND_PASSWORD)
		{
			
			activeChar.setTradeRefusal(true);
			activeChar.setExchangeRefusal(true);
			activeChar.setIsInvul(true);
			activeChar.setIsParalyzed(true);
			activeChar.setSecondRefusal(true);
			
			if (RequestBypassToServer.getPassKeyEnable(activeChar))
			{
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/login.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
			}
			else
			{
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/setup.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
			}
		}
		
		if (activeChar.isVip())
		{
			onEnterVip(activeChar);
		}
		
		if (activeChar.isAio())
		{
			onEnterAio(activeChar);
		}
		
		if (Config.ALLOW_VIP_NCOLOR && activeChar.isVip())
		{
			activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);
		}
		
		if (Config.ALLOW_VIP_TCOLOR && activeChar.isVip())
		{
			activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);
		}
		
		if (Config.ALLOW_AIO_NCOLOR && activeChar.isAio())
		{
			activeChar.getAppearance().setNameColor(Config.AIO_NCOLOR);
		}
		
		if (Config.ALLOW_AIO_TCOLOR && activeChar.isAio())
		{
			activeChar.getAppearance().setTitleColor(Config.AIO_TCOLOR);
		}
		if (Config.CLASS_MASTER_POPUP)
		{
			IVoicedCommandHandler handler = VoicedCommandHandler.getInstance().getVoicedCommandHandler("classmaster");
			if (handler != null)
			{
				int classLevel = activeChar.getClassLevel();
				if (classLevel == 0 && activeChar.getLevel() >= 20)
				{
					handler.useVoicedCommand("classmaster", activeChar, "");
				}
				else if (classLevel == 1 && activeChar.getLevel() >= 40)
				{
					handler.useVoicedCommand("classmaster", activeChar, "");
				}
				else if (classLevel == 2 && activeChar.getLevel() >= 76)
				{
					handler.useVoicedCommand("classmaster", activeChar, "");
				}
			}
		}
		
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
		
		if (activeChar.isAlikeDead())
		{
			sendPacket(new Die(activeChar));
		}
		
		if (Config.ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar);
			
			if (!activeChar.isMaried())
			{
				L2ItemInstance item = activeChar.getInventory().getItemByItemId(9140);
				
				if (item != null)
				{
					activeChar.destroyItem("Removing Cupid's Bow", item, activeChar, true);
					
					_log.info("Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " got Cupid's Bow removed.");
				}
			}
		}
		if (L2PartyFarmEvent.isRunning())
		{
			
			String lastStart = L2PartyFarmEvent.lastEvent();
			LocalTime startTime = LocalTime.parse(lastStart, DateTimeFormatter.ofPattern("HH:mm"));
			
			LocalTime now = LocalTime.now();
			
			int durationMinutes = PartyFarmData.getInstance().getConfig().getDuration();
			
			LocalTime endTime = startTime.plusMinutes(durationMinutes);
			
			Duration remaining = Duration.between(now, endTime);
			long minutes = remaining.toMinutes();
			
			if (minutes > 0)
			{
				activeChar.sendMessage("Party Farm is live! Started at " + lastStart + "h. Remaining time: " + minutes + " minute(s).");
			}
			else
			{
				activeChar.sendMessage("Party Farm is live! Started ons " + lastStart + "h.");
			}
			
		}
		notifyFriends(activeChar);
		activeChar.onPlayerEnter();
		sendPacket(new SkillCoolTime(activeChar));
		updateShortCuts(activeChar);
		
		if (!Config.STARTUP_SYSTEM_SELECTCLASS)
			activeChar.setVar("select_class", "1");
		
		if (!Config.STARTUP_SYSTEM_SELECTARMOR)
			activeChar.setVar("select_armor", "1");
		
		if (!Config.STARTUP_SYSTEM_SELECTWEAP)
			activeChar.setVar("select_weapon", "1");
		
		if (activeChar.getVar("startupfinish") == null || activeChar.getVar("select_armor") == null || activeChar.getVar("select_weapon") == null || activeChar.getVar("select_class") == null)
			onEnterNewbie(activeChar);
		
		if (activeChar.getVar("firstloginbuff") == null && Config.FIRSTLOGIN_BUFFS)
		{
			if (activeChar.isMageClass())
			{
				for (Integer skillid : Config.FIRSTLOGIN_MAGE_BUFF_LIST)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getMaxLevel(skillid));
					if (skill != null)
						skill.getEffects(activeChar, activeChar);
					activeChar.setVar("firstloginbuff", "1");
				}
			}
			else
			{
				for (Integer skillid : Config.FIRSTLOGIN_FIGHTER_BUFF_LIST)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getMaxLevel(skillid));
					if (skill != null)
						skill.getEffects(activeChar, activeChar);
					activeChar.setVar("firstloginbuff", "1");
				}
			}
		}
		
		L2ItemInstance flag = activeChar.getInventory().getItemByItemId(Config.FORTSIEGE_COMBAT_FLAG_ID);
		if (flag != null)
		{
			Fort fort = FortManager.getInstance().getFort(activeChar);
			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(activeChar);
			}
			else
			{
				int slot = flag.getItem().getBodyPart();
				activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
				activeChar.destroyItem("CombatFlag", flag, null, true);
			}
		}
		
		if (Olympiad.playerInStadia(activeChar))
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		}
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		}
		
		if (!activeChar.isGM() && activeChar.getSiegeState() < 2 && activeChar.isInsideZone(L2Zone.FLAG_SIEGE))
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
		
		if (!activeChar.isGM() && activeChar.isInsideZone(L2Zone.FLAG_NORESTART))
		{
			final long allowed_time = Config.KICKTIMERESTART * 60000;
			final long last_time = activeChar.getLastAccess();
			final long curr_time = System.currentTimeMillis();
			
			if (last_time + allowed_time < curr_time)
			{
				activeChar.sendPacket(SystemMessageId.NO_RESTART_HERE);
				activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				activeChar.sendMessage("SVR: Teleported to the nearest city for being offline for too long in a restricted area.");
			}
			
		}
		
		if (activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquippedId()).cursedOnLogin(activeChar);
		}
		
		announceSystem(activeChar);
		
		if (Config.SHOW_HTML_WELCOME)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/mods/welcome.htm");
			sendPacket(html);
		}
		
		if ((activeChar._event != null) && (activeChar._event.getState() == GameEvent.STATE_ACTIVE))
		{
			activeChar.addMessage("<html><body><br>Don't forget, you party the remaining <font color=\"LEVEL\">" + activeChar._event.getName() + "</font>!</body></html>");
		}
		CrownManager.checkCrowns(activeChar);
	}
	
	private static void announceSystem(L2PcInstance activeChar)
	{
		if (Config.ANNOUNCE_HER0 && activeChar.isHero())
		{
			Announcements.getInstance().announceToAll("The Hero " + activeChar.getName() + " is now Online!");
		}
		if (Config.ANNOUNCE_AIO && activeChar.isAio())
		{
			Announcements.getInstance().announceToAll("The AIOx " + activeChar.getName() + " is now Online!");
		}
		if (Config.ANNOUNCE_VIP && activeChar.isVip())
		{
			Announcements.getInstance().announceToAll("The VIP " + activeChar.getName() + " is now Online!");
		}
		if (Config.ANNOUNCE_NEWBIE && activeChar.getLevel() <= 20)
		{
			Announcements.getInstance().announceToAll("The Newbie " + activeChar.getName() + " is now Online!");
		}
		if (Config.ANNOUNCE_LORD)
		{
			final L2Clan clan = activeChar.getClan();
			if (clan != null)
			{
				if (clan.getHasCastle() > 0)
				{
					final Castle castle = CastleManager.getInstance().getCastleById(clan.getHasCastle());
					if ((castle != null) && (activeChar.getObjectId() == clan.getLeaderId()))
						Announcements.getInstance().announceToAll("Lord " + activeChar.getName() + " Ruler Of " + castle.getName() + " Castle is now Online!");
				}
			}
		}
	}
	
	private void onEnterAio(L2PcInstance activeChar)
	{
		long xpcur = 0;
		long xpres = 0;
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getAioEndTime();
		if (now > endDay)
		{
			activeChar.setAio(false);
			activeChar.setAioEndTime(0);
			activeChar.lostAioSkills();
			activeChar.getAppearance().setNameColor(0xFFFFFF);
			activeChar.getAppearance().setTitleColor(0xFFFF77);
			
			if (Config.ENABLE_AIO_DELEVEL)
			{
				xpcur = activeChar.getStat().getExp();
				xpres = activeChar.getStat().getExpForLevel(Config.AIO_SET_DELEVEL);
				if (xpcur > xpres)
				{
					activeChar.getStat().removeExp(xpcur - xpres);
				}
			}
			if (Config.ALLOW_AIO_DUAL)
				activeChar.getInventory().destroyItemByItemId("AIOx", Config.AIO_DUAL_ID, 1, activeChar, null);
			
			activeChar.sendMessage("Your AIOx period has ended, removing status.");
		}
		else
		{
			_daysleft = (endDay - now) / 86400000;
			if (_daysleft > 0)
			{
				activeChar.sendMessage("Hello, " + activeChar.getName() + " you have " + (int) _daysleft + " days left for AIOx period end.");
			}
			else if (_daysleft < 1)
			{
				long hour = (endDay - now) / 3600000;
				activeChar.sendMessage("Hello, " + activeChar.getName() + " you have " + (int) hour + " hours left for Aio period ends");
			}
		}
	}
	
	private void onEnterVip(L2PcInstance activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getVipEndTime();
		if (now > endDay)
		{
			activeChar.setVip(false);
			activeChar.setVipEndTime(0);
			activeChar.sendMessage("Your VIP period has ended removing status.");
		}
		else
		{
			_daysleft = (endDay - now) / 86400000;
			if (_daysleft > 0)
			{
				activeChar.sendMessage("Hello, " + activeChar.getName() + " you have " + (int) _daysleft + " days left for Vip period ends");
			}
			else if (_daysleft < 1)
			{
				long hour = (endDay - now) / 3600000;
				activeChar.sendMessage("Hello, " + activeChar.getName() + " you have " + (int) hour + " hours left for Vip period ends");
			}
		}
	}
	
	private void onEnterNewbie(L2PcInstance activeChar)
	{
		if (Config.STARTUP_SYSTEM_ENABLED && !activeChar.isGM())
		{
			// make char disappears
			activeChar.getAppearance().setInvisible();
			activeChar.broadcastUserInfo();
			activeChar.decayMe();
			activeChar.spawnMe();
			sendPacket(new SpecialCamera(activeChar.getObjectId(), 30, 200, 20, 999999999, 999999999, 0, 0, 1, 0));
			StartupSystem.startSetup(activeChar);
		}
	}
	
}