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
package com.dream.game.model;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.communitybbs.BB.Forum;
import com.dream.game.communitybbs.Manager.ForumsBBSManager;
import com.dream.game.datatables.sql.CharNameTable;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.xml.ClanLeveLUpPricesData;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CrownManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.itemcontainer.ClanWarehouse;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.PledgeReceiveSubPledgeCreated;
import com.dream.game.network.serverpackets.PledgeShowInfoUpdate;
import com.dream.game.network.serverpackets.PledgeShowMemberListAll;
import com.dream.game.network.serverpackets.PledgeShowMemberListDeleteAll;
import com.dream.game.network.serverpackets.PledgeShowMemberListUpdate;
import com.dream.game.network.serverpackets.PledgeSkillList;
import com.dream.game.network.serverpackets.PledgeSkillListAdd;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.util.PcAction;
import com.dream.game.util.Util;
import com.dream.util.LinkedBunch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class L2Clan
{
	public class RankPrivs
	{
		private final int _rankId;
		private final int _party;
		private int _rankPrivs;

		public RankPrivs(int rank, int party, int privs)
		{
			_rankId = rank;
			_party = party;
			_rankPrivs = privs;
		}

		public int getParty()
		{
			return _party;
		}

		public int getPrivs()
		{
			return _rankPrivs;
		}

		public int getRank()
		{
			return _rankId;
		}

		public void setPrivs(int privs)
		{
			_rankPrivs = privs;
		}
	}

	public class SubPledge
	{
		private final int _id;
		private String _subPledgeName;
		private int _leaderId;

		public SubPledge(int id, String name, int leaderId)
		{
			_id = id;
			_subPledgeName = name;
			_leaderId = leaderId;
		}

		public int getId()
		{
			return _id;
		}

		public int getLeaderId()
		{
			return _leaderId;
		}

		public String getName()
		{
			return _subPledgeName;
		}

		public void setLeaderId(int leaderId)
		{
			_leaderId = leaderId;
		}

		public void setName(String newName)
		{
			_subPledgeName = newName;
		}
	}

	private static final Logger _log = Logger.getLogger(L2Clan.class.getName());
	public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
	public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;

	public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
	public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
	public static final int CP_NOTHING = 0;
	public static final int CP_CL_JOIN_CLAN = 2;
	public static final int CP_CL_GIVE_TITLE = 4;
	public static final int CP_CL_VIEW_WAREHOUSE = 8;
	public static final int CP_CL_MANAGE_RANKS = 16;
	public static final int CP_CL_PLEDGE_WAR = 32;
	public static final int CP_CL_DISMISS = 64;
	public static final int CP_CL_REGISTER_CREST = 128;
	public static final int CP_CL_MASTER_RIGHTS = 256;
	public static final int CP_CL_MANAGE_LEVELS = 512;

	public static final int CP_CH_OPEN_DOOR = 1024;
	public static final int CP_CH_OTHER_RIGHTS = 2048;
	public static final int CP_CH_AUCTION = 4096;
	public static final int CP_CH_DISMISS = 8192;
	public static final int CP_CH_SET_FUNCTIONS = 16384;
	public static final int CP_CS_OPEN_DOOR = 32768;
	public static final int CP_CS_MANOR_ADMIN = 65536;
	public static final int CP_CS_MANAGE_SIEGE = 131072;

	public static final int CP_CS_USE_FUNCTIONS = 262144;
	public static final int CP_CS_DISMISS = 524288;
	public static final int CP_CS_TAXES = 1048576;
	public static final int CP_CS_MERCENARIES = 2097152;

	public static final int CP_CS_SET_FUNCTIONS = 4194304;

	public static final int CP_ALL = 8388606;
	public static final int SUBUNIT_ACADEMY = -1;

	public static final int SUBUNIT_ROYAL1 = 100;
	public static final int SUBUNIT_ROYAL2 = 200;
	public static final int SUBUNIT_KNIGHT1 = 1001;
	public static final int SUBUNIT_KNIGHT2 = 1002;
	public static final int SUBUNIT_KNIGHT3 = 2001;
	public static final int SUBUNIT_KNIGHT4 = 2002;
	private String _name;
	private int _clanId;
	private L2ClanMember _leader;
	private final Map<Integer, L2ClanMember> _members = new HashMap<>();
	private String _allyName;
	private int _allyId;
	private int _level;
	private int _hasCastle;
	private int _hasHideout;
	private int _hasFort;
	private boolean _hasCrest;
	private int _hiredGuards;
	private int _crestId;
	private int _crestLargeId;
	private int _allyCrestId;
	private int _auctionBiddedAt = 0;
	private long _allyPenaltyExpiryTime;
	private int _allyPenaltyType;

	private long _charPenaltyExpiryTime;
	private long _dissolvingExpiryTime;
	private final ClanWarehouse _warehouse = new ClanWarehouse(this);
	private final List<Integer> _atWarWith = new ArrayList<>();
	private final List<Integer> _atWarAttackers = new ArrayList<>();
	private final Map<Integer, Long> _warPenaltyExpiryTime = new HashMap<>();
	private boolean _hasCrestLarge;

	private final List<L2Skill> _skillList = new ArrayList<>();
	private Forum _forum;
	protected final Map<Integer, L2Skill> _skills = new HashMap<>();

	protected final Map<Integer, RankPrivs> _privs = new HashMap<>();
	protected final Map<Integer, SubPledge> _subPledges = new HashMap<>();

	private int _reputationScore = 0;
	private int _rank = 0;

	private String _notice;

	private int _newLeaderId;

	public L2Clan(int clanId)
	{
		_clanId = clanId;
		initializePrivs();
		restore();
		getWarehouse().restore();
		if (getNotice() == "")
		{
			insertNotice();
		}
	}

	public L2Clan(int clanId, String clanName)
	{
		_clanId = clanId;
		_name = clanName;
		initializePrivs();
	}

	private void addClanMember(L2ClanMember member)
	{
		_members.put(member.getObjectId(), member);
	}

	public void addClanMember(L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(this, player);
		addClanMember(member);
		member.setPlayerInstance(player);
		player.setClan(this);
		player.setPledgeClass(L2ClanMember.calculatePledgeClass(player));

		for (Siege siege : SiegeManager.getSieges())
		{
			if (!siege.getIsInProgress())
			{
				continue;
			}

			if (siege.checkIsAttacker(this))
			{
				player.setSiegeState((byte) 1);
			}
			else if (siege.checkIsDefender(this))
			{
				player.setSiegeState((byte) 2);
			}
		}

		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new PledgeSkillList(this));
		addSkillEffects(true);
		player.rewardSkills();

		member = null;
	}

	public L2Skill addNewSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		Connection con = null;

		if (newSkill != null)
		{
			oldSkill = _skills.put(newSkill.getId(), newSkill);

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement;

				if (oldSkill != null)
				{
					statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
					statement.setInt(1, newSkill.getLevel());
					statement.setInt(2, oldSkill.getId());
					statement.setInt(3, getClanId());
					statement.execute();
					statement.close();
				}
				else
				{
					statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name) VALUES (?,?,?,?)");
					statement.setInt(1, getClanId());
					statement.setInt(2, newSkill.getId());
					statement.setInt(3, newSkill.getLevel());
					statement.setString(4, newSkill.getName());
					statement.execute();
					statement.close();
				}
			}
			catch (Exception e)
			{
				_log.error("Error saving clan skills.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			addSkillEffects(true);
		}

		return oldSkill;
	}

	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;

		if (newSkill != null)
		{
			oldSkill = addSkill(newSkill);
		}

		return oldSkill;
	}

	public void addSkillEffects(boolean notify)
	{
		if (_skills.size() < 1)
			return;

		for (L2ClanMember temp : _members.values())
			if (temp != null)
				if (temp.isOnline() && temp.getPlayerInstance() != null)
				{
					addSkillEffects(temp.getPlayerInstance(), notify);
				}
	}

	public void addSkillEffects(L2PcInstance cm, boolean notify)
	{
		if (cm == null)
			return;

		if (cm.isClanLeader() && getLevel() >= Config.SIEGE_CLAN_MIN_LEVEL)
		{
			SiegeManager.addSiegeSkills(cm);
		}

		for (L2Skill skill : _skills.values())
			if (skill.getMinPledgeClass() <= cm.getPledgeClass())
			{
				cm.addSkill(skill, false);
				if (notify)
				{
					cm.sendPacket(new PledgeSkillListAdd(skill.getId(), skill.getLevel()));
				}
			}
	}

	public void addWarPenaltyTime(int clanId, long expiryTime)
	{
		_warPenaltyExpiryTime.put(clanId, expiryTime);
	}

	public void broadcastClanStatus()
	{
		for (L2PcInstance member : getOnlineMembers())
		{
			member.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
			member.sendPacket(new PledgeShowMemberListAll(this, 0));

			for (SubPledge sp : getAllSubPledges())
			{
				member.sendPacket(new PledgeShowMemberListAll(this, sp.getId()));
			}

			member.sendPacket(new UserInfo(member));
		}
	}

	public void broadcastCreatureSayToOnlineMembers(CreatureSay packet, L2PcInstance broadcaster)
	{
		for (L2ClanMember member : _members.values())
			if (member.isOnline() && member.getPlayerInstance() != null && !(Config.REGION_CHAT_ALSO_BLOCKED && BlockList.isBlocked(member.getPlayerInstance(), broadcaster)))
			{
				member.getPlayerInstance().sendPacket(packet);
			}
	}

	public void broadcastSnoopToOnlineAllyMembers(int type, String name, String text)
	{
		for (L2Clan clan : ClanTable.getInstance().getClanAllies(getAllyId()))
		{
			clan.broadcastSnoopToOnlineMembers(type, name, text);
		}
	}

	public void broadcastSnoopToOnlineMembers(int type, String name, String text)
	{
		for (L2ClanMember member : _members.values())
		{
			if (member == null || !member.isOnline())
			{
				continue;
			}

			L2PcInstance pl = member.getPlayerInstance();
			if (pl != null)
			{
				pl.broadcastSnoop(type, name, text);
			}
		}
	}

	public void broadcastToOnlineAllyMembers(L2GameServerPacket packet)
	{
		for (L2Clan clan : ClanTable.getInstance().getClanAllies(_allyId))
		{
			clan.broadcastToOnlineMembers(packet);
		}
	}

	public void broadcastToOnlineMembers(L2GameServerPacket... packets)
	{
		for (L2ClanMember member : _members.values())
			if (member != null && member.isOnline())
			{
				for (L2GameServerPacket packet : packets)
				{
					member.getPlayerInstance().sendPacket(packet);
				}
			}
	}

	public void broadcastToOnlineMembers(L2GameServerPacket packet)
	{
		for (L2ClanMember member : _members.values())
		{
			if (member == null)
			{
				continue;
			}

			if (member.isOnline() && member.getPlayerInstance() != null)
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}

	public void broadcastToOtherOnlineMembers(L2GameServerPacket packet, L2PcInstance player)
	{
		for (L2ClanMember member : _members.values())
			if (member != null && member.isOnline() && member.getPlayerInstance() != player)
			{
				member.getPlayerInstance().sendPacket(packet);
			}
	}

	
	public void changeLevel(int level)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
			statement.setInt(1, level);
			statement.setInt(2, getClanId());
			statement.execute();
			statement.close();

			con.close();
		}
		catch (Exception e)
		{
			_log.warn("could not increase clan level:" + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		setLevel(level);

		if (getLeader().isOnline())
		{
			L2PcInstance leader = getLeader().getPlayerInstance();
			if (level >= Config.SIEGE_CLAN_MIN_LEVEL)
			{
				SiegeManager.addSiegeSkills(leader);
			}
			else
			{
				SiegeManager.removeSiegeSkills(leader);
			}
			if (level > 4)
			{
				leader.sendPacket(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
			}
		}

		broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED));
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
	}

	public boolean checkAllyJoinCondition(L2PcInstance activeChar, L2PcInstance target)
	{
		if (activeChar == null)
			return false;

		if (activeChar.getAllyId() == 0 || !activeChar.isClanLeader() || activeChar.getClanId() != activeChar.getAllyId())
		{
			activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return false;
		}
		L2Clan leaderClan = activeChar.getClan();
		if (leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
			if (leaderClan.getAllyPenaltyType() == PENALTY_TYPE_DISMISS_CLAN)
			{
				activeChar.sendPacket(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY);
				return false;
			}
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if (activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
			return false;
		}
		if (target.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
			return false;
		}
		if (!target.isClanLeader())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(target.getName()));
			return false;
		}
		L2Clan targetClan = target.getClan();
		if (target.getAllyId() != 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE).addString(targetClan.getName()).addString(targetClan.getAllyName()));
			return false;
		}
		if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_LEAVED)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY).addString(target.getClan().getName()).addString(target.getClan().getAllyName()));
				return false;
			}
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_DISMISSED)
			{
				activeChar.sendPacket(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
				return false;
			}
		}
		if (SiegeManager.checkIfInZone(activeChar) && SiegeManager.checkIfInZone(target))
		{
			activeChar.sendPacket(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE);
			return false;
		}
		if (leaderClan.isAtWarWith(targetClan.getClanId()))
		{
			activeChar.sendPacket(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE);
			return false;
		}

		if (ClanTable.getInstance().getClanAllies(activeChar.getAllyId()).size() >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
			return false;
		}

		return true;
	}

	public boolean checkClanJoinCondition(L2PcInstance activeChar, L2PcInstance target, int pledgeType)
	{
		if (activeChar == null)
			return false;

		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_JOIN_CLAN) != L2Clan.CP_CL_JOIN_CLAN)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if (activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
			return false;
		}
		if (getCharPenaltyExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
			return false;
		}
		if (target.getClanId() != 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN).addString(target.getName()));
			return false;
		}
		if (target.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN).addString(target.getName()));
			return false;
		}
		if ((target.getLevel() > 40 || target.getClassId().level() >= 2) && pledgeType == -1)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY).addString(target.getName()));
			activeChar.sendPacket(SystemMessageId.ACADEMY_REQUIREMENTS);
			return false;
		}
		if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType))
		{
			if (pledgeType == 0)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_FULL).addString(getName()));
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.SUBCLAN_IS_FULL);
			}

			return false;
		}
		return true;
	}

	public void createAlly(L2PcInstance player, String allyName)
	{
		if (null == player)
			return;

		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.info(player.getObjectId() + "(" + player.getName() + ") requested ally creation from ");
		}

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
			return;
		}
		if (getAllyId() != 0)
		{
			player.sendPacket(SystemMessageId.ALREADY_JOINED_ALLIANCE);
			return;
		}
		if (getLevel() < 5)
		{
			player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			return;
		}
		if (getAllyPenaltyExpiryTime() > System.currentTimeMillis())
			if (getAllyPenaltyType() == L2Clan.PENALTY_TYPE_DISSOLVE_ALLY)
			{
				player.sendPacket(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION);
				return;
			}
		if (getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING);
			return;
		}
		if (allyName.length() > 16 || allyName.length() < 3)
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH);
			return;
		}
		if (!Util.isValidPlayerName(allyName))
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME);
			return;
		}
		if (ClanTable.getInstance().isAllyExists(allyName))
		{
			player.sendPacket(SystemMessageId.ALLIANCE_ALREADY_EXISTS);
			return;
		}

		setAllyId(getClanId());
		setAllyName(allyName.trim());
		setAllyPenaltyExpiryTime(0, 0);
		updateClanInDB();

		player.sendPacket(new UserInfo(player));

		player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_CREATE_ALLIANCE), allyName));
	}

	
	public SubPledge createSubPledge(L2PcInstance player, int subPledgeType, int leaderId, String subPledgeName)
	{
		final int originalSubPledgeType = subPledgeType;
		SubPledge subPledge = null;
		subPledgeType = getAvailablePledgeTypes(subPledgeType);
		if (subPledgeType == 0)
		{
			if (originalSubPledgeType == L2Clan.SUBUNIT_ACADEMY)
			{
				player.sendPacket(SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
			}
			else
			{
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CANT_CREATE_SUB_UNITS));
			}
			return null;
		}
		if (_leader.getObjectId() == leaderId)
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_LEADER_IS_INCORECT));
			return null;
		}

		int neededRepu = 0;
		if (subPledgeType != -1)
			if (subPledgeType < L2Clan.SUBUNIT_KNIGHT1)
			{
				neededRepu = 5000;
			}
			else if (subPledgeType > L2Clan.SUBUNIT_ROYAL2)
			{
				neededRepu = 10000;
			}

		if (getReputationScore() < neededRepu)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_REPUTATION_SCORE_IS_TOO_LOW));
			return null;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_id) values (?,?,?,?)");
			statement.setInt(1, getClanId());
			statement.setInt(2, subPledgeType);
			statement.setString(3, subPledgeName);
			if (subPledgeType != -1)
			{
				statement.setInt(4, leaderId);
			}
			else
			{
				statement.setInt(4, 0);
			}
			statement.execute();
			statement.close();

			subPledge = new SubPledge(subPledgeType, subPledgeName, leaderId);
			_subPledges.put(subPledgeType, subPledge);

			if (subPledgeType != -1)
			{
				setReputationScore(getReputationScore() - neededRepu, true);
			}

			if (_log.isDebugEnabled())
			{
				_log.debug("New sub_clan saved in db: " + getClanId() + "; " + subPledgeType);
			}
		}
		catch (Exception e)
		{
			_log.error("Error saving sub clan data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		broadcastToOnlineMembers(new PledgeShowInfoUpdate(_leader.getClan()));
		broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge, _leader.getClan()));
		return subPledge;
	}

	public void deleteAttackerClan(int clanId)
	{
		_atWarAttackers.remove(Integer.valueOf(clanId));
	}

	public void deleteEnemyClan(int clanId)
	{
		_atWarWith.remove(Integer.valueOf(clanId));
	}

	public void dissolveAlly(L2PcInstance player)
	{
		if (getAllyId() == 0)
		{
			player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		if (!player.isClanLeader() || getClanId() != getAllyId())
		{
			player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}
		if (SiegeManager.checkIfInZone(player))
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE);
			return;
		}

		broadcastToOnlineAllyMembers(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_DISOLVED));

		long currentTime = System.currentTimeMillis();
		for (L2Clan clan : ClanTable.getInstance().getClanAllies(getAllyId()))
			if (clan.getClanId() != getClanId())
			{
				clan.setAllyId(0);
				clan.setAllyName(null);
				clan.setAllyPenaltyExpiryTime(0, 0);
				clan.updateClanInDB();
			}

		setAllyId(0);
		setAllyName(null);
		setAllyPenaltyExpiryTime(currentTime + Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000L, L2Clan.PENALTY_TYPE_DISSOLVE_ALLY); // 24*60*60*1000 = 86400000
		updateClanInDB();

		player.deathPenalty(false, false);
	}

	public final RankPrivs[] getAllRankPrivs()
	{
		if (_privs == null)
			return new RankPrivs[0];

		return _privs.values().toArray(new RankPrivs[_privs.values().size()]);
	}

	public final L2Skill[] getAllSkills()
	{
		if (_skills == null)
			return new L2Skill[0];

		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}

	public final SubPledge[] getAllSubPledges()
	{
		if (_subPledges == null)
			return new SubPledge[0];

		return _subPledges.values().toArray(new SubPledge[_subPledges.values().size()]);
	}

	public int getAllyCrestId()
	{
		return _allyCrestId;
	}

	public int getAllyId()
	{
		return _allyId;
	}

	public String getAllyName()
	{
		return _allyName;
	}

	public long getAllyPenaltyExpiryTime()
	{
		return _allyPenaltyExpiryTime;
	}

	public int getAllyPenaltyType()
	{
		return _allyPenaltyType;
	}

	public List<Integer> getAttackerList()
	{
		return _atWarAttackers;
	}

	public int getAuctionBiddedAt()
	{
		return _auctionBiddedAt;
	}

	public int getAvailablePledgeTypes(int pledgeType)
	{
		if (_subPledges.get(pledgeType) != null)
		{
			switch (pledgeType)
			{
				case SUBUNIT_ACADEMY:
					return 0;
				case SUBUNIT_ROYAL1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
					break;
				case SUBUNIT_ROYAL2:
					return 0;
				case SUBUNIT_KNIGHT1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
					break;
				case SUBUNIT_KNIGHT2:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
					break;
				case SUBUNIT_KNIGHT3:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
					break;
				case SUBUNIT_KNIGHT4:
					return 0;
			}
		}
		return pledgeType;
	}

	public long getCharPenaltyExpiryTime()
	{
		return _charPenaltyExpiryTime;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public L2ClanMember getClanMember(int objectId)
	{
		return _members.get(objectId);
	}

	public L2ClanMember getClanMember(String name)
	{
		for (L2ClanMember temp : _members.values())
			if (temp.getName().equalsIgnoreCase(name))
				return temp;
		return null;
	}

	public int getCrestId()
	{
		return _crestId;
	}

	public int getCrestLargeId()
	{
		return _crestLargeId;
	}

	public long getDissolvingExpiryTime()
	{
		return _dissolvingExpiryTime;
	}

	public int getHasCastle()
	{
		return _hasCastle;
	}

	public int getHasFort()
	{
		return _hasFort;
	}

	public int getHasHideout()
	{
		return _hasHideout | _hasFort;
	}

	public int getHiredGuards()
	{
		return _hiredGuards;
	}

	public L2ClanMember getLeader()
	{
		return _leader;
	}

	public int getLeaderId()
	{
		return _leader != null ? _leader.getObjectId() : 0;
	}

	public String getLeaderName()
	{
		return _leader == null ? "None" : _leader.getName();
	}

	public int getLeaderSubPledge(int leaderId)
	{
		int id = 0;
		for (SubPledge sp : _subPledges.values())
		{
			if (sp.getLeaderId() == 0)
			{
				continue;
			}
			if (sp.getLeaderId() == leaderId)
			{
				id = sp.getId();
				break;
			}
		}
		return id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMaxNrOfMembers(int subpledgetype)
	{
		int limit = 0;

		switch (subpledgetype)
		{
			case 0:
				switch (getLevel())
				{
					case 8:
						limit = Config.MAX_CLAN_MEMBERS_LVL8;
						break;
					case 7:
						limit = Config.MAX_CLAN_MEMBERS_LVL7;
						break;
					case 6:
						limit = Config.MAX_CLAN_MEMBERS_LVL6;
						break;
					case 5:
						limit = Config.MAX_CLAN_MEMBERS_LVL5;
						break;
					case 4:
						limit = Config.MAX_CLAN_MEMBERS_LVL4;
						break;
					case 3:
						limit = Config.MAX_CLAN_MEMBERS_LVL3;
						break;
					case 2:
						limit = Config.MAX_CLAN_MEMBERS_LVL2;
						break;
					case 1:
						limit = Config.MAX_CLAN_MEMBERS_LVL1;
						break;
					case 0:
						limit = Config.MAX_CLAN_MEMBERS_LVL0;
						break;
					default:
						limit = Config.MAX_CLAN_MEMBERS_LVL8;
						break;
				}
				break;
			case -1:
			case 100:
			case 200:
				limit = Config.MAX_CLAN_MEMBERS_ROYALS;
				break;
			case 1001:
			case 1002:
			case 2001:
			case 2002:
				limit = Config.MAX_CLAN_MEMBERS_KNIGHTS;
				break;
			default:
				break;
		}

		return limit;
	}

	public L2ClanMember[] getMembers()
	{
		return _members.values().toArray(new L2ClanMember[_members.size()]);
	}

	public int getMembersCount()
	{
		return _members.size();
	}

	public String getName()
	{
		return _name;
	}

	public L2PcInstance getNewLeader()
	{
		return L2World.getInstance().getPlayer(_newLeaderId);
	}

	public int getNewLeaderId()
	{
		return _newLeaderId;
	}

	public String getNewLeaderName()
	{
		return CharNameTable.getInstance().getByObjectId(_newLeaderId);
	}

	
	public String getNotice()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT notice FROM clan_notices WHERE clanID=?");
			statement.setInt(1, getClanId());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				_notice = rset.getString("notice");
			}

			rset.close();
			statement.close();
			con.close();

		}
		catch (Exception e)
		{
			System.out.println("BBS: Error while getting notice from DB for clan " + getClanId() + "");
			if (e.getMessage() != null)
			{
				System.out.println("BBS: Exception = " + e.getMessage() + "");
			}
		}

		return _notice;
	}

	
	public String getNoticeForBBS()
	{
		String notice = "";
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT notice FROM clan_notices WHERE clanID=?");
			statement.setInt(1, getClanId());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				notice = rset.getString("notice");
			}

			rset.close();
			statement.close();
			con.close();

		}
		catch (Exception e)
		{
			System.out.println("BBS: Error while getting notice from DB for clan " + getClanId() + "");
			if (e.getMessage() != null)
			{
				System.out.println("BBS: Exception = " + e.getMessage() + "");
			}
		}
		return notice.replaceAll("<br>", "\n");
	}

	public List<L2PcInstance> getOnlineAllyMembers()
	{
		List<L2PcInstance> list = new ArrayList<>();

		for (L2Clan clan : ClanTable.getInstance().getClanAllies(getAllyId()))
			if (clan.getAllyId() == getAllyId())
			{
				list.addAll(clan.getOnlineMembersList());
			}
		return list;
	}

	public L2PcInstance[] getOnlineMembers()
	{
		List<L2PcInstance> list = new ArrayList<>();
		for (L2ClanMember temp : _members.values())
			if (temp != null && temp.isOnline())
			{
				list.add(temp.getPlayerInstance());
			}
		return list.toArray(new L2PcInstance[list.size()]);
	}

	public L2PcInstance[] getOnlineMembers(int exclude)
	{
		LinkedBunch<L2PcInstance> result = new LinkedBunch<>();
		for (L2ClanMember temp : _members.values())
			if (temp != null)
				if (temp.isOnline() && temp.getObjectId() != exclude)
				{
					result.add(temp.getPlayerInstance());
				}

		return result.moveToArray(new L2PcInstance[result.size()]);
	}

	public int getOnlineMembersCount()
	{
		int count = 0;
		for (L2ClanMember temp : _members.values())
		{
			if (temp == null || !temp.isOnline())
			{
				continue;
			}

			count++;
		}
		return count;
	}

	public List<L2PcInstance> getOnlineMembersList()
	{
		List<L2PcInstance> result = new ArrayList<>();
		for (L2ClanMember temp : _members.values())
			if (temp != null)
				if (temp.isOnline() && temp.getPlayerInstance() != null)
				{
					result.add(temp.getPlayerInstance());
				}

		return result;
	}

	public int getRank()
	{
		return _rank;
	}

	public int getRankPrivs(int rank)
	{
		if (_privs.get(rank) != null)
			return _privs.get(rank).getPrivs();

		return CP_NOTHING;
	}

	public int getReputationScore()
	{
		return _reputationScore;
	}

	public List<L2Skill> getSkills()
	{
		return _skillList;
	}

	public final SubPledge getSubPledge(int subpledgeType)
	{
		if (_subPledges == null)
			return null;

		return _subPledges.get(subpledgeType);
	}

	public final SubPledge getSubPledge(String pledgeName)
	{
		if (_subPledges == null)
			return null;

		for (SubPledge sp : _subPledges.values())
			if (sp.getName().equalsIgnoreCase(pledgeName))
				return sp;
		return null;
	}

	public String getSubPledgeLeaderName(int pledgeType)
	{
		if (pledgeType == 0)
			return _leader.getName();

		SubPledge subPledge = _subPledges.get(pledgeType);
		int leaderId = subPledge.getLeaderId();

		if (subPledge.getId() == L2Clan.SUBUNIT_ACADEMY || leaderId == 0)
			return "";

		if (!_members.containsKey(leaderId))
		{
			_log.warn("SubPledgeLeader: " + leaderId + " is missing from clan: " + _name + "[" + _clanId + "]");
			return "";
		}

		return _members.get(leaderId).getName();
	}

	public int getSubPledgeMembersCount(int subpl)
	{
		int result = 0;
		for (L2ClanMember temp : _members.values())
			if (temp.getPledgeType() == subpl)
			{
				result++;
			}
		return result;
	}

	public ClanWarehouse getWarehouse()
	{
		return _warehouse;
	}

	public List<Integer> getWarList()
	{
		return _atWarWith;
	}

	public Map<Integer, Long> getWarPenalty()
	{
		return _warPenaltyExpiryTime;
	}

	public boolean hasCrest()
	{
		return _hasCrest;
	}

	public boolean hasCrestLarge()
	{
		return _hasCrestLarge;
	}

	public boolean hasWarPenaltyWith(int clanId)
	{
		if (!_warPenaltyExpiryTime.containsKey(clanId))
			return false;

		return _warPenaltyExpiryTime.get(clanId) > System.currentTimeMillis();
	}

	public void incrementHiredGuards()
	{
		_hiredGuards++;
	}

	public void initializePrivs()
	{
		RankPrivs privs;
		for (int i = 1; i < 10; i++)
		{
			privs = new RankPrivs(i, 0, CP_NOTHING);
			_privs.put(i, privs);
		}
	}

	
	public void insertNotice()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_notices (clanID, notice, enabled) values (?,?,?)");
			statement.setInt(1, getClanId());
			statement.setString(2, "Change me");
			statement.setString(3, "false");
			statement.execute();
			statement.close();
			con.close();

		}
		catch (Exception e)
		{
			System.out.println("BBS: Error while creating clan notice for clan " + getClanId() + "");
			if (e.getMessage() != null)
			{
				System.out.println("BBS: Exception = " + e.getMessage() + "");
			}
		}
	}

	public boolean isAtWar()
	{
		return _atWarWith != null && !_atWarWith.isEmpty();
	}

	public boolean isAtWarAttacker(int id)
	{
		return _atWarAttackers.contains(id);
	}

	public boolean isAtWarAttacker(Integer id)
	{
		if (_atWarAttackers != null && !_atWarAttackers.isEmpty())
			if (_atWarAttackers.contains(id))
				return true;

		return false;
	}

	public boolean isAtWarWith(int id)
	{
		return _atWarWith.contains(id);
	}

	public boolean isAtWarWith(Integer id)
	{
		if (_atWarWith != null && !_atWarWith.isEmpty())
			if (_atWarWith.contains(id))
				return true;

		return false;
	}

	public boolean isMember(int id)
	{
		return id != 0 && _members.containsKey(id);
	}

	
	public boolean isNoticeEnabled()
	{
		String result = "";
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT enabled FROM clan_notices WHERE clanID=?");
			statement.setInt(1, getClanId());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				result = rset.getString("enabled");
			}

			rset.close();
			statement.close();
			con.close();

		}
		catch (Exception e)
		{
			System.out.println("BBS: Error while reading _noticeEnabled for clan " + getClanId() + "");
			if (e.getMessage() != null)
			{
				System.out.println("BBS: Exception = " + e.getMessage() + "");
			}
		}
		if (result.isEmpty())
		{
			insertNotice();
			return false;
		}
		return result.compareToIgnoreCase("true") == 0;
	}

	public void levelUpClan(L2PcInstance player)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (System.currentTimeMillis() < getDissolvingExpiryTime())
		{
			player.sendPacket(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS);
			return;
		}

		boolean increaseClanLevel = false;

		ClanLeveLUpPricesData.ClanLevel clanLevel = ClanLeveLUpPricesData.getInstance().getClanLevel(getLevel() + 1);

		if (clanLevel != null)
		{
			increaseClanLevel = true;
			for (Map.Entry<Integer, Integer> entry : clanLevel.getItems().entrySet())
			{
				increaseClanLevel = increaseClanLevel && PcAction.haveItem(player, entry.getKey(), entry.getValue(), true);
			}

			increaseClanLevel = increaseClanLevel && player.getSp() >= clanLevel.sp;
			increaseClanLevel = increaseClanLevel && player.getExp() >= clanLevel.exp;
			increaseClanLevel = increaseClanLevel && player.getClan().getReputationScore() >= clanLevel.reputation;
			increaseClanLevel = increaseClanLevel && player.getClan().getMembersCount() >= clanLevel.members;

			if (increaseClanLevel)
			{
				for (Map.Entry<Integer, Integer> entry : clanLevel.getItems().entrySet())
				{
					PcAction.removeItem(player, entry.getKey(), entry.getValue(), "clan level up");
				}

				if (clanLevel.sp > 0)
				{
					player.setSp(player.getSp() - clanLevel.sp);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1).addNumber(clanLevel.sp));
				}

				if (clanLevel.reputation > 0)
				{
					setReputationScore(getReputationScore() - clanLevel.reputation, true);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(clanLevel.reputation));
				}
			}
		}

		if (!increaseClanLevel)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL);
			return;
		}

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);

		player.broadcastPacket(new SocialAction(player, 15));
		player.sendPacket(new ItemList(player, false));

		changeLevel(getLevel() + 1);

		player.updateNameTitleColor();
	}

	public void removeClanMember(int objectId, long clanJoinExpiryTime)
	{
		L2ClanMember exMember = _members.remove(objectId);
		if (exMember == null)
		{
			_log.warn("Member Object ID: " + objectId + " not found in clan while trying to remove");
			return;
		}
		int leadssubpledge = getLeaderSubPledge(objectId);
		if (leadssubpledge != 0)
		{
			getSubPledge(leadssubpledge).setLeaderId(0);
			updateSubPledgeInDB(leadssubpledge);
		}

		if (exMember.getApprentice() != 0)
		{
			L2ClanMember apprentice = getClanMember(exMember.getApprentice());
			if (apprentice != null)
			{
				if (apprentice.getPlayerInstance() != null)
				{
					apprentice.getPlayerInstance().setSponsor(0);
				}
				else
				{
					apprentice.initApprenticeAndSponsor(0, 0);
				}

				apprentice.saveApprenticeAndSponsor(0, 0);
			}
		}
		if (exMember.getSponsor() != 0)
		{
			L2ClanMember sponsor = getClanMember(exMember.getSponsor());
			if (sponsor != null)
			{
				if (sponsor.getPlayerInstance() != null)
				{
					sponsor.getPlayerInstance().setApprentice(0);
				}
				else
				{
					sponsor.initApprenticeAndSponsor(0, 0);
				}

				sponsor.saveApprenticeAndSponsor(0, 0);
			}
		}
		exMember.saveApprenticeAndSponsor(0, 0);

		if (Config.REMOVE_CASTLE_CIRCLETS)
		{
			CastleManager.getInstance().removeCirclet(exMember, getHasCastle());
		}

		if (exMember.isOnline())
		{
			L2PcInstance player = exMember.getPlayerInstance();

			if (!player.isNoble())
			{
				player.setTitle("");
			}

			// Setup active warehouse to null.
			if (player.getActiveWarehouse() != null)
			{
				player.setActiveWarehouse(null);
			}

			player.setApprentice(0);
			player.setSponsor(0);
			player.setSiegeState((byte) 0);

			if (player.isClanLeader())
			{
				SiegeManager.removeSiegeSkills(player);
				player.setClanCreateExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L);
			}

			for (L2Skill skill : player.getClan().getAllSkills())
			{
				player.removeSkill(skill, false);
			}

			if (player.getClan().getHasFort() > 0)
			{
				FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
			}
			if (player.getClan().getHasCastle() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(player.getClan()).removeResidentialSkills(player);
			}
			player.sendSkillList();

			player.setClan(null);
			if (exMember.getPledgeRank() != -1)
			{
				player.setClanJoinExpiryTime(clanJoinExpiryTime);
			}
			player.setPledgeClass(L2ClanMember.getCurrentPledgeClass(player));

			player.broadcastUserInfo();

			player.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
		}
		else
		{
			removeMemberInDatabase(exMember, clanJoinExpiryTime, getLeaderId() == objectId ? System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L : 0);
		}
	}

	
	private void removeMemberInDatabase(L2ClanMember member, long clanJoinExpiryTime, long clanCreateExpiryTime)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE charId=?");
			statement.setString(1, "");
			statement.setLong(2, clanJoinExpiryTime);
			statement.setLong(3, clanCreateExpiryTime);
			statement.setInt(4, member.getObjectId());
			statement.execute();
			statement.close();
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.info("clan member removed in db: " + getClanId());
			}

			statement = con.prepareStatement("UPDATE characters SET apprentice=0 WHERE apprentice=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();

			statement = con.prepareStatement("UPDATE characters SET sponsor=0 WHERE sponsor=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error removing clan member.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void removeSkill(int id)
	{
		L2Skill deleteSkill = null;
		for (L2Skill sk : _skillList)
			if (sk.getId() == id)
			{
				deleteSkill = sk;
				return;
			}
		_skillList.remove(deleteSkill);
	}

	public void removeSkill(L2Skill deleteSkill)
	{
		_skillList.remove(deleteSkill);
	}

	
	private void restore()
	{
		Connection con = null;
		try
		{
			L2ClanMember member;

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM clan_data where clan_id=?");
			statement.setInt(1, getClanId());
			ResultSet clanData = statement.executeQuery();

			if (clanData.next())
			{
				setName(clanData.getString("clan_name"));
				setLevel(clanData.getInt("clan_level"));
				setHasCastle(clanData.getInt("hasCastle"));
				setAllyId(clanData.getInt("ally_id"));
				setAllyName(clanData.getString("ally_name"));
				setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
				if (getAllyPenaltyExpiryTime() < System.currentTimeMillis())
				{
					setAllyPenaltyExpiryTime(0, 0);
				}

				setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
				if (getCharPenaltyExpiryTime() + Config.ALT_CLAN_JOIN_DAYS * 86400000L < System.currentTimeMillis())
				{
					setCharPenaltyExpiryTime(0);
				}

				setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));

				setCrestId(clanData.getInt("crest_id"));
				if (getCrestId() != 0)
				{
					setHasCrest(true);
				}

				setCrestLargeId(clanData.getInt("crest_large_id"));
				if (getCrestLargeId() != 0)
				{
					setHasCrestLarge(true);
				}

				setAllyCrestId(clanData.getInt("ally_crest_id"));
				setReputationScore(clanData.getInt("reputation_score"), false);
				setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);
				setNewLeaderId(clanData.getInt("new_leader_id"), false);
				int leaderId = clanData.getInt("leader_id");

				PreparedStatement statement2 = con.prepareStatement("SELECT char_name,level,classid,charId,title,pledge_rank,subpledge,apprentice,sponsor,race,sex FROM characters WHERE clanid=?");
				statement2.setInt(1, getClanId());
				ResultSet clanMembers = statement2.executeQuery();

				while (clanMembers.next())
				{
					member = new L2ClanMember(this, clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("charId"), clanMembers.getInt("subpledge"), clanMembers.getInt("pledge_rank"), clanMembers.getString("title"), clanMembers.getInt("sex"), clanMembers.getInt("race"));
					if (member.getObjectId() == leaderId)
					{
						setLeader(member);
					}
					else
					{
						addClanMember(member);
					}
					member.initApprenticeAndSponsor(clanMembers.getInt("apprentice"), clanMembers.getInt("sponsor"));
				}
				clanMembers.close();
				statement2.close();
			}

			clanData.close();
			statement.close();

			restoreSubPledges();
			restoreRankPrivs();
			restoreSkills();
		}
		catch (Exception e)
		{
			_log.error("Error restoring clan data.", e);
			_log.warn(String.valueOf(getClanId()), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private void restoreRankPrivs()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT privilleges,rank,party FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, getClanId());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int rank = rset.getInt("rank");
				int privileges = rset.getInt("privilleges");
				if (rank == -1)
				{
					continue;
				}
				_privs.get(rank).setPrivs(privileges);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error restoring clan privs by rank.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private void restoreSkills()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, getClanId());

			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				if (skill != null)
				{
					_skills.put(skill.getId(), skill);
				}
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error restoring clan skills.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private void restoreSubPledges()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT sub_pledge_id,name,leader_id FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, getClanId());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int id = rset.getInt("sub_pledge_id");
				String name = rset.getString("name");
				int leaderId = rset.getInt("leader_id");
				SubPledge pledge = new SubPledge(id, name, leaderId);
				_subPledges.put(id, pledge);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error restoring clan sub-units.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void setAllyCrestId(int allyCrestId)
	{
		_allyCrestId = allyCrestId;
	}

	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}

	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}

	public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType)
	{
		_allyPenaltyExpiryTime = expiryTime;
		_allyPenaltyType = penaltyType;
	}

	public void setAttackerClan(int clanId)
	{
		_atWarAttackers.add(clanId);
	}

	
	public void setAuctionBiddedAt(int id, boolean storeInDb)
	{
		_auctionBiddedAt = id;

		if (storeInDb)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");
				statement.setInt(1, id);
				statement.setInt(2, getClanId());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("Could not store auction for clan: " + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	public void setCharPenaltyExpiryTime(long time)
	{
		_charPenaltyExpiryTime = time;
	}

	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}

	public void setCrestId(int crestId)
	{
		_crestId = crestId;
	}

	public void setCrestLargeId(int crestLargeId)
	{
		_crestLargeId = crestLargeId;
	}

	public void setDissolvingExpiryTime(long time)
	{
		_dissolvingExpiryTime = time;
	}

	public void setEnemyClan(int clanId)
	{
		_atWarWith.add(clanId);
	}

	public void setHasCastle(int hasCastle)
	{
		_hasCastle = hasCastle;
	}

	public void setHasCrest(boolean flag)
	{
		_hasCrest = flag;
	}

	public void setHasCrestLarge(boolean flag)
	{
		_hasCrestLarge = flag;
	}

	public void setHasFort(int hasFort)
	{
		_hasFort = hasFort;
	}

	public void setHasHideout(int hasHideout)
	{
		_hasHideout = hasHideout;
	}

	public void setLeader(L2ClanMember leader)
	{
		_leader = leader;
		_members.put(leader.getObjectId(), leader);
	}

	public void setLevel(int level)
	{
		_level = level;
		if (_forum == null)
			if (_level >= 2)
			{
				_forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot").getChildByName(_name);

				if (_forum == null)
				{
					_forum = ForumsBBSManager.getInstance().createNewForum(_name, ForumsBBSManager.getInstance().getForumByName("ClanRoot"), Forum.CLAN, Forum.CLANMEMBERONLY, getClanId());
				}
			}
	}

	public void setName(String name)
	{
		_name = name;
	}

	public void setNewLeader(L2ClanMember member)
	{
		final L2PcInstance newLeader = member.getPlayerInstance();
		final L2ClanMember exMember = getLeader();
		final L2PcInstance exLeader = exMember.getPlayerInstance();

		if (exLeader != null)
		{
			if (exLeader.isFlying())
			{
				exLeader.dismount();
			}
			SiegeManager.removeSiegeSkills(exLeader);
			exLeader.setClanPrivileges(L2Clan.CP_NOTHING);
			exLeader.broadcastUserInfo();

		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?"))
			{
				statement.setInt(1, L2Clan.CP_NOTHING);
				statement.setInt(2, getLeaderId());
				statement.execute();
			}
			catch (Exception e)
			{
				_log.warn("Couldn't update clan privs for old clan leader", e);
			}
		}

		setLeader(member);
		if (getNewLeaderId() != 0)
		{
			setNewLeaderId(0, true);
		}
		updateClanInDB();

		if (exLeader != null)
		{
			exLeader.setPledgeClass(L2ClanMember.calculatePledgeClass(exLeader));
			exLeader.broadcastUserInfo();
			exLeader.checkItemRestriction();
		}

		if (newLeader != null)
		{
			newLeader.setPledgeClass(L2ClanMember.calculatePledgeClass(newLeader));
			newLeader.setClanPrivileges(L2Clan.CP_ALL);
			SiegeManager.addSiegeSkills(newLeader);
			newLeader.broadcastUserInfo();
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?"))
			{
				statement.setInt(1, L2Clan.CP_ALL);
				statement.setInt(2, getLeaderId());
				statement.execute();
			}
			catch (Exception e)
			{
				_log.warn("Couldn't update clan privs for new clan leader", e);
			}
		}

		broadcastClanStatus();
		broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1).addString(member.getName()));
		CrownManager.checkCrowns(exLeader);
		CrownManager.checkCrowns(newLeader);
	}

	public void setNewLeaderId(int objectId, boolean storeInDb)
	{
		_newLeaderId = objectId;
		if (storeInDb)
		{
			updateClanInDB();
		}
	}

	
	public void setNotice(String notice)
	{
		notice = notice.replaceAll("\n", "<br>");

		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE clan_notices SET notice=? WHERE clanID=?");

			statement.setString(1, notice);
			statement.setInt(2, getClanId());
			statement.execute();
			statement.close();
			con.close();

			_notice = notice;
		}
		catch (Exception e)
		{
			System.out.println("BBS: Error while saving notice for clan " + getClanId() + "");
			if (e.getMessage() != null)
			{
				System.out.println("BBS: Exception = " + e.getMessage() + "");
			}
		}
	}

	
	public void setNoticeEnabled(boolean noticeEnabled)
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE clan_notices SET enabled=? WHERE clanID=?");
			if (noticeEnabled)
			{
				statement.setString(1, "true");
			}
			else
			{
				statement.setString(1, "false");
			}
			statement.setInt(2, getClanId());
			statement.execute();
			statement.close();
			con.close();

		}
		catch (Exception e)
		{
			System.out.println("BBS: Error while updating notice status for clan " + getClanId() + "");
			if (e.getMessage() != null)
			{
				System.out.println("BBS: Exception = " + e.getMessage() + "");
			}
		}

	}

	public void setRank(int rank)
	{
		_rank = rank;
	}


	
	public void setRankPrivs(int rank, int privs)
	{
		if (_privs.get(rank) != null)
		{
			_privs.get(rank).setPrivs(privs);

			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privilleges) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE privilleges = ?");
				statement.setInt(1, getClanId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.setInt(5, privs);

				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("Could not store clan privs for rank: " + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			for (L2ClanMember cm : getMembers())
				if (cm.isOnline())
					if (cm.getPledgeRank() == rank)
						if (cm.getPlayerInstance() != null)
						{
							cm.getPlayerInstance().setClanPrivileges(privs);
							cm.getPlayerInstance().sendPacket(new UserInfo(cm.getPlayerInstance()));
						}
			broadcastClanStatus();
		}
		else
		{
			_privs.put(rank, new RankPrivs(rank, 0, privs));

			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privilleges) VALUES (?,?,?,?)");
				statement.setInt(1, getClanId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("Could not create new rank and store clan privs for rank: " + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	public void setReputationScore(int value, boolean save)
	{
		if (_reputationScore >= 0 && value < 0)
		{
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED));
			L2Skill[] skills = getAllSkills();
			for (L2ClanMember member : _members.values())
				if (member.isOnline() && member.getPlayerInstance() != null)
				{
					for (L2Skill sk : skills)
					{
						member.getPlayerInstance().removeSkill(sk, false);
					}
				}
		}
		else if (_reputationScore < 0 && value >= 0)
		{
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER));
			L2Skill[] skills = getAllSkills();
			for (L2ClanMember member : _members.values())
				if (member.isOnline() && member.getPlayerInstance() != null)
				{
					for (L2Skill sk : skills)
						if (sk.getMinPledgeClass() <= member.getPlayerInstance().getPledgeClass())
						{
							member.getPlayerInstance().addSkill(sk, false);
						}
				}
		}

		_reputationScore = value;
		if (_reputationScore > 100000000)
		{
			_reputationScore = 100000000;
		}
		if (_reputationScore < -100000000)
		{
			_reputationScore = -100000000;
		}
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
		if (save)
		{
			updateClanInDB();
		}
	}

	
	public void store()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,new_leader_id) values (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getClanId());
			statement.setString(2, getName());
			statement.setInt(3, getLevel());
			statement.setInt(4, getHasCastle());
			statement.setInt(5, getAllyId());
			statement.setString(6, getAllyName());
			statement.setInt(7, getLeaderId());
			statement.setInt(8, getCrestId());
			statement.setInt(9, getCrestLargeId());
			statement.setInt(10, getAllyCrestId());
			statement.setInt(11, getNewLeaderId());
			statement.execute();
			statement.close();
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.info("New clan saved in db: " + getClanId());
			}
		}
		catch (Exception e)
		{
			_log.error("Error saving new clan.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	public String toString()
	{
		return getName();
	}

	
	public void updateClanInDB()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=?,ally_crest_id=?,new_leader_id=? WHERE clan_id=?");
			statement.setInt(1, getLeaderId());
			statement.setInt(2, getAllyId());
			statement.setString(3, getAllyName());
			statement.setInt(4, getReputationScore());
			statement.setLong(5, getAllyPenaltyExpiryTime());
			statement.setInt(6, getAllyPenaltyType());
			statement.setLong(7, getCharPenaltyExpiryTime());
			statement.setLong(8, getDissolvingExpiryTime());
			statement.setInt(9, getAllyCrestId());
			statement.setInt(10, getNewLeaderId());
			statement.setInt(11, getClanId());

			statement.execute();
			statement.close();
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.info("New clan leader saved in db: " + getClanId());
			}
		}
		catch (Exception e)
		{
			_log.error("Error while saving new clan leader.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void updateClanMember(L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(player);
		if (player.isClanLeader())
		{
			setLeader(member);
		}

		addClanMember(member);
	}

	
	public void updateSubPledgeInDB(int pledgeType)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE clan_subpledges SET leader_id=? WHERE clan_id=? AND sub_pledge_id=?");
			statement.setInt(1, getSubPledge(pledgeType).getLeaderId());
			statement.setInt(2, getClanId());
			statement.setInt(3, pledgeType);
			statement.execute();
			statement = con.prepareStatement("UPDATE clan_subpledges SET name=? WHERE clan_id=? AND sub_pledge_id=?");
			statement.setString(1, getSubPledge(pledgeType).getName());
			statement.setInt(2, getClanId());
			statement.setInt(3, pledgeType);
			statement.execute();
			statement.close();
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.info("New subpledge leader and/or name saved in db: " + getClanId());
			}
		}
		catch (Exception e)
		{
			_log.error("Error saving new sub clan leader.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

}