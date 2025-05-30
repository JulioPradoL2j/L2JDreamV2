package com.dream.game.model.entity.siege;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.Announcements;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.MercTicketManager;
import com.dream.game.manager.SiegeGuardManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.SiegeManager.SiegeSpawn;
import com.dream.game.manager.SiegeRewardManager;
import com.dream.game.manager.TownManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.L2SiegeClan.SiegeClanType;
import com.dream.game.model.L2SiegeStatus;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ControlTowerInstance;
import com.dream.game.model.actor.instance.L2FlameTowerInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.TowerSpawn;
import com.dream.game.model.entity.Town;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.Location;
import com.dream.game.model.zone.L2SiegeZone;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.SiegeInfo;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.util.Broadcast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

import org.apache.log4j.Logger;

public class Siege
{
	public class ScheduleEndSiegeTask implements Runnable
	{
		private final Castle _castleInst;

		public ScheduleEndSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}

		@Override
		public void run()
		{
			if (!getIsInProgress())
				return;

			try
			{
				long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 3600000)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_HOURS_UNTIL_SIEGE_CONCLUSION).addNumber(2), true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 3600000);
				}
				else if (timeRemaining <= 3600000 && timeRemaining > 600000)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(Math.round(timeRemaining / 60000)), true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 600000);
				}
				else if (timeRemaining <= 600000 && timeRemaining > 300000)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(Math.round(timeRemaining / 60000)), true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 300000);
				}
				else if (timeRemaining <= 300000 && timeRemaining > 10000)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(Math.round(timeRemaining / 60000)), true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 10000);
				}
				else if (timeRemaining <= 10000 && timeRemaining > 0)
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_SIEGE_S1_SECONDS_LEFT).addNumber(Math.round(timeRemaining / 1000)), true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining);
				}
				else
				{
					_castleInst.getSiege().endSiege();
				}
			}
			catch (Exception e)
			{
				_log.warn(Level.SEVERE, e);
			}
		}
	}

	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Castle _castleInst;

		public ScheduleStartSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}

		@Override
		public void run()
		{
			_scheduledStartSiegeTask.cancel(false);
			if (getIsInProgress())
				return;

			try
			{
				if (!getIsTimeRegistrationOver())
				{
					long regTimeRemaining = getTimeRegistrationOverDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
					if (regTimeRemaining > 0)
					{
						_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), regTimeRemaining);
						return;
					}
					endTimeRegistration(true);
				}

				long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

				if (timeRemaining > 86400000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 86400000);
				}
				else if (timeRemaining <= 86400000 && timeRemaining > 13600000)
				{
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED).addString(getCastle().getName()));
					_isRegistrationOver = true;
					clearSiegeWaitingClan();
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 13600000);
				}
				else if (timeRemaining <= 13600000 && timeRemaining > 600000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 600000);
				}
				else if (timeRemaining <= 600000 && timeRemaining > 300000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 300000);
				}
				else if (timeRemaining <= 300000 && timeRemaining > 10000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 10000);
				}
				else if (timeRemaining <= 10000 && timeRemaining > 0)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining);
				}
				else
				{
					_castleInst.getSiege().startSiege();
				}
			}
			catch (Exception e)
			{
				_log.warn(Level.SEVERE, e);
			}
		}
	}

	public static enum TeleportWhoType
	{
		All,
		Attacker,
		DefenderNotOwner,
		Owner,
		Spectator
	}

	public static final byte OWNER = -1;
	public static final byte DEFENDER = 0;

	public static final byte ATTACKER = 1;

	public static final byte DEFENDER_NOT_APPROVED = 2;

	public final static Logger _log = Logger.getLogger(Siege.class.getName());

	public final static int getAttackerRespawnDelay()
	{
		return Config.SIEGE_RESPAWN_DELAY_ATTACKER;
	}

	private int _controlTowerCount;

	private int _controlTowerMaxCount;
	private int _flameTowerCount;

	private int _flameTowerMaxCount;
	protected ScheduledFuture<?> _scheduledStartSiegeTask;

	private final List<L2SiegeClan> _attackerClans = new CopyOnWriteArrayList<>();
	private final List<L2SiegeClan> _defenderClans = new CopyOnWriteArrayList<>();

	private final List<L2SiegeClan> _defenderWaitingClans = new CopyOnWriteArrayList<>();
	private List<L2ControlTowerInstance> _controlTowers = new ArrayList<>();
	private List<L2FlameTowerInstance> _flameTowers = new ArrayList<>();
	private final Castle _castle;
	private boolean _isInProgress = false;
	private boolean _isNormalSide = true;
	protected boolean _isRegistrationOver = false;

	protected Calendar _siegeEndDate;

	private final SiegeGuardManager _siegeGuardManager;

	public Siege(Castle castle)
	{
		_castle = castle;
		_siegeGuardManager = new SiegeGuardManager(getCastle().getName(), getCastle().getCastleId(), getCastle().getOwnerId());
		startAutoTask();
	}

	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER));
	}

	private void addAttacker(L2SiegeClan sc)
	{
		if (sc == null)
			return;
		sc.setType(SiegeClanType.ATTACKER);
		getAttackerClans().add(sc);
	}

	private void addDefender(int clanId)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER));
	}

	private void addDefender(int clanId, SiegeClanType type)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, type));
	}

	private void addDefender(L2SiegeClan sc, SiegeClanType type)
	{
		if (sc == null)
			return;
		sc.setType(type);
		getDefenderClans().add(sc);
	}

	private void addDefenderWaiting(int clanId)
	{
		getDefenderWaitingClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING));
	}

	private boolean allyIsRegisteredOnOppositeSide(L2Clan clan, boolean attacker)
	{
		int allyId = clan.getAllyId();

		if (allyId != 0)
		{
			for (L2Clan alliedClan : ClanTable.getInstance().getClans())
			{
				if (alliedClan.getAllyId() == allyId)
				{
					if (alliedClan.getClanId() == clan.getClanId())
					{
						continue;
					}

					if (attacker)
					{
						if (checkIsDefender(alliedClan) || checkIsDefenderWaiting(alliedClan))
							return true;
					}
					else
					{
						if (checkIsAttacker(alliedClan))
							return true;
					}
				}
			}
		}
		return false;
	}

	public void announceToOpponent(SystemMessage sm, boolean toAtk)
	{
		List<L2SiegeClan> clans = toAtk ? getAttackerClans() : getDefenderClans();
		for (L2SiegeClan siegeclan : clans)
		{
			ClanTable.getInstance().getClan(siegeclan.getClanId()).broadcastToOnlineMembers(sm);
		}
	}

	public void announceToOpponent(SystemMessage sm, L2Clan self)
	{
		if (self != null)
		{
			boolean atk = true;
			if (getAttackerClan(self) != null)
			{
				atk = false;
			}
			else if (getDefenderClan(self) == null)
				return;
			announceToOpponent(sm, atk);
		}
	}

	public void announceToParticipants(SystemMessage sm)
	{
		for (L2SiegeClan siegeclan : getAttackerClans())
			if (ClanTable.getInstance().getClan(siegeclan.getClanId()) != null)
			{
				ClanTable.getInstance().getClan(siegeclan.getClanId()).broadcastToOnlineMembers(sm);
			}
		for (L2SiegeClan siegeclan : getDefenderClans())
			if (ClanTable.getInstance().getClan(siegeclan.getClanId()) != null)
			{
				ClanTable.getInstance().getClan(siegeclan.getClanId()).broadcastToOnlineMembers(sm);
			}
	}

	public void announceToPlayer(String message, boolean inAreaOnly)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (!inAreaOnly || inAreaOnly && checkIfInZone(player.getX(), player.getY(), player.getZ()))
			{
				player.sendMessage(message);
			}
	}

	public void announceToPlayer(SystemMessage sm, boolean inAreaOnly)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (!inAreaOnly || inAreaOnly && checkIfInZone(player.getX(), player.getY(), player.getZ()))
			{
				player.sendPacket(sm);
			}
	}

	public void approveSiegeDefenderClan(int clanId)
	{
		if (clanId <= 0)
			return;

		saveSiegeClan(ClanTable.getInstance().getClan(clanId), DEFENDER);
		loadSiegeClan();
	}

	public boolean checkIfAlreadyRegisteredForSameDay(L2Clan clan)
	{
		for (Siege siege : SiegeManager.getSieges())
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
				if (siege.checkIsDefenderWaiting(clan))
					return true;
			}
		}
		return false;
	}

	private boolean checkIfCanRegister(L2PcInstance player, int typeId)
	{
		L2Clan clan = player.getClan();
		if (clan == null || clan.getLevel() < Config.SIEGE_CLAN_MIN_LEVEL)
		{
			player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_LOW_CLAN_LEVEL), Config.SIEGE_CLAN_MIN_LEVEL));
			return false;
		}
		else if (clan.getMembersCount() < Config.SIEGE_CLAN_MIN_MEMBERCOUNT)
		{
			player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_NOT_ENOUGH_CLAN_MEMBERS), Config.SIEGE_CLAN_MIN_MEMBERCOUNT));
			return false;
		}
		else if (getIsRegistrationOver())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED).addString(getCastle().getName()));
			return false;
		}
		else if (getIsInProgress())
		{
			player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
			return false;
		}
		else if (clan.getHasCastle() > 0)
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
			return false;
		}
		else if (clan.getClanId() == getCastle().getOwnerId())
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
			return false;
		}
		else if (checkIfAlreadyRegisteredForSameDay(player.getClan()))
		{
			player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
			return false;
		}
		else
		{
			for (int i = 0; i < 10; i++)
				if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), i))
				{
					player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
					return false;
				}
		}

		if (typeId == 0 || typeId == 2 || typeId == -1)
			if (getDefenderClans().size() + getDefenderWaitingClans().size() >= Config.SIEGE_MAX_DEFENDER)
			{
				player.sendPacket(SystemMessageId.DEFENDER_SIDE_FULL);
				return false;
			}
		if (typeId == 1)
			if (getAttackerClans().size() >= Config.SIEGE_MAX_ATTACKER)
			{
				player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
				return false;
			}

		return true;
	}

	public boolean checkIfInZone(int x, int y, int z)
	{
		Town town = TownManager.getInstance().getTown(x, y, z);
		return getIsInProgress() && (getCastle().checkIfInZone(x, y, z) || getZone().isInsideZone(x, y) || town != null && getCastle().getCastleId() == town.getCastleId());
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
		if (clan == null)
			return false;
		return getDefenderClan(clan) != null;
	}

	public boolean checkIsDefenderWaiting(L2Clan clan)
	{
		return getDefenderWaitingClan(clan) != null;
	}

	
	public void clearSiegeClan()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.close();

			if (getCastle().getOwnerId() > 0)
			{
				PreparedStatement statement2 = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");
				statement2.setInt(1, getCastle().getOwnerId());
				statement2.execute();
				statement2.close();
			}

			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			_log.error("Exception: clearSiegeClan(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	public void clearSiegeWaitingClan()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and type = 2");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.close();

			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			_log.error("Exception: clearSiegeWaitingClan(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void disableTraps()
	{
		_flameTowerCount--;
	}

	public void endSiege()
	{
		if (getIsInProgress())
		{
			Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED).addString(getCastle().getName()));
			Broadcast.toAllOnlinePlayers(new PlaySound("systemmsg_e.18"));

			announceToParticipants(SystemMessageId.TEMPORARY_ALLIANCE_DISSOLVED.getSystemMessage());

			if (getCastle().getOwnerId() > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
				announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.NEW_CASTLE_LORD), true);
				Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE).addString(clan.getName()).addString(getCastle().getName()));
			}
			else
				Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW).addString(getCastle().getName()));

			getCastle().updateClansReputation();
			removeFlags();
			teleportPlayer(Siege.TeleportWhoType.Attacker, TeleportWhereType.Town);
			teleportPlayer(Siege.TeleportWhoType.DefenderNotOwner, TeleportWhereType.Town);
			teleportPlayer(Siege.TeleportWhoType.Spectator, TeleportWhereType.Town);
			_isInProgress = false;
			updatePlayerSiegeStateFlags(true);
			getZone().updateSiegeStatus();
			saveCastleSiege();
			clearSiegeClan();
			removeControlTower();
			removeFlameTower();

			_siegeGuardManager.unspawnSiegeGuard();
			if (getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs();
			}
			getCastle().spawnDoor();

			if (Config.ACTIVATED_SYSTEM)
			{
				if (getCastle().getOwnerId() > 0)
					SiegeRewardManager.getInstance().notifySiegeEnded(ClanTable.getInstance().getClan(getCastle().getOwnerId()), getCastle().getName());
			}
		}
	}

	public void endTimeRegistration(boolean automatic)
	{
		getCastle().setIsTimeRegistrationOver(true);
		if (!automatic)
		{
			saveSiegeDate();
			Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME).addString(getCastle().getName()));
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
		if (_isNormalSide)
			return _attackerClans;
		return _defenderClans;
	}

	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan == null)
			{
				continue;
			}
			for (L2PcInstance player : clan.getOnlineMembers(0))
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
		}
		return players;
	}

	public final Castle getCastle()
	{
		return _castle;
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

	public int getControlTowerCount()
	{
		return _controlTowerCount;
	}

	public int getControlTowerMaxCount()
	{
		return _controlTowerMaxCount;
	}

	public final L2SiegeClan getDefenderClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderClans())
			if (sc != null && sc.getClanId() == clanId)
				return sc;
		return null;
	}

	public final L2SiegeClan getDefenderClan(L2Clan clan)
	{
		if (clan == null)
			return null;
		return getDefenderClan(clan.getClanId());
	}

	public final List<L2SiegeClan> getDefenderClans()
	{
		if (_isNormalSide)
			return _defenderClans;
		return _attackerClans;
	}

	public List<L2PcInstance> getDefendersButNotOwnersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getClanId() == getCastle().getOwnerId())
			{
				continue;
			}
			for (L2PcInstance player : clan.getOnlineMembers(0))
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
		}
		return players;
	}

	public final L2SiegeClan getDefenderWaitingClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderWaitingClans())
			if (sc != null && sc.getClanId() == clanId)
				return sc;
		return null;
	}

	public final L2SiegeClan getDefenderWaitingClan(L2Clan clan)
	{
		if (clan == null)
			return null;
		return getDefenderWaitingClan(clan.getClanId());
	}

	public final List<L2SiegeClan> getDefenderWaitingClans()
	{
		return _defenderWaitingClans;
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

	public int getFlameTowerMaxCount()
	{
		return _flameTowerMaxCount;
	}

	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}

	public final boolean getIsRegistrationOver()
	{
		return _isRegistrationOver;
	}

	public final boolean getIsTimeRegistrationOver()
	{
		return getCastle().getIsTimeRegistrationOver();
	}

	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getClanId() != getCastle().getOwnerId())
			{
				continue;
			}
			for (L2PcInstance player : clan.getOnlineMembers(0))
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
		}
		return players;
	}

	public List<L2PcInstance> getPlayersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();

		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (!player.isInsideZone(L2Zone.FLAG_SIEGE))
			{
				continue;
			}
			if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
			{
				players.add(player);
			}
		}

		return players;
	}

	public final Calendar getSiegeDate()
	{
		return getCastle().getSiegeDate();
	}

	public final SiegeGuardManager getSiegeGuardManager()
	{
		return _siegeGuardManager;
	}

	public List<L2PcInstance> getSpectatorsInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();

		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (!player.isInsideZone(L2Zone.FLAG_SIEGE) || player.getSiegeState() != 0)
			{
				continue;
			}
			if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
			{
				players.add(player);
			}
		}

		return players;
	}

	public final Calendar getTimeRegistrationOverDate()
	{
		return getCastle().getTimeRegistrationOverDate();
	}

	public final L2SiegeZone getZone()
	{
		return getCastle().getBattlefield();
	}

	public boolean isTrapsActive()
	{
		return _flameTowerCount > 0;
	}

	public void killedCT(L2Npc ct)
	{
		_controlTowerCount--;
		if (_controlTowerCount < 0)
		{
			_controlTowerCount = 0;
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

	public void listRegisterClan(L2PcInstance player)
	{
		player.sendPacket(new SiegeInfo(getCastle(), null, null));
	}

	private void loadSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();

			// Add castle owner as defender (add owner first so that they are on the top of the defender list)
			if (getCastle().getOwnerId() > 0)
			{
				addDefender(getCastle().getOwnerId(), SiegeClanType.OWNER);
			}

			PreparedStatement statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans where castle_id=?");
			statement.setInt(1, getCastle().getCastleId());
			ResultSet rs = statement.executeQuery();

			int typeId;
			while (rs.next())
			{
				typeId = rs.getInt("type");
				if (typeId == DEFENDER)
				{
					addDefender(rs.getInt("clan_id"));
				}
				else if (typeId == ATTACKER)
				{
					addAttacker(rs.getInt("clan_id"));
				}
				else if (typeId == DEFENDER_NOT_APPROVED)
				{
					addDefenderWaiting(rs.getInt("clan_id"));
				}
			}
		}
		catch (Exception e)
		{
			_log.warn(Level.WARNING, e);
		}
	}

	public void midVictory()
	{
		if (getIsInProgress())
		{
			if (getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs();
			}

			if (getDefenderClans().size() == 0 && getAttackerClans().size() == 1)
			{
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				endSiege();
				return;
			}
			if (getCastle().getOwnerId() > 0)
			{
				int allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
				if (getDefenderClans().size() == 0)
					if (allyId != 0)
					{
						boolean allinsamealliance = true;
						for (L2SiegeClan sc : getAttackerClans())
							if (sc != null)
								if (ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId)
								{
									allinsamealliance = false;
								}
						if (allinsamealliance)
						{
							L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
							removeAttacker(sc_newowner);
							addDefender(sc_newowner, SiegeClanType.OWNER);
							endSiege();
							return;
						}
					}

				for (L2SiegeClan sc : getDefenderClans())
					if (sc != null)
					{
						removeDefender(sc);
						addAttacker(sc);
					}

				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);

				if (allyId != 0)
				{
					L2Clan[] clanList = ClanTable.getInstance().getClans();

					for (L2Clan clan : clanList)
						if (clan.getAllyId() == allyId)
						{
							L2SiegeClan sc = getAttackerClan(clan.getClanId());
							if (sc != null)
							{
								removeAttacker(sc);
								addDefender(sc, SiegeClanType.DEFENDER);
							}
						}
				}
				for (L2Character cha : getZone().getCharactersInside().values())
					if (cha instanceof L2PcInstance)
						if (!checkIsDefender(((L2PcInstance) cha).getClan()))
						{
							cha.teleToLocation(TeleportWhereType.Town);
						}
						else
						{
							cha.broadcastFullInfo();
						}
				removeDefenderFlags();
				getCastle().removeUpgrade();
				getCastle().spawnDoor(true);
				removeControlTower();
				removeFlameTower();

				_controlTowerCount = 0;
				_controlTowerMaxCount = 0;
				_flameTowerCount = 0;
				_flameTowerMaxCount = 0;
				spawnFlameTowers();
				spawnControlTower(getCastle().getCastleId());
				updatePlayerSiegeStateFlags(false);
				announceToParticipants(SystemMessageId.TEMPORARY_ALLIANCE.getSystemMessage());
			}
		}
	}

	public void registerAttacker(L2PcInstance player)
	{
		if (player.getClan() == null)
			return;

		int allyId = 0;
		if (getCastle().getOwnerId() != 0)
		{
			allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
		}

		if (allyId != 0)
		{
			if (player.getClan().getAllyId() == allyId)
			{
				player.sendPacket(SystemMessageId.CANNOT_ATTACK_ALLIANCE_CASTLE);
				return;
			}
		}

		if (allyIsRegisteredOnOppositeSide(player.getClan(), true))
		{
			player.sendPacket(SystemMessageId.CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE);
		}
		else if (checkIfCanRegister(player, ATTACKER))
		{
			saveSiegeClan(player.getClan(), ATTACKER);
		}
	}

	public void registerDefender(L2PcInstance player)
	{
		if (getCastle().getOwnerId() <= 0)
		{
			player.sendPacket(SystemMessageId.DEFENDER_SIDE_FULL);
		}
		else if (allyIsRegisteredOnOppositeSide(player.getClan(), false))
		{
			player.sendPacket(SystemMessageId.CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE);
		}
		else if (checkIfCanRegister(player, DEFENDER_NOT_APPROVED))
		{
			saveSiegeClan(player.getClan(), DEFENDER_NOT_APPROVED);
		}
	}

	private void removeAttacker(L2SiegeClan sc)
	{
		if (sc != null)
		{
			getAttackerClans().remove(sc);
		}
	}

	private void removeControlTower()
	{
		if (_controlTowers != null)
		{
			for (L2ControlTowerInstance ct : _controlTowers)
				if (ct != null)
				{
					ct.decayMe();
				}

			_controlTowers = null;
		}
	}

	private void removeDefender(L2SiegeClan sc)
	{
		if (sc != null)
		{
			getDefenderClans().remove(sc);
		}
	}

	private void removeDefenderFlags()
	{
		for (L2SiegeClan sc : getDefenderClans())
			if (sc != null)
			{
				sc.removeFlags();
			}
	}

	private void removeFlags()
	{
		for (L2SiegeClan sc : getAttackerClans())
			if (sc != null)
			{
				sc.removeFlags();
			}
		for (L2SiegeClan sc : getDefenderClans())
			if (sc != null)
			{
				sc.removeFlags();
			}
	}

	private void removeFlameTower()
	{
		if (_flameTowers != null && !_flameTowers.isEmpty())
		{
			// Remove all instances of control tower for this castle
			for (L2FlameTowerInstance ct : _flameTowers)
				if (ct != null)
				{
					ct.deleteMe();
				}
			_flameTowers.clear();
			_flameTowers = null;
		}
	}

	
	public void removeSiegeClan(int clanId)
	{
		if (clanId <= 0)
			return;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and clan_id=?");
			statement.setInt(1, getCastle().getCastleId());
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();
			SiegeManager.getInstance().removeClan(getCastle().getCastleId(), clanId);
			loadSiegeClan();

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
		if (clan == null || clan.getHasCastle() == getCastle().getCastleId() || !SiegeManager.getInstance().checkIsRegistered(clan, getCastle().getCastleId()))
			return;
		removeSiegeClan(clan.getClanId());
	}

	public void removeSiegeClan(L2PcInstance player)
	{
		removeSiegeClan(player.getClan());
	}

	private void saveCastleSiege()
	{
		setNextSiegeDate();
		getTimeRegistrationOverDate().setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		getTimeRegistrationOverDate().add(Calendar.DAY_OF_MONTH, 1);
		getCastle().setIsTimeRegistrationOver(false);
		saveSiegeDate();
		startAutoTask();
	}

	
	private void saveSiegeClan(L2Clan clan, byte typeId)
	{
		if (clan.getHasCastle() > 0)
			return;

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (typeId == DEFENDER || typeId == DEFENDER_NOT_APPROVED || typeId == OWNER)
			{
				if (getDefenderClans().size() + getDefenderWaitingClans().size() >= Config.SIEGE_MAX_DEFENDER)
					return;
			}
			else
			{
				if (getAttackerClans().size() >= Config.SIEGE_MAX_ATTACKER)
					return;
			}

			PreparedStatement statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) VALUES (?,?,?,0) ON DUPLICATE KEY UPDATE type=?");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, getCastle().getCastleId());
			statement.setInt(3, typeId);
			statement.setInt(4, typeId);
			statement.execute();
			statement.close();

			if (typeId == DEFENDER || typeId == OWNER)
			{
				addDefender(clan.getClanId());
			}
			else if (typeId == ATTACKER)
			{
				addAttacker(clan.getClanId());
			}
			else if (typeId == DEFENDER_NOT_APPROVED)
			{
				addDefenderWaiting(clan.getClanId());
			}
		}
		catch (Exception e)
		{
			_log.warn(Level.WARNING, e);
		}
	}

	
	public void saveSiegeDate()
	{
		if (_scheduledStartSiegeTask != null)
		{
			_scheduledStartSiegeTask.cancel(true);
			_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(getCastle()), 1000);
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE castle SET siegeDate = ?, regTimeEnd = ?, regTimeOver = ?, AutoTime = ?  WHERE id = ?");
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setLong(2, getTimeRegistrationOverDate().getTimeInMillis());
			statement.setString(3, String.valueOf(getIsTimeRegistrationOver()));
			statement.setString(4, "true");
			statement.setInt(5, getCastle().getCastleId());
			statement.execute();

			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Exception: saveSiegeDate(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void setNextSiegeDate()
	{
		while (getCastle().getSiegeDate().getTimeInMillis() < System.currentTimeMillis())
		{
			if (getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
			{
				switch (getCastle().getCastleId())
				{
					case 1:
					case 2:
					case 5:
					case 8:
						getCastle().getSiegeDate().set(Config.DAY_TO_SIEGE, Calendar.SATURDAY);
						break;
					case 3:
					case 4:
					case 6:
					case 7:
					case 9:
						getCastle().getSiegeDate().set(Config.DAY_TO_SIEGE, Calendar.SUNDAY);
						break;
					default:
						_log.info("Could't set siege day for castle ID: " + getCastle().getCastleId());
						break;
				}
			}

			getCastle().getSiegeDate().add(Calendar.DAY_OF_MONTH, 7);
			if (!getCastle().isAutoTime())
			{
				switch (getCastle().getCastleId())
				{
					case 1:
					case 2:
					case 5:
					case 8:
						getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
						getCastle().getSiegeDate().set(Calendar.HOUR_OF_DAY, Config.HOUR_TO_SIEGE);
						break;
					case 3:
					case 4:
					case 6:
					case 7:
					case 9:
						getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
						getCastle().getSiegeDate().set(Calendar.HOUR_OF_DAY, Config.HOUR_TO_SIEGE);
						break;
					default:
						_log.info("Could't set siege day for castle ID: " + getCastle().getCastleId());
						break;
				}
				getCastle().getSiegeDate().set(Calendar.MINUTE, 00);
			}
		}

		if (!SevenSigns.getInstance().isDateInSealValidPeriod(getCastle().getSiegeDate()) && Config.CORECT_SIEGE_DATE_BY_7S)
		{
			getCastle().getSiegeDate().add(Calendar.DAY_OF_MONTH, 7);
		}

		Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME).addString(getCastle().getName()));

		_isRegistrationOver = false;
	}

	private void spawnControlTower(int Id)
	{
		if (_controlTowers == null)
		{
			_controlTowers = new ArrayList<>();
		}

		for (SiegeSpawn _sp : SiegeManager.getInstance().getControlTowerSpawnList(Id))
		{
			L2ControlTowerInstance ct;

			L2NpcTemplate template = NpcTable.getInstance().getTemplate(_sp.getNpcId());

			template.setBaseHpMax(_sp.getHp());

			ct = new L2ControlTowerInstance(IdFactory.getInstance().getNextId(), template);
			ct.getStatus().setCurrentHpMp(_sp.getHp(), ct.getMaxMp());
			ct.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 20);

			_controlTowerCount++;
			_controlTowers.add(ct);
		}
	}

	private void spawnFlameTowers()
	{
		for (TowerSpawn ts : SiegeManager.getInstance().getFlameTowers(getCastle().getCastleId()))
		{
			try
			{
				final L2Spawn spawn = new L2Spawn(NpcTable.getInstance().getTemplate(ts.getId()));

				final Location loc = ts.getLocation(); // TODO : implements spawn via Location.
				spawn.setLocx(loc.getX());
				spawn.setLocy(loc.getY());
				spawn.setLocz(loc.getZ());

				final L2FlameTowerInstance tower = (L2FlameTowerInstance) spawn.doSpawn();
				tower.setUpgradeLevel(ts.getUpgradeLevel());
				tower.setZoneList(ts.getZoneList());
				_flameTowers.add(tower);
			}
			catch (Exception e)
			{
				_log.warn(getClass().getName() + ": Cannot spawn flame tower! " + e);
			}
		}
	}

	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();

		if (!getSiegeGuardManager().getSiegeGuardSpawn().isEmpty() && !_controlTowers.isEmpty())
		{
			L2ControlTowerInstance closestCt;
			int x, y, z;
			double distance;
			double distanceClosest = 0;
			for (L2Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn())
			{
				if (spawn == null)
				{
					continue;
				}

				closestCt = null;
				distanceClosest = Integer.MAX_VALUE;

				x = spawn.getLocx();
				y = spawn.getLocy();
				z = spawn.getLocz();

				for (L2ControlTowerInstance ct : _controlTowers)
				{
					if (ct == null)
					{
						continue;
					}

					distance = ct.getDistanceSq(x, y, z);

					if (distance < distanceClosest)
					{
						closestCt = ct;
						distanceClosest = distance;
					}
				}
				if (closestCt != null)
				{
					closestCt.registerGuard(spawn);
				}
			}
		}
	}

	public void startAutoTask()
	{
		loadSiegeClan();

		if (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			saveCastleSiege();
		}
		else
		{
			if (_scheduledStartSiegeTask != null)
			{
				_scheduledStartSiegeTask.cancel(false);
			}

			_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(getCastle()), 1000);
		}
	}

	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (getAttackerClans().size() <= 0)
			{
				if (getCastle().getOwnerId() <= 0)
				{
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addString(getCastle().getName()));
				}
				else
				{
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED).addString(getCastle().getName()));
				}

				saveCastleSiege();
				return;
			}

			_isNormalSide = true;
			_isInProgress = true;
			loadSiegeClan();
			updatePlayerSiegeStateFlags(false);
			for (L2Character cha : getZone().getCharactersInside().values())
				if (cha instanceof L2PcInstance)
				{
					L2PcInstance pc = (L2PcInstance) cha;
					if (!checkIsDefender(pc.getClan()))
					{
						pc.teleToLocation(TeleportWhereType.Town);
					}
					else
					{
						pc.broadcastFullInfo();
					}
				}

			_controlTowerCount = 0;
			_controlTowerMaxCount = 0;

			spawnControlTower(getCastle().getCastleId());

			getCastle().resetArtifact();
			getCastle().spawnDoor();
			spawnFlameTowers();
			spawnSiegeGuard();
			MercTicketManager.getInstance().deleteTickets(getCastle().getCastleId());
			getZone().updateSiegeStatus();

			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, Config.SIEGE_LENGTH_MINUTES);
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getCastle()), 1000);

			Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_STARTED).addString(getCastle().getName()));
			Broadcast.toAllOnlinePlayers(new PlaySound("systemmsg_e.17"));
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
			case DefenderNotOwner:
				players = getDefendersButNotOwnersInZone();
				break;
			case Spectator:
				players = getSpectatorsInZone();
				break;
			default:
				players = getPlayersInZone();
		}

		for (L2PcInstance player : players)
		{
			if (player.isGM() || player.isInJail())
			{
				continue;
			}
			L2SiegeStatus.getInstance().addStatus(player.getClanId(), player.getCharId());
			player.teleToLocation(teleportWhere);
		}
	}

	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for (L2SiegeClan siegeClan : getAttackerClans())
		{
			if (siegeClan == null)
			{
				continue;
			}

			clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
			if (clan == null)
			{
				continue;
			}
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
				member.revalidateZone(true);
				L2SiegeStatus.getInstance().addStatus(member.getClanId(), member.getObjectId());
			}
		}
		for (L2SiegeClan siegeClan : getDefenderClans())
		{
			if (siegeClan == null)
			{
				continue;
			}

			clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
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
				member.revalidateZone(true);
				L2SiegeStatus.getInstance().addStatus(member.getClanId(), member.getObjectId());
			}
		}
	}

}