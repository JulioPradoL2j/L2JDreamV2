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
package com.dream.game.datatables.sql;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.MagicSkillLaunched;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.PledgeShowInfoUpdate;
import com.dream.game.network.serverpackets.PledgeShowMemberListAll;
import com.dream.game.network.serverpackets.PledgeShowMemberListUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class ClanTable
{
	private static class InstanceHolder
	{
		public static ClanTable INSTANCE = new ClanTable();
	}

	private static final Logger _log = Logger.getLogger(ClanTable.class.getName());

	public static ClanTable getInstance()
	{
		return InstanceHolder.INSTANCE;
	}

	private final Map<Integer, L2Clan> _clans;

	
	public ClanTable()
	{
		_clans = new HashMap<>();
		L2Clan clan;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM clan_data");
			ResultSet result = statement.executeQuery();

			int clanCount = 0;

			while (result.next())
			{
				_clans.put(Integer.parseInt(result.getString("clan_id")), new L2Clan(Integer.parseInt(result.getString("clan_id"))));
				clan = getClan(Integer.parseInt(result.getString("clan_id")));
				if (clan.getDissolvingExpiryTime() != 0)
					if (clan.getDissolvingExpiryTime() < System.currentTimeMillis())
					{
						destroyClan(clan.getClanId());
					}
					else
					{
						scheduleRemoveClan(clan.getClanId());
					}
				clanCount++;
			}
			result.close();
			statement.close();

			_log.info("Clan Data: Loaded " + clanCount + " clans from the database.");
		}
		catch (Exception e)
		{
			_log.error("data error on ClanTable:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		allianceCheck();

		restoreWars();
	}

	private void allianceCheck()
	{
		for (L2Clan clan : _clans.values())
		{
			int allyId = clan.getAllyId();
			if (allyId != 0 && clan.getClanId() != allyId)
				if (!_clans.containsKey(allyId))
				{
					clan.setAllyId(0);
					clan.setAllyName(null);
					clan.updateClanInDB();
					_log.info(getClass().getSimpleName() + ": Removed alliance from clan: " + clan);
				}
		}
	}

	public void checkSurrender(L2Clan clan1, L2Clan clan2)
	{
		int count = 0;
		for (L2ClanMember player : clan1.getMembers())
			if (player != null && player.getPlayerInstance().wantsPeace())
			{
				count++;
			}

		if (count == clan1.getMembersCount() - 1)
		{
			clan1.deleteEnemyClan(clan2.getClanId());
			clan2.deleteEnemyClan(clan1.getClanId());
			deleteClansWars(clan1.getClanId(), clan2.getClanId());
		}
	}

	public L2Clan createClan(L2PcInstance player, String clanName)
	{
		if (null == player)
			return null;

		if (Config.MINIMUN_LEVEL_FOR_PLEDGE_CREATION > player.getLevel() && !player.isGM())
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
			return null;
		}
		if (0 != player.getClanId())
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CREATE_CLAN);
			return null;
		}
		if (System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return null;
		}
		if (clanName.length() < 3 || clanName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
			return null;
		}
		if (!Util.isValidPlayerName(clanName))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return null;
		}
		if (null != getClanByName(clanName))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(clanName));
			return null;
		}

		L2Clan clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName);
		L2ClanMember leader = new L2ClanMember(clan, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPledgeRank(), player.getTitle(), player.getAppearance().getSex() ? 1 : 0, player.getRace().ordinal());
		clan.setLeader(leader);
		leader.setPlayerInstance(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(L2ClanMember.getCurrentPledgeClass(player));
		player.setClanPrivileges(L2Clan.CP_ALL);

		_clans.put(clan.getClanId(), clan);

		player.sendPacket(new PledgeShowInfoUpdate(clan));
		player.sendPacket(new PledgeShowMemberListAll(clan, 0));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(SystemMessageId.CLAN_CREATED);
		player.sendPacket(new UserInfo(player));
		player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 0, 0, false));
		player.broadcastPacket(new MagicSkillLaunched(player, 5103, 1, false));
		return clan;
	}

	
	public void deleteClansWars(int clanId1, int clanId2)
	{
		final L2Clan clan1 = _clans.get(clanId1);
		final L2Clan clan2 = _clans.get(clanId2);

		clan1.deleteEnemyClan(clanId2);
		clan1.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan1), SystemMessage.getSystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED).addString(clan2.getName()));

		clan2.deleteAttackerClan(clanId1);
		clan2.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan2), SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP).addString(clan1.getName()));

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;

			if (Config.ALT_CLAN_WAR_PENALTY_WHEN_ENDED > 0)
			{
				final long penaltyExpiryTime = System.currentTimeMillis() + Config.ALT_CLAN_WAR_PENALTY_WHEN_ENDED * 86400000L;

				clan1.addWarPenaltyTime(clanId2, penaltyExpiryTime);

				statement = con.prepareStatement("UPDATE clan_wars SET expiry_time=? WHERE clan1=? AND clan2=?");
				statement.setLong(1, penaltyExpiryTime);
				statement.setInt(2, clanId1);
				statement.setInt(3, clanId2);
			}
			else
			{
				statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
				statement.setInt(1, clanId1);
				statement.setInt(2, clanId2);
			}
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error removing clan wars data.", e);
		}
	}

	public synchronized void destroyClan(int clanId)
	{
		L2Clan clan = getClan(clanId);
		if (clan == null)
			return;

		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));

		int castleId = clan.getHasCastle();
		if (castleId == 0)
		{
			for (Castle castle : CastleManager.getInstance().getCastles().values())
			{
				castle.getSiege().removeSiegeClan(clanId);
			}
		}
		int fortId = clan.getHasFort();
		if (fortId == 0)
		{
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clanId);
			}
		}

		L2ClanMember leaderMember = clan.getLeader();
		if (leaderMember == null)
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", null, null);
		}
		else
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", clan.getLeader().getPlayerInstance(), null);
		}

		for (L2ClanMember member : clan.getMembers())
		{
			clan.removeClanMember(member.getObjectId(), 0);
		}

		_clans.remove(clanId);
		IdFactory.getInstance().releaseId(clanId);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?");
			statement.setInt(1, clanId);
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();

			if (castleId != 0)
			{
				statement = con.prepareStatement("UPDATE castle SET taxPercent = 0 WHERE id = ?");
				statement.setInt(1, castleId);
				statement.execute();
				statement.close();
			}
			if (fortId != 0)
			{
				Fort fort = FortManager.getInstance().getFortById(fortId);
				if (fort != null)
				{
					L2Clan owner = fort.getOwnerClan();
					if (clan == owner)
					{
						fort.removeOwner(true);
					}
				}
			}

			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.info("clan removed in db: " + clanId);
			}
		}
		catch (Exception e)
		{
			_log.error("error while removing clan in db ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public L2Clan getClan(int clanId)
	{
		L2Clan clan = _clans.get(clanId);
		return clan;
	}

	public List<L2Clan> getClanAllies(int allianceId)
	{
		final List<L2Clan> clanAllies = new ArrayList<>();
		if (allianceId != 0)
		{
			for (L2Clan clan : _clans.values())
				if (clan != null && clan.getAllyId() == allianceId)
				{
					clanAllies.add(clan);
				}
		}
		return clanAllies;
	}

	public L2Clan getClanByName(String clanName)
	{
		for (L2Clan clan : getClans())
			if (clan.getName().equalsIgnoreCase(clanName))
				return clan;

		return null;
	}

	public L2Clan[] getClans()
	{
		return _clans.values().toArray(new L2Clan[_clans.size()]);
	}

	public boolean isAllyExists(String allyName)
	{
		for (L2Clan clan : getClans())
			if (clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName))
				return true;
		return false;
	}

	
	private void restoreWars()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete deprecated wars (server was offline).
			PreparedStatement statement = con.prepareStatement("DELETE FROM clan_wars WHERE expiry_time > 0 AND expiry_time <= ?");
			statement.setLong(1, System.currentTimeMillis());
			statement.execute();
			statement.close();

			// Load all wars.
			statement = con.prepareStatement("SELECT * FROM clan_wars");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				final int clan1 = rset.getInt("clan1");
				final int clan2 = rset.getInt("clan2");
				final long expiryTime = rset.getLong("expiry_time");

				// Expiry timer is found, add a penalty. Otherwise, add the regular war.
				if (expiryTime > 0)
				{
					_clans.get(clan1).addWarPenaltyTime(clan2, expiryTime);
				}
				else
				{
					_clans.get(clan1).setEnemyClan(clan2);
					_clans.get(clan2).setAttackerClan(clan1);
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error restoring clan wars data.", e);
		}
	}

	public void scheduleRemoveClan(final int clanId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (getClan(clanId) == null)
					return;
				if (getClan(clanId).getDissolvingExpiryTime() != 0)
				{
					destroyClan(clanId);
				}
			}
		}, getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis());
	}

	
	public void storeClansWars(int clanId1, int clanId2)
	{
		final L2Clan clan1 = _clans.get(clanId1);
		final L2Clan clan2 = _clans.get(clanId2);

		clan1.setEnemyClan(clanId2);
		clan1.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan1), SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP).addString(clan2.getName()));

		clan2.setAttackerClan(clanId1);
		clan2.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan2), SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR).addString(clan1.getName()));

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2) VALUES(?,?)");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error storing clan wars data.", e);
		}
	}

}