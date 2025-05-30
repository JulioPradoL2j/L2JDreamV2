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
package com.dream.game.model.entity.siege;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.Announcements;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeGuardManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.FortSiegeManager.SiegeSpawn;
import com.dream.game.model.CombatFlag;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.L2SiegeClan.SiegeClanType;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2FortCommanderInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2SiegeZone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public class FortSiege
{
	public class ScheduleEndSiegeTask implements Runnable
	{
		private final Fort _fortInst;

		public ScheduleEndSiegeTask(final Fort pFort)
		{
			_fortInst = pFort;
		}

		@Override
		public void run()
		{
			if (!getIsInProgress())
				return;

			try
			{
				final long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

				if (timeRemaining > 3600000)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 3600000); // Prepare task for 1 hr left.
				}
				else if (timeRemaining <= 3600000 && timeRemaining > 600000)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), timeRemaining / 60000 + " minute(s) until " + getFort().getName() + " siege conclusion.");
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				}
				else if (timeRemaining <= 600000 && timeRemaining > 300000)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), timeRemaining / 60000 + " minute(s) until " + getFort().getName() + " siege conclusion.");
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 300000);
				}
				else if (timeRemaining <= 300000 && timeRemaining > 10000)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), timeRemaining / 60000 + " minute(s) until " + getFort().getName() + " siege conclusion.");
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 10000);
				}
				else if (timeRemaining <= 10000 && timeRemaining > 0)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), getFort().getName() + " siege " + timeRemaining / 1000 + " second(s) left!");
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining);
				}
				else
				{
					_fortInst.getSiege().endSiege();
				}
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
			}
		}
	}

	public class ScheduleSiegeRestore implements Runnable
	{
		private final Fort _fortInst;

		public ScheduleSiegeRestore(Fort pFort)
		{
			_fortInst = pFort;
		}

		@Override
		public void run()
		{
			if (!getIsInProgress())
				return;

			try
			{
				_siegeRestore = null;
				_fortInst.getSiege().resetSiege();
				announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("Barracks are again"), 0, false);
			}
			catch (Exception e)
			{
				_log.warn("Exception: ScheduleSiegeRestore() for Fort: " + _fortInst.getName() + " " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Fort _fortInst;
		private final int _time;

		public ScheduleStartSiegeTask(Fort pFort, int time)
		{
			_fortInst = pFort;
			_time = time;
		}

		@Override
		public void run()
		{
			if (getIsInProgress())
				return;

			try
			{
				final long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

				if (_time == 3600)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), timeRemaining / 60000 + " minute(s) until " + getFort().getName() + " siege begin.");
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst, 600), 3000000);
				}
				else if (_time == 600)
				{
					getFort().getSpawnManager().despawnSuspiciousMerchant();
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst, 300), 300000);
				}
				else if (_time == 300)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst, 60), 240000);
				}
				else if (_time == 60)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst, 30), 30000);
				}
				else if (_time == 30)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst, 10), 20000);
				}
				else if (_time == 10)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst, 5), 5000);
				}
				else if (_time == 5)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst, 1), 4000);
				}
				else if (_time == 1)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst, 0), 1000);
				}
				else if (_time == 0)
				{
					_fortInst.getSiege().startSiege();
				}
				else
				{
					_log.warn("Exception: ScheduleStartSiegeTask(): unknown siege time: " + String.valueOf(_time));
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public class ScheduleSuspicoiusMerchantSpawn implements Runnable
	{
		private final Fort _fortInst;

		public ScheduleSuspicoiusMerchantSpawn(Fort pFort)
		{
			_fortInst = pFort;
		}

		@Override
		public void run()
		{
			if (!getIsInProgress())
				return;

			try
			{
				_fortInst.getSpawnManager().spawnSuspiciousMerchant();
			}
			catch (Exception e)
			{
				_log.warn("Exception: ScheduleSuspicoiusMerchantSpawn() for Fort: " + _fortInst.getName() + " " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static enum TeleportWhoType
	{
		All,
		Attacker,
		Owner
	}

	protected static final Logger _log = Logger.getLogger(FortSiege.class.getName());

	private static void spawnFlag(int Id)
	{
		List<CombatFlag> list = FortSiegeManager.getInstance().getFlagList(Id);
		if (list == null)
			return;

		for (CombatFlag cf : list)
		{
			cf.spawnMe();
		}
	}

	private final List<L2SiegeClan> _attackerClans = new ArrayList<>();
	protected Map<Integer, List<L2Spawn>> _commanders = new HashMap<>();
	protected List<L2Spawn> _commandersSpawns;
	private final Fort[] _fort;
	private boolean _isInProgress = false;
	private FortSiegeGuardManager _siegeGuardManager;
	private ScheduledFuture<?> _siegeEnd = null;
	public ScheduledFuture<?> _siegeRestore = null;

	private ScheduledFuture<?> _siegeStartTask = null;

	public FortSiege(Fort[] fort)
	{
		_fort = fort;

		checkAutoTask();
	}

	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER));
	}

	public void announceToPlayer(SystemMessage sm, int val, boolean useFortId)
	{
		if (!useFortId && val > 0)
		{
			sm.addNumber(val);
		}
		else
		{
			sm.addFortId(val);
		}
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				member.sendPacket(sm);
			}
		}
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				member.sendPacket(sm);
			}
		}
	}

	public void announceToPlayer(SystemMessage sm, String text)
	{
		sm.addString(text);
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				member.sendPacket(sm);
			}
		}
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				member.sendPacket(sm);
			}
		}
	}

	public void checkAutoTask()
	{
		if (getFort().getSiegeDate().getTimeInMillis() < System.currentTimeMillis())
		{
			clearSiegeDate();
			saveSiegeDate();
			removeSiegeClan(0);
			return;
		}

		startAutoTask(false);
	}

	public boolean checkIfAlreadyRegisteredForSameDay(L2Clan clan)
	{
		for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
		{
			if (siege == this)
			{
				continue;
			}
			if (siege.getSiegeDate().get(Calendar.DAY_OF_WEEK) == getSiegeDate().get(Calendar.DAY_OF_WEEK))
			{
				if (siege.checkIsAttacker(clan))
					return true;
				if (siege.checkIsDefender(clan))
					return true;
			}
		}
		return false;
	}

	public boolean checkIfCanRegister(L2PcInstance player)
	{
		boolean b = true;
		L2Clan clan = player.getClan();

		if (clan == null || clan.getLevel() < Config.FORTSIEGE_CLAN_MIN_LEVEL)
		{
			b = false;
			player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_LOW_CLAN_LEVEL), Config.FORTSIEGE_CLAN_MIN_LEVEL));
		}
		else if (clan == getFort().getOwnerClan())
		{
			b = false;
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		}
		else if (clan.getHasCastle() > 0)
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
			return false;
		}
		else
		{
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort.getSiege().getAttackerClan(player.getClanId()) != null)
				{
					b = false;
					player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
					break;
				}
				if (fort.getOwnerClan() == clan && (fort.getSiege().getIsInProgress() || fort.getSiege()._siegeStartTask != null))
				{
					b = false;
					player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
					break;
				}
			}
		}
		return b;
	}

	public boolean checkIfInZone(int x, int y, int z)
	{
		return getIsInProgress() && getFort().checkIfInZone(x, y, z);
	}

	public boolean checkIfInZone(L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}

	public boolean checkIsAttacker(L2Clan clan)
	{
		return getAttackerClan(clan) != null;
	}

	public boolean checkIsDefender(L2Clan clan)
	{
		return getFort().getOwnerClan() == clan && clan != null;
	}

	
	public void clearSiegeClan()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
			statement.setInt(1, getFort().getFortId());
			statement.execute();
			statement.close();

			if (getFort().getOwnerClan() != null)
			{
				PreparedStatement statement2 = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?");
				statement2.setInt(1, getFort().getOwnerClan().getClanId());
				statement2.execute();
				statement2.close();
			}

			getAttackerClans().clear();

			if (getIsInProgress())
			{
				endSiege();
			}
			if (_siegeStartTask != null)
			{
				_siegeStartTask.cancel(true);
				_siegeStartTask = null;
				ThreadPoolManager.getInstance().executeAi(new ScheduleSuspicoiusMerchantSpawn(getFort()));
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception: clearSiegeClan(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void clearSiegeDate()
	{
		getFort().getSiegeDate().setTimeInMillis(0);
	}

	public void endSiege()
	{
		if (getIsInProgress())
		{
			announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), "The siege of " + getFort().getName() + " has finished!");

			if (getFort().getOwnerId() <= 0)
			{
				announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), "The siege of " + getFort().getName() + " has ended in a draw.");
			}

			removeFlags();
			unSpawnFlags();
			teleportPlayer(FortSiege.TeleportWhoType.Attacker, TeleportWhereType.Town);
			_isInProgress = false;
			updatePlayerSiegeStateFlags(true);
			getZone().updateSiegeStatus();
			saveFortSiege();
			clearSiegeClan();
			removeCommanders();
			getFort().getSpawnManager().spawnNpcCommanders();
			getSiegeGuardManager().unspawnSiegeGuard();
			getFort().resetDoors();
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSuspicoiusMerchantSpawn(getFort()), Config.FORTSIEGE_MERCHANT_DELAY * 60 * 1000);
			if (_siegeEnd != null)
			{
				_siegeEnd.cancel(false);
			}
			if (_siegeRestore != null)
			{
				_siegeRestore.cancel(false);
			}
			if (getFort().getOwnerClan() != null)
			{
				getFort().setVisibleFlag(true);
			}
		}
	}

	public final L2SiegeClan getAttackerClan(int clanId)
	{
		for (L2SiegeClan sc : getAttackerClans())
			if (sc != null && sc.getClanId() == clanId)
				return sc;
		return null;
	}

	public final L2SiegeClan getAttackerClan(L2Clan clan)
	{
		if (clan == null)
			return null;
		return getAttackerClan(clan.getClanId());
	}

	public final List<L2SiegeClan> getAttackerClans()
	{
		return _attackerClans;
	}

	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance player : clan.getOnlineMembers(0))
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
		}
		return players;
	}

	public L2Npc getClosestFlag(L2Object obj)
	{
		if (obj != null && obj instanceof L2PcInstance)
			if (((L2PcInstance) obj).getClan() != null)
			{
				L2SiegeClan sc = getAttackerClan(((L2PcInstance) obj).getClan());
				if (sc != null)
					return sc.getClosestFlag(obj);
			}
		return null;
	}

	public Map<Integer, List<L2Spawn>> getCommanders()
	{
		return _commanders;
	}

	public Set<L2Npc> getFlag(L2Clan clan)
	{
		if (clan != null)
		{
			L2SiegeClan sc = getAttackerClan(clan);
			if (sc != null)
				return sc.getFlag();
		}
		return null;
	}

	public final Fort getFort()
	{
		if (_fort == null || _fort.length <= 0)
			return null;
		return _fort[0];
	}

	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}

	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> lst = new ArrayList<>();
		for (L2Character cha : getZone().getCharactersInside().values())
			if (cha instanceof L2PcInstance && ((L2PcInstance) cha).getClan() != null && ((L2PcInstance) cha).getClan() == getFort().getOwnerClan())
			{
				lst.add((L2PcInstance) cha);
			}
		return lst;
	}

	public List<L2PcInstance> getPlayersInZone()
	{
		List<L2PcInstance> lst = new ArrayList<>();
		for (L2Character cha : getZone().getCharactersInside().values())
			if (cha instanceof L2PcInstance)
			{
				lst.add((L2PcInstance) cha);
			}
		return lst;
	}

	public final Calendar getSiegeDate()
	{
		return getFort().getSiegeDate();
	}

	public final FortSiegeGuardManager getSiegeGuardManager()
	{
		if (_siegeGuardManager == null)
		{
			_siegeGuardManager = new FortSiegeGuardManager(getFort());
		}
		return _siegeGuardManager;
	}

	public final L2SiegeZone getZone()
	{
		return getFort().getBattlefield();
	}

	public void killedCommander(L2FortCommanderInstance instance)
	{
		if (_commanders != null && !_commanders.get(getFort().getFortId()).isEmpty())
		{
			L2Spawn spawn = instance.getSpawn();
			if (spawn != null)
			{
				List<SiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getFortId());
				for (SiegeSpawn spawn2 : commanders)
					if (spawn2.getNpcId() == spawn.getNpcid())
					{
						String text = "";
						switch (spawn2.getId())
						{
							case 1:
								text = "You may have broken our arrows, but you will never break our will! Archers retreat!";
								break;
							case 2:
								text = "Aieeee! Command Center! This is guard unit! We need backup right away!";
								break;
							case 3:
								text = "At last! The Magic Field that protects the fortress has weakened! Volunteers, stand back!";
								break;
							case 4:
								text = "I feel so much grief that I can't even take care of myself. There isn't any reason for me to stay here any longer.";
								break;
						}
						if (!text.isEmpty())
						{
							instance.broadcastPacket(new NpcSay(instance.getObjectId(), 1, instance.getNpcId(), text));
						}
					}
				_commanders.get(getFort().getFortId()).remove(spawn);
				if (_commanders.get(getFort().getFortId()).size() == 0)
				{
					spawnFlag(getFort().getFortId());
					if (_siegeRestore != null)
					{
						_siegeRestore.cancel(false);
					}

					for (L2DoorInstance door : getFort().getDoors())
					{
						if (!door.getIsCommanderDoor())
						{
							continue;
						}
						door.openMe();
					}
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), "All barracks captured");
				}
				else if (_siegeRestore == null)
				{
					_siegeRestore = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSiegeRestore(getFort()), Config.FORTSIEGE_COUNTDOWN_LENGTH * 60 * 1000);
				}
			}
			else
			{
				_log.warn("FortSiege.killedCommander(): killed commander, but commander not registered for fortress. NpcId: " + instance.getNpcId() + " FortId: " + getFort().getFortId());
			}
		}
	}

	public void killedFlag(L2Npc flag)
	{
		if (flag == null)
			return;
		for (L2SiegeClan clan : getAttackerClans())
			if (clan.removeFlag(flag))
				return;
	}

	
	private void loadSiegeClan()
	{
		Connection con = null;
		try
		{
			getAttackerClans().clear();

			PreparedStatement statement = null;
			ResultSet rs = null;

			con = L2DatabaseFactory.getInstance().getConnection(con);

			statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans WHERE fort_id=?");
			statement.setInt(1, getFort().getFortId());
			rs = statement.executeQuery();

			while (rs.next())
			{
				addAttacker(rs.getInt("clan_id"));
			}

			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadSiegeClan(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public boolean registerAttacker(L2PcInstance player, boolean force)
	{
		if (player.getClan() == null)
			return false;
		if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan());
			if (getAttackerClans().size() == 1)
			{
				if (!force)
				{
					player.reduceAdena("siege", 250000, null, true);
				}
				startAutoTask(true);
			}
			return true;
		}
		return false;
	}

	private void removeCommanders()
	{
		try
		{
			for (L2Spawn spawn : _commanders.get(getFort().getFortId()))
				if (spawn != null)
				{
					spawn.getLastSpawn().deleteMe();
					_commanders.get(getFort().getFortId()).remove(spawn);
				}
		}
		catch (NullPointerException npe)
		{
			return;
		}
	}

	private void removeFlags()
	{
		for (L2SiegeClan sc : getAttackerClans())
			if (sc != null)
			{
				sc.removeFlags();
			}
	}

	
	public void removeSiegeClan(int clanId)
	{

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			if (clanId != 0)
			{
				statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? AND clan_id=?");
			}
			else
			{
				statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
			}

			statement.setInt(1, getFort().getFortId());
			if (clanId != 0)
			{
				statement.setInt(2, clanId);
			}
			statement.execute();
			statement.close();

			loadSiegeClan();
			if (getAttackerClans().size() == 0)
			{
				if (getIsInProgress())
				{
					endSiege();
				}
				if (_siegeStartTask != null)
				{
					_siegeStartTask.cancel(true);
					_siegeStartTask = null;
					ThreadPoolManager.getInstance().executeAi(new ScheduleSuspicoiusMerchantSpawn(getFort()));
				}
			}
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void removeSiegeClan(L2Clan clan)
	{
		if (clan == null || clan.getHasFort() == getFort().getFortId() || !FortSiegeManager.getInstance().checkIsRegistered(clan, getFort().getFortId()))
			return;
		removeSiegeClan(clan.getClanId());
	}

	public void removeSiegeClan(L2PcInstance player)
	{
		removeSiegeClan(player.getClan());
	}

	public void resetSiege()
	{
		removeCommanders();
		spawnCommanders();
		getFort().resetDoors();
	}

	private void saveFortSiege()
	{
		clearSiegeDate();
		saveSiegeDate();
	}

	
	private void saveSiegeClan(L2Clan clan)
	{
		Connection con = null;
		try
		{
			if (getAttackerClans().size() >= Config.FORTSIEGE_MAX_ATTACKER)
				return;

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id) VALUES (?,?)");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, getFort().getFortId());
			statement.execute();
			statement.close();

			addAttacker(clan.getClanId());
		}
		catch (Exception e)
		{
			_log.warn("Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private void saveSiegeDate()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE fort SET siegeDate = ? WHERE id = ?");
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setInt(2, getFort().getFortId());
			statement.execute();

			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Exception: saveSiegeDate(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void setSiegeDateTime()
	{
		Calendar newDate = Calendar.getInstance();
		newDate.add(Calendar.MINUTE, 60);
		getFort().setSiegeDate(newDate);
		saveSiegeDate();
	}

	private void spawnCommanders()
	{
		try
		{
			_commanders.clear();
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			_commandersSpawns = new ArrayList<>();
			for (SiegeSpawn _sp : FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getFortId()))
			{
				template1 = NpcTable.getInstance().getTemplate(_sp.getNpcId());
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(1);
					spawnDat.setLocx(_sp.getLocation().getX());
					spawnDat.setLocy(_sp.getLocation().getY());
					spawnDat.setLocz(_sp.getLocation().getZ());
					spawnDat.setHeading(_sp.getLocation().getHeading());
					spawnDat.setRespawnDelay(60);
					spawnDat.doSpawn();
					spawnDat.stopRespawn();
					_commandersSpawns.add(spawnDat);
				}
				else
				{
					_log.warn("FortSiege.spawnCommander: Data missing in NPC table for ID: " + _sp.getNpcId() + ".");
				}
				_commanders.put(getFort().getFortId(), _commandersSpawns);
			}
		}
		catch (Exception e)
		{
			_log.warn("FortSiege.spawnCommander: Spawn could not be initialized: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
	}

	public void startAutoTask(boolean setTime)
	{
		if (setTime)
		{
			setSiegeDateTime();
		}
		if (getFort().getOwnerClan() != null)
		{
			for (L2PcInstance member : getFort().getOwnerClan().getOnlineMembers(0))
			{
				member.sendMessage(Message.getMessage(member, Message.MessageId.MSG_FORT_ATTACKED));
			}
		}
		System.out.println("Siege of " + getFort().getName() + ": " + getFort().getSiegeDate().getTime());
		loadSiegeClan();
		_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(getFort(), 3600), 0);
	}

	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (_siegeStartTask != null)
			{
				_siegeStartTask.cancel(false);
			}
			_siegeStartTask = null;

			if (getAttackerClans().size() <= 0)
			{
				if (getFort().getOwnerId() <= 0)
				{
					Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addString(getFort().getName()));
				}
				else
				{
					Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED).addString(getFort().getName()));
				}
				return;
			}

			_isInProgress = true;

			loadSiegeClan();
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(FortSiege.TeleportWhoType.Attacker, TeleportWhereType.Town);
			getFort().getSpawnManager().despawnNpcCommanders();
			spawnCommanders();
			getFort().resetDoors();
			spawnSiegeGuard();
			getFort().setVisibleFlag(false);

			getZone().updateSiegeStatus();
	

			_siegeEnd = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getFort()), Config.FORTSIEGE_LENGTH_MINUTES * 60 * 1000);

			announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), "The siege of " + getFort().getName() + " has started!");
			saveFortSiege();
		}
	}

	public void teleportPlayer(TeleportWhoType teleportWho, TeleportWhereType teleportWhere)
	{
		List<L2PcInstance> players;
		switch (teleportWho)
		{
			case Owner:
				players = getOwnersInZone();
				break;
			case Attacker:
				players = getAttackersInZone();
				break;
			default:
				players = getPlayersInZone();
		}

		for (L2PcInstance player : players)
		{
			if (player.isGM() || player.isInJail() || player.getClan() == getFort().getOwnerClan())
			{
				continue;
			}
			player.teleToLocation(teleportWhere);
		}
	}

	private void unSpawnFlags()
	{
		List<CombatFlag> list = FortSiegeManager.getInstance().getFlagList(getFort().getFortId());
		if (list == null)
			return;

		for (CombatFlag cf : list)
		{
			cf.unSpawnMe();
		}
	}

	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
				}
				else
				{
					member.setSiegeState((byte) 1);
				}
				member.sendPacket(new UserInfo(member));
			}
		}
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
				}
				else
				{
					member.setSiegeState((byte) 2);
				}
				member.sendPacket(new UserInfo(member));
			}
		}
	}
}