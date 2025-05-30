/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.model.actor.instance;

import java.util.Set;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.xml.CharTemplateTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Clan.SubPledge;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.L2PledgeSkillLearn;
import com.dream.game.model.L2Skill;
import com.dream.game.model.base.ClassId;
import com.dream.game.model.base.ClassType;
import com.dream.game.model.base.PlayerClass;
import com.dream.game.model.base.Race;
import com.dream.game.model.base.SubClass;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.AcquireSkillDone;
import com.dream.game.network.serverpackets.AcquireSkillList;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.PledgeReceiveSubPledgeCreated;
import com.dream.game.network.serverpackets.PledgeShowMemberListAll;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;
import com.dream.game.util.Util;

public class L2VillageMasterInstance extends L2NpcInstance
{
	private static final void renameSubPledge(L2PcInstance player, int pledgeType, String pledgeName)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		final L2Clan clan = player.getClan();
		final SubPledge subPledge = player.getClan().getSubPledge(pledgeType);

		if (subPledge == null)
		{
			player.sendMessage("Pledge doesn't exist.");
			return;
		}

		if (!Util.isAlphaNumeric(pledgeName))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return;
		}

		if (pledgeName.length() < 2 || pledgeName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
			return;
		}

		subPledge.setName(pledgeName);
		clan.updateSubPledgeInDB(subPledge.getId());
		clan.broadcastToOnlineMembers(new PledgeShowMemberListAll(clan, subPledge.getId()));
		player.sendMessage("Pledge name have been changed to: " + pledgeName);
	}

	public L2VillageMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void assignSubPledgeLeader(L2PcInstance player, String clanName, String leaderName)
	{
		L2Clan clan = player.getClan();
		if (clan == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (leaderName.length() > 16)
		{
			player.sendPacket(SystemMessageId.NAMING_CHARNAME_UP_TO_16CHARS);
			return;
		}

		if (player.getName().equals(leaderName))
		{
			player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
			return;
		}

		SubPledge subPledge = player.getClan().getSubPledge(clanName);

		if (null == subPledge)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return;
		}

		if (subPledge.getId() == L2Clan.SUBUNIT_ACADEMY)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return;
		}

		L2PcInstance newLeader = L2World.getInstance().getPlayer(leaderName);
		if (newLeader == null || newLeader.getClan() == null || newLeader.getClan() != clan)
		{
			player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_NOT_FOUND_IN_CLAN), leaderName));
			return;
		}

		if (clan.getClanMember(leaderName) == null || clan.getClanMember(leaderName).getPledgeType() != 0)
		{
			if (subPledge.getId() >= L2Clan.SUBUNIT_KNIGHT1)
			{
				player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
			}
			else if (subPledge.getId() >= L2Clan.SUBUNIT_ROYAL1)
			{
				player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
			}
			return;
		}

		try
		{
			L2ClanMember oldLeader = clan.getClanMember(subPledge.getLeaderId());
			String oldLeaderName = oldLeader == null ? "" : oldLeader.getName();
			clan.getClanMember(oldLeaderName).setSubPledgeType(0);
			clan.getClanMember(oldLeaderName).updateSubPledgeType();
			clan.getClanMember(oldLeaderName).getPlayerInstance().setPledgeClass(L2ClanMember.getCurrentPledgeClass(clan.getClanMember(oldLeaderName).getPlayerInstance()));
			clan.getClanMember(oldLeaderName).getPlayerInstance().setActiveClass(0);
		}
		catch (Throwable t)
		{
		}

		int leaderId = clan.getClanMember(leaderName).getObjectId();

		subPledge.setLeaderId(leaderId);
		clan.updateSubPledgeInDB(subPledge.getId());
		L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
		leaderSubPledge.getPlayerInstance().setPledgeClass(L2ClanMember.getCurrentPledgeClass(leaderSubPledge.getPlayerInstance()));
		leaderSubPledge.getPlayerInstance().sendPacket(new UserInfo(leaderSubPledge.getPlayerInstance()));
		clan.broadcastClanStatus();
		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2).addString(leaderName).addString(clanName));
	}

	public void changeClanLeader(L2PcInstance player, String target)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (player.getName().equalsIgnoreCase(target))
			return;
		if (player.isFlying())
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_UNRIDE_PET));
			return;
		}
		L2Clan clan = player.getClan();
		if (SiegeManager.getSiege(clan) != null && SiegeManager.getSiege(clan).getIsInProgress())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("Can't do it while siege in progress"));
			return;
		}
		L2ClanMember member = clan.getClanMember(target);
		if (member == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DOES_NOT_EXIST).addString(target));
			return;
		}
		if (!member.isOnline())
		{
			player.sendPacket(SystemMessageId.INVITED_USER_NOT_ONLINE);
			return;
		}
		if (member.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER));
			return;
		}
		clan.setNewLeader(member);
	}

	public void changeSubPledge(L2Clan clan, SubPledge element, String newName)
	{
		if (newName.length() > 16 || newName.length() < 3)
		{
			clan.getLeader().getPlayerInstance().sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
			return;
		}
		String oldName = element.getName();
		element.setName(newName);
		clan.updateSubPledgeInDB(element.getId());
		for (L2ClanMember member : clan.getMembers())
		{
			if (member == null || member.getPlayerInstance() == null || member.getPlayerInstance().isOnline() == 0)
			{
				continue;
			}
			if (member.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
			{
				continue;
			}
			SubPledge[] subPledge = clan.getAllSubPledges();
			for (SubPledge sp : subPledge)
			{
				member.getPlayerInstance().sendPacket(new PledgeReceiveSubPledgeCreated(sp, clan));
			}
			if (member.getPlayerInstance() != null)
			{
				member.getPlayerInstance().sendMessage("Clan sub unit " + oldName + "'s name has been changed into " + newName + ".");
			}
		}
	}

	public void createSubPledge(L2PcInstance player, String clanName, String leaderName, int pledgeType, int minClanLvl)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		L2Clan clan = player.getClan();
		if (clan.getLevel() < minClanLvl)
		{
			if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY);
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT);
			}
			return;
		}
		if (!Util.isValidPlayerName(clanName))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return;
		}
		if (pledgeType != L2Clan.SUBUNIT_ACADEMY && clan.getClanMember(leaderName) == null)
			return;
		int leaderId = pledgeType != L2Clan.SUBUNIT_ACADEMY ? clan.getClanMember(leaderName).getObjectId() : 0;
		if (leaderId != 0 && clan.getLeaderSubPledge(leaderId) != 0)
		{
			player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME);
			return;
		}
		for (L2Clan tempClan : ClanTable.getInstance().getClans())
			if (tempClan.getSubPledge(clanName) != null)
			{
				if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(clanName));
				}
				else
				{
					player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME);
				}
				return;
			}
		if (pledgeType != L2Clan.SUBUNIT_ACADEMY)
			if (clan.getClanMember(leaderName) == null || clan.getClanMember(leaderName).getPledgeType() != 0)
			{
				if (pledgeType >= L2Clan.SUBUNIT_KNIGHT1)
				{
					player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
				}
				else if (pledgeType >= L2Clan.SUBUNIT_ROYAL1)
				{
					player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
				}
				return;
			}
		if (clan.createSubPledge(player, pledgeType, leaderId, clanName) == null)
			return;

		if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED).addString(player.getClan().getName()));
		}
		else if (pledgeType >= L2Clan.SUBUNIT_KNIGHT1)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED).addString(player.getClan().getName()));
		}
		else if (pledgeType >= L2Clan.SUBUNIT_ROYAL1)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED).addString(player.getClan().getName()));
		}
		else
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_CREATED));
		}

		if (pledgeType != L2Clan.SUBUNIT_ACADEMY)
		{
			L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
			if (leaderSubPledge.getPlayerInstance() == null)
				return;
			leaderSubPledge.getPlayerInstance().setPledgeClass(L2ClanMember.getCurrentPledgeClass(leaderSubPledge.getPlayerInstance()));
			leaderSubPledge.getPlayerInstance().sendPacket(new UserInfo(leaderSubPledge.getPlayerInstance()));
			try
			{
				clan.getClanMember(leaderName).updateSubPledgeType();
				for (L2Skill skill : leaderSubPledge.getPlayerInstance().getAllSkills())
				{
					leaderSubPledge.getPlayerInstance().removeSkill(skill, false);
				}
				clan.getClanMember(leaderName).getPlayerInstance().setActiveClass(0);
			}
			catch (Throwable t)
			{
			}

			for (L2ClanMember member : clan.getMembers())
			{
				if (member == null || member.getPlayerInstance() == null || member.getPlayerInstance().isOnline() == 0)
				{
					continue;
				}
				SubPledge[] subPledge = clan.getAllSubPledges();
				for (SubPledge element : subPledge)
				{
					member.getPlayerInstance().sendPacket(new PledgeReceiveSubPledgeCreated(element, clan));
				}
			}
		}
	}

	public void dissolveClan(L2PcInstance player, int clanId)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		L2Clan clan = player.getClan();
		if (clan.getAllyId() != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_DISPERSE_THE_CLANS_IN_ALLY);
			return;
		}
		if (clan.isAtWar())
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_WAR);
			return;
		}
		if (clan.getHasCastle() != 0 || clan.getHasHideout() != 0 || clan.getHasFort() != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE);
			return;
		}
		for (Castle castle : CastleManager.getInstance().getCastles().values())
			if (SiegeManager.getInstance().checkIsRegistered(clan, castle.getCastleId()))
			{
				player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
				return;
			}
		for (Fort fort : FortManager.getInstance().getForts())
			if (FortSiegeManager.getInstance().checkIsRegistered(clan, fort.getFortId()))
			{
				player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
				return;
			}
		if (SiegeManager.checkIfInZone(player))
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
			return;
		}
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.DISSOLUTION_IN_PROGRESS);
			return;
		}
		clan.setDissolvingExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_DISSOLVE_DAYS * 86400000L);
		clan.updateClanInDB();
		ClanTable.getInstance().scheduleRemoveClan(clan.getClanId());
		player.deathPenalty(false, false);
	}

	private final Set<PlayerClass> getAvailableSubClasses(L2PcInstance player)
	{
		int baseClassId = player.getBaseClass();
		if (baseClassId >= 88 && baseClassId <= 118)
		{
			baseClassId = ClassId.values()[baseClassId].getParent().getId();
		}

		PlayerClass baseClass = PlayerClass.values()[baseClassId];

		final Race npcRace = getVillageMasterRace();
		final ClassType npcTeachType = getVillageMasterTeachType();

		Set<PlayerClass> availSubs = baseClass.getAvailableSubclasses(player);

		if (availSubs != null && !availSubs.isEmpty())
		{
			for (PlayerClass availSub : availSubs)
			{
				for (SubClass subClass : player.getSubClasses().values())
				{
					int subClassId = subClass.getClassId();
					if (subClassId >= 88 && subClassId <= 118)
					{
						subClassId = ClassId.values()[subClassId].getParent().getId();
					}

					if (availSub.ordinal() == subClassId || availSub.ordinal() == baseClassId)
					{
						availSubs.remove(availSub);
					}
				}

				if (npcRace == Race.Human || npcRace == Race.Elf)
				{
					if (!availSub.isOfType(npcTeachType))
					{
						if (!Config.ALT_GAME_SUBCLASS_EVERYWHERE)
						availSubs.remove(availSub);
					}
					else if (!availSub.isOfRace(Race.Human) && !availSub.isOfRace(Race.Elf))
					{
						if (!Config.ALT_GAME_SUBCLASS_EVERYWHERE)
						availSubs.remove(availSub);
					}
				}
				else if (!availSub.isOfRace(npcRace))
				{
					if (!Config.ALT_GAME_SUBCLASS_EVERYWHERE)
					availSubs.remove(availSub);
				}
			}
		}
		return availSubs;
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";

		if (val == 0)
		{
			filename = "" + npcId;
		}
		else
		{
			filename = npcId + "-" + val;
		}

		return "data/html/villagemaster/" + filename + ".htm";
	}

	private final Race getVillageMasterRace()
	{
		return getTemplate().getNpcRace();
	}

	protected ClassType getVillageMasterTeachType()
	{
		return ClassType.Fighter;
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		String[] commandStr = command.split(" ");
		String actualCommand = commandStr[0];

		String cmdParams = "";
		String cmdParams2 = "";

		if (commandStr.length >= 2)
		{
			cmdParams = commandStr[1];
		}
		if (commandStr.length >= 3)
		{
			cmdParams2 = commandStr[2];
		}

		if (actualCommand.equalsIgnoreCase("create_clan"))
		{
			if (cmdParams.isEmpty())
				return;

			ClanTable.getInstance().createClan(player, command.substring(actualCommand.length()).trim());
		}
		else if (actualCommand.equalsIgnoreCase("create_academy"))
		{
			if (cmdParams.isEmpty())
				return;

			createSubPledge(player, cmdParams, null, -1, 5);
		}
		else if (actualCommand.equalsIgnoreCase("create_royal"))
		{
			if (cmdParams.isEmpty())
				return;

			createSubPledge(player, cmdParams, cmdParams2, 100, 6);
		}
		else if (actualCommand.equalsIgnoreCase("rename_pledge"))
		{
			if (cmdParams.isEmpty() || cmdParams2.isEmpty())
				return;

			renameSubPledge(player, Integer.valueOf(cmdParams), cmdParams2);
		}
		else if (actualCommand.equalsIgnoreCase("assign_subpl_leader"))
		{
			if (cmdParams.isEmpty())
				return;

			assignSubPledgeLeader(player, cmdParams, cmdParams2);
		}
		else if (actualCommand.equalsIgnoreCase("create_knight"))
		{
			if (cmdParams.isEmpty())
				return;

			createSubPledge(player, cmdParams, cmdParams2, 1001, 7);
		}
		else if (actualCommand.equalsIgnoreCase("create_ally"))
		{
			if (cmdParams.isEmpty())
				return;

			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
				return;
			}
			player.getClan().createAlly(player, command.substring(actualCommand.length()).trim());
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
				return;
			}
			player.getClan().dissolveAlly(player);
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_clan"))
		{
			dissolveClan(player, player.getClanId());
		}
		else if (actualCommand.equalsIgnoreCase("change_clan_leader"))
		{
			if (cmdParams.isEmpty())
				return;

			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}

			if (player.getName().equalsIgnoreCase(cmdParams))
				return;

			final L2Clan clan = player.getClan();
			final L2ClanMember member = clan.getClanMember(cmdParams);
			if (member == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DOES_NOT_EXIST).addString(cmdParams));
				return;
			}

			if (!member.isOnline())
			{
				player.sendPacket(SystemMessageId.INVITED_USER_NOT_ONLINE);
				return;
			}

			// To avoid clans with null clan leader, academy members shouldn't be eligible for clan leader.
			if (member.getPlayerInstance().isAcademyMember())
			{
				player.sendPacket(SystemMessageId.RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER);
				return;
			}

			if (Config.ALT_CLAN_LEADER_INSTANT_ACTIVATION)
			{
				clan.setNewLeader(member);
			}
			else
			{
				final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				if (clan.getNewLeaderId() == 0)
				{
					clan.setNewLeaderId(member.getObjectId(), true);
					msg.setFile("data/scripts/village_master/9000_clan/9000-07-success.htm");
				}
				else
				{
					msg.setFile("data/scripts/village_master/9000_clan/9000-07-in-progress.htm");
				}
				player.sendPacket(msg);
			}
		}
		else if (actualCommand.equalsIgnoreCase("cancel_clan_leader_change"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}

			final L2Clan clan = player.getClan();
			final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
			if (clan.getNewLeaderId() != 0)
			{
				clan.setNewLeaderId(0, true);
				msg.setFile("data/scripts/village_master/9000_clan/9000-07-canceled.htm");
			}
			else
			{
				msg.setHtml("<html><body>You don't have clan leader delegation applications submitted yet!</body></html>");
			}

			player.sendPacket(msg);
		}
		else if (actualCommand.equalsIgnoreCase("recover_clan"))
		{
			recoverClan(player, player.getClanId());
		}
		else if (actualCommand.equalsIgnoreCase("increase_clan_level"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			player.getClan().levelUpClan(player);
		}
		else if (actualCommand.equalsIgnoreCase("learn_clan_skills"))
		{
			showPledgeSkillList(player);
		}
		else if (command.contains("Link villagemaster/SubClass.htm"))
		{
			String html = "";
			html = HtmCache.getInstance().getHtm("data/html/villagemaster/SubClass.htm");
			NpcHtmlMessage subMsg = new NpcHtmlMessage(getObjectId());
			subMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(getObjectId())));
			player.sendPacket(subMsg);
		}
		else if (command.startsWith("Subclass"))
		{
			int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());

			if (Olympiad.isRegistered(player) || Olympiad.isRegisteredInComp(player))
			{
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_YOU_IN_OLYMPIAD));
				return;
			}
			if (player.isCastingNow() || player.isAllSkillsDisabled())
			{
				player.sendPacket(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
				return;
			}
			if (player.getPet() != null)
			{
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_UNSUMMON_YOUR_PET));
				return;
			}

			StringBuilder content = new StringBuilder("<html><body>");
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			Set<PlayerClass> subsAvailable;

			int paramOne = 0;
			int paramTwo = 0;

			try
			{
				int endIndex = command.indexOf(' ', 11);
				if (endIndex == -1)
				{
					endIndex = command.length();
				}

				paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
				if (command.length() > endIndex)
				{
					paramTwo = Integer.parseInt(command.substring(endIndex).trim());
				}
			}
			catch (Exception NumberFormatException)
			{
			}

			switch (cmdChoice)
			{
				case 1:
					if (player.getTotalSubClasses() == Config.MAX_SUBCLASS)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_MAX_SUB_CLASS));
						return;
					}
					if (player.getPet() != null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_UNSUMMON_YOUR_PET));
						return;
					}
					subsAvailable = getAvailableSubClasses(player);
					if (subsAvailable != null && !subsAvailable.isEmpty())
					{
						content.append("Add sub class:<br>A sub class you want to add?<br>");
						for (PlayerClass subClass : subsAvailable)
						{
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 4 " + subClass.ordinal() + "\" msg=\"1268;" + CharTemplateTable.getClassNameById(subClass.ordinal()) + "\">" + CharTemplateTable.getClassNameById(subClass.ordinal()) + "</a><br>");
						}
					}
					else
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_SUB_CLASS));
						return;
					}
					break;
				case 2:
					if (player.getPet() != null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_UNSUMMON_YOUR_PET));
						return;
					}
					content.append("Add sub class:<br>");
					final int baseClassId = player.getBaseClass();
					if (player.getSubClasses().isEmpty())
					{
						content.append("You must add at least 1 sub-class for its change.<br>" + "<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 1\">Add sub class.</a>");
					}
					else
					{
						content.append("What kind of sub-class would like to choose?<br>");
						if (baseClassId == player.getActiveClass())
						{
							content.append(CharTemplateTable.getClassNameById(baseClassId) + "&nbsp;<font color=\"LEVEL\">(The main class)</font><br>");
						}
						else
						{
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 0\">" + CharTemplateTable.getClassNameById(baseClassId) + "</a>&nbsp;" + "<font color=\"LEVEL\">(The main class)</font><br>");
						}

						for (SubClass subClass : player.getSubClasses().values())
						{
							int subClassId = subClass.getClassId();
							if (subClassId == player.getActiveClass())
							{
								content.append(CharTemplateTable.getClassNameById(subClassId) + "<br>");
							}
							else
							{
								content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " + subClass.getClassIndex() + "\">" + CharTemplateTable.getClassNameById(subClassId) + "</a><br>");
							}
						}
					}
					break;
				case 3:
					if (player.getPet() != null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_UNSUMMON_YOUR_PET));
						return;
					}
					content.append("Change the sub class:<br>Some of the sub class you would like to change?<br>");
					int classIndex = 1;
					for (SubClass subClass : player.getSubClasses().values())
					{
						content.append("Subclasse " + classIndex++ + "<br1>");
						content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 6 " + subClass.getClassIndex() + "\">" + CharTemplateTable.getClassNameById(subClass.getClassId()) + "</a><br>");
					}
					content.append("<br>Your new sub-class will receive 40 level and 2 skills.");
					break;
				case 4:
					if (player.getPet() != null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_UNSUMMON_YOUR_PET));
						return;
					}
					boolean allowAddition = true;
					if (!FloodProtector.tryPerformAction(player, Protected.SUBCLASS))
					{
						StringBuilder text = new StringBuilder();
						text.append("<html><head><body><font color=\"LEVEL\">The message Server:</font><br>Wait a few seconds.</body></html>");
						html.setHtml(text.toString());
						player.sendPacket(html);
						return;
					}
					if (player.getLevel() < 75)
					{
						html.setFile("data/html/villagemaster/SubClass_Fail.htm");
						allowAddition = false;
					}
					if (player._event != null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
						return;
					}
					if (allowAddition)
						if (!player.getSubClasses().isEmpty())
						{
							for (SubClass subClass : player.getSubClasses().values())
								if (subClass.getLevel() < 75)
								{
									html.setFile("data/html/villagemaster/SubClass_Fail.htm");
									allowAddition = false;
									break;
								}
						}
					if (Config.SUBCLASS_WITH_CUSTOM_ITEM)
					{
						if (Config.SUBCLASS_WITH_CUSTOM_ITEM_COUNT >= 0)
							if (Config.SUBCLASS_WITH_CUSTOM_ITEM_COUNT > 0)
							{
								if (player.getInventory().getInventoryItemCount(Config.SUBCLASS_WITH_CUSTOM_ITEM_ID, -1) < Config.SUBCLASS_WITH_CUSTOM_ITEM_COUNT)
								{
									player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
									return;
								}
								else if (allowAddition)
								{
									player.destroyItemByItemId("ServiceManager", Config.SUBCLASS_WITH_CUSTOM_ITEM_ID, Config.SUBCLASS_WITH_CUSTOM_ITEM_COUNT, player, true);
								}
							}
							else if (player.getInventory().getItemByItemId(Config.SUBCLASS_WITH_CUSTOM_ITEM_ID) == null)
							{
								player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
								return;
							}
					}
					else if (Config.SUBCLASS_WITH_ITEM_AND_NO_QUEST)
					{
						L2ItemInstance elixirItem = player.getInventory().getItemByItemId(6319);
						L2ItemInstance destinyItem = player.getInventory().getItemByItemId(5011);
						if (elixirItem == null)
						{
							player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_NEED_ITEM), "\"Mimir's Elixir\""));
							return;
						}
						if (destinyItem == null)
						{
							player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_NEED_ITEM), "\"Star of Destiny\""));
							return;
						}
						if (allowAddition)
						{
							// player.destroyItemByItemId("Quest", 6319, 1, this,
							// true);
							// player.destroyItemByItemId("Quest", 5011, 1, this,
							// true);
						}
					}
					else if (!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
					{
						/* QuestState qs = player.getQuestState("234_FatesWhisper"); if (qs == null || !qs.isCompleted()) { html.setFile("data/html/villagemaster/SubClass_Fail.htm" ); player.sendPacket(html); return; } */
						QuestState qs = player.getQuestState("235_MimirsElixir");
						if (qs == null || !qs.isCompleted())
						{
							html.setFile("data/html/villagemaster/SubClass_Fail.htm");
							player.sendPacket(html);
							return;
						}
					}
					if (allowAddition)
					{
						if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1))
						{
							player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CANT_ADD_SUB));
							return;
						}
						player.setActiveClass(player.getTotalSubClasses());
						content.append("Sub class added:<br>Congrats! You have added a new sub-class. <br>, open (ALT + T) for persuasion.");
						player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS);
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						html.setFile("data/html/villagemaster/SubClass_Fail.htm");
					}
					break;
				case 5:
					if (player.getPet() != null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_UNSUMMON_YOUR_PET));
						return;
					}
					if (player._event != null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
						return;
					}
					if (!FloodProtector.tryPerformAction(player, Protected.SUBCLASS))
					{
						StringBuilder text = new StringBuilder();
						text.append("<html><head><body><font color=\"LEVEL\">The message Server:</font><br>Wait for a few seconds.</body></html>");
						html.setHtml(text.toString());
						player.sendPacket(html);
						return;
					}
					player.stopAllEffects();
					player.clearCharges();
					player.setActiveClass(paramOne);
					content.append("Sub class changed:<br>Your current sub class <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getActiveClass()) + "</font>.");
					player.sendPacket(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED);
					if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
					{
						player.checkAllowedSkills();
					}
					break;
				case 6:
					if (player.getPet() != null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_UNSUMMON_YOUR_PET));
						return;
					}
					content.append("Select sub class for a change. If the desired class here there is no, " + "find the appropriate wizard.<br>" + "<font color=\"LEVEL\">Attention!</font> All information of this class will be removed.<br><br>");

					subsAvailable = getAvailableSubClasses(player);
					if (subsAvailable != null && !subsAvailable.isEmpty())
					{
						for (PlayerClass subClass : subsAvailable)
						{
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 7 " + paramOne + " " + subClass.ordinal() + "\">" + CharTemplateTable.getClassNameById(subClass.ordinal()) + "</a><br>");
						}
					}
					else
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_SUB_CLASS));
						return;
					}
					break;
				case 7:
					if (player.getPet() != null)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_UNSUMMON_YOUR_PET));
						return;
					}
					if (!FloodProtector.tryPerformAction(player, Protected.SUBCLASS))
					{
						StringBuilder text = new StringBuilder();
						text.append("<html><head><body><font color=\"LEVEL\">The message Server:</font><br>Wait for a few seconds.</body></html>");
						html.setHtml(text.toString());
						player.sendPacket(html);
						return;
					}
					if (player.modifySubClass(paramOne, paramTwo))
					{
						player.stopAllEffects();
						player.clearCharges();
						player.setActiveClass(paramOne);
						content.append("Change the sub class:<br>Your class has been changed to <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(paramTwo) + "</font>.");
						player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS);
						player.sendPacket(ActionFailed.STATIC_PACKET);
						if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
						{
							player.checkAllowedSkills();
						}
					}
					else
					{
						player.setActiveClass(0);
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CANT_ADD_SUB));
						return;
					}
					break;
			}
			content.append("</body></html>");
			if (content.length() > 26)
			{
				html.setHtml(content.toString());
			}
			player.sendPacket(html);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	public void recoverClan(L2PcInstance player, int clanId)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		L2Clan clan = player.getClan();
		clan.setDissolvingExpiryTime(0);
		clan.updateClanInDB();
	}

	public void renameSubPledge(L2PcInstance player, String newName, String command)
	{
		if (player == null || player.getClan() == null || !player.isClanLeader())
		{
			if (player != null)
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			}
			return;
		}
		L2Clan clan = player.getClan();
		SubPledge[] subPledge = clan.getAllSubPledges();
		for (SubPledge element : subPledge)
		{
			switch (element.getId())
			{
				case 100:
					if (command.equalsIgnoreCase("rename_royal1"))
					{
						changeSubPledge(clan, element, newName);
						return;
					}
					break;
				case 200:
					if (command.equalsIgnoreCase("rename_royal2"))
					{
						changeSubPledge(clan, element, newName);
						return;
					}
					break;
				case 1001:
					if (command.equalsIgnoreCase("rename_knights1"))
					{
						changeSubPledge(clan, element, newName);
						return;
					}
					break;
				case 1002:
					if (command.equalsIgnoreCase("rename_knights2"))
					{
						changeSubPledge(clan, element, newName);
						return;
					}
					break;
				case 2001:
					if (command.equalsIgnoreCase("rename_knights3"))
					{
						changeSubPledge(clan, element, newName);
						return;
					}
					break;
				case 2002:
					if (command.equalsIgnoreCase("rename_knights4"))
					{
						changeSubPledge(clan, element, newName);
						return;
					}
					break;
			}
		}
		player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_ERROR_CONTACT_GM));
	}

	public void showPledgeSkillList(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		if (player.getClan() == null || !player.isClanLeader())
		{
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append("<br><br>You have no right to learn clan skills.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Clan);
		int counts = 0;

		for (L2PledgeSkillLearn s : skills)
		{
			int cost = s.getRepCost();
			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if (counts == 0)
		{
			if (player.getClan().getLevel() < 8)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_REACHED_S1);
				if (player.getClan().getLevel() < 5)
				{
					sm.addNumber(5);
				}
				else
				{
					sm.addNumber(player.getClan().getLevel() + 1);
				}
				player.sendPacket(sm);
				player.sendPacket(new AcquireSkillDone());
			}
			else
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN));
			}
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}