package com.dream.game.manager.clanhallsiege;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.Announcements;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.ClanHallSiege;
import com.dream.game.manager.SiegeGuardManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.L2SiegeClan.SiegeClanType;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.world.L2World;
import com.dream.game.model.zone.L2SiegeZone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SiegeInfo;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.taskmanager.tasks.ExclusiveTask;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

public class FortressOfDeadSiege extends ClanHallSiege
{
	protected static Logger _log = Logger.getLogger(FortressOfDeadSiege.class.getName());

	private static FortressOfDeadSiege _instance;

	public static final FortressOfDeadSiege getInstance()
	{
		if (_instance == null)
		{
			_instance = new FortressOfDeadSiege();
		}
		return _instance;
	}

	public static final FortressOfDeadSiege load()
	{
		_log.info("Clan Hall Siege: Fortress of Dead Initialized.");
		if (_instance == null)
		{
			_instance = new FortressOfDeadSiege();
		}
		return _instance;
	}

	public ClanHall _clanhall = ClanHallManager.getInstance().getClanHallById(64);
	private final List<L2SiegeClan> _registeredClans = new ArrayList<>();
	private final List<L2DoorInstance> _doors = new ArrayList<>();
	private final List<String> _doorDefault = new ArrayList<>();
	private L2SiegeZone _zone = null;
	private L2MonsterInstance _questMob = null;

	protected boolean _isRegistrationOver = false;

	private final SiegeGuardManager _siegeGuardManager;

	private final ExclusiveTask _endSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (!getIsInProgress())
			{
				cancel();
				return;
			}
			final long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				endSiege(null);
				cancel();
				return;
			}
			if (3600000 > timeRemaining)
				if (timeRemaining > 120000)
				{
					announceToPlayer(Math.round(timeRemaining / 60000.0) + " minute (s) before the end of the siege of Clan Hall" + _clanhall.getName() + ".");
				}
				else
				{
					announceToPlayer("Siege Clan Hall " + _clanhall.getName() + " will expire in " + Math.round(timeRemaining / 1000.0) + " seconds (s)!");
				}
			int divider;
			if (timeRemaining > 3600000)
			{
				divider = 3600000;
			}
			else if (timeRemaining > 600000)
			{
				divider = 600000;
			}
			else if (timeRemaining > 60000)
			{
				divider = 60000;
			}
			else if (timeRemaining > 10000)
			{
				divider = 10000;
			}
			else
			{
				divider = 1000;
			}
			schedule(timeRemaining - (timeRemaining - 500) / divider * divider);
		}
	};

	private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (getIsInProgress())
			{
				cancel();
				return;
			}
			if (!getIsRegistrationOver())
			{
				long regTimeRemaining = getSiegeDate().getTimeInMillis() - 2 * 3600000 - System.currentTimeMillis();

				if (regTimeRemaining > 0)
				{
					schedule(regTimeRemaining);
					return;
				}
			}
			long timeRemaining = getSiegeDate().getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				startSiege();
				cancel();
				return;
			}
			if (86400000 > timeRemaining)
			{
				if (!getIsRegistrationOver())
				{
					_isRegistrationOver = true;
					announceToPlayer("The registration period for the siege Clan Hall " + _clanhall.getName() + " is over.");
				}
				if (timeRemaining > 7200000)
				{
					announceToPlayer(Math.round(timeRemaining / 3600000.0) + " hours before the start of the siege, the Clan Hall: " + _clanhall.getName() + ".");
				}
				else if (timeRemaining > 120000)
				{
					announceToPlayer(Math.round(timeRemaining / 60000.0) + " minutes before the start of the siege, the Clan Hall: " + _clanhall.getName() + ".");
				}
				else
				{
					announceToPlayer("Siege Clan Hall: " + _clanhall.getName() + " will start in " + Math.round(timeRemaining / 1000.0) + " seconds!");
				}
			}
			int divider;
			if (timeRemaining > 86400000)
			{
				divider = 86400000;
			}
			else if (timeRemaining > 3600000)
			{
				divider = 3600000;
			}
			else if (timeRemaining > 600000)
			{
				divider = 600000;
			}
			else if (timeRemaining > 60000)
			{
				divider = 60000;
			}
			else if (timeRemaining > 10000)
			{
				divider = 10000;
			}
			else
			{
				divider = 1000;
			}
			schedule(timeRemaining - (timeRemaining - 500) / divider * divider);
		}
	};

	private FortressOfDeadSiege()
	{
		long siegeDate = restoreSiegeDate(64);
		Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate, 64, 22);
		loadSiegeClan();
		loadDoor();
		_siegeGuardManager = new SiegeGuardManager(_clanhall.getName(), _clanhall.getId(), _clanhall.getOwnerId());
		_startSiegeTask.schedule(1000);
		_isRegistrationOver = false;
	}

	private void addAttacker(int clanId)
	{
		getRegisteredClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER));
	}

	public void announceToPlayer(String message)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendMessage(message);
		}
	}

	private boolean checkIfCanRegister(L2PcInstance player)
	{
		L2Clan clan = player.getClan();
		if (clan == null || clan.getLevel() < 4)
		{
			player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_WRONG_CLAN_LEVEL), "4"));
			return false;
		}
		else if (getIsRegistrationOver())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED).addString(_clanhall.getName()));
			return false;
		}
		else if (getIsInProgress())
		{
			player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
			return false;
		}
		else if (clan.getClanId() == _clanhall.getOwnerId())
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CLAN_AUTO_REGISTERED));
			return false;
		}
		else
		{
			if (checkIsRegistered(player.getClan()))
			{
				player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
				return false;
			}
			if (DevastatedCastleSiege.getInstance().checkIsRegistered(player.getClan()))
			{
				player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
				return false;
			}
		}
		if (getRegisteredClans().size() >= Config.SIEGE_MAX_ATTACKER)
		{
			player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
			return false;
		}
		return true;
	}

	public final boolean checkIsRegistered(L2Clan clan)
	{
		if (clan == null)
			return false;

		return SiegeManager.getInstance().checkIsRegistered(clan, _clanhall.getId());
	}

	
	public void clearSiegeClan()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");
			statement.setInt(1, _clanhall.getId());
			statement.execute();
			statement.close();

			getRegisteredClans().clear();
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

	public void endSiege(L2Character par)
	{
		if (getIsInProgress())
		{
			setIsInProgress(false);
			if (par != null)
			{
				if (par instanceof L2PcInstance)
				{
					L2PcInstance killer = (L2PcInstance) par;
					if (killer.getClan() != null && checkIsRegistered(killer.getClan()))
					{
						ClanHallManager.getInstance().setOwner(_clanhall.getId(), killer.getClan());
						announceToPlayer("Siege Clan Hall: " + _clanhall.getName() + " is over.");
						announceToPlayer("Clan Hall became owner " + killer.getClan().getName());
					}
					else
					{
						announceToPlayer("Siege Clan Hall: " + _clanhall.getName() + " is over.");
						announceToPlayer("Clan Hall owner remains the same");
					}
				}
			}
			else
			{
				announceToPlayer("Siege Clan Hall: " + _clanhall.getName() + " is over.");
				announceToPlayer("Clan Hall owner remains the same");
				_questMob.doDie(_questMob);
			}
			spawnDoor();
			_clanhall.setUnderSiege(false);
			_zone.updateSiegeStatus();
			updatePlayerSiegeStateFlags(true);
			clearSiegeClan();
			if (_clanhall.getOwnerClan() != null)
			{
				saveSiegeClan(_clanhall.getOwnerClan());
			}
			_siegeGuardManager.unspawnSiegeGuard();

			setNewSiegeDate(getSiegeDate().getTimeInMillis(), 64, 22);
			_startSiegeTask.schedule(1000);
			_isRegistrationOver = false;
		}
	}

	public final List<L2DoorInstance> getDoors()
	{
		return _doors;
	}

	public final boolean getIsRegistrationOver()
	{
		return _isRegistrationOver;
	}

	public List<L2SiegeClan> getRegisteredClans()
	{
		return _registeredClans;
	}

	public void listRegisterClan(L2PcInstance player)
	{
		player.sendPacket(new SiegeInfo(null, _clanhall, getSiegeDate()));
	}

	
	private void loadDoor()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_door WHERE castleId = ?");
			statement.setInt(1, 64);
			ResultSet rs = statement.executeQuery();

			while (rs.next())
			{
				_doorDefault.add(rs.getString("name") + ";" + rs.getInt("id") + ";" + rs.getInt("x") + ";" + rs.getInt("y") + ";" + rs.getInt("z") + ";" + rs.getInt("range_xmin") + ";" + rs.getInt("range_ymin") + ";" + rs.getInt("range_zmin") + ";" + rs.getInt("range_xmax") + ";" + rs.getInt("range_ymax") + ";" + rs.getInt("range_zmax") + ";" + rs.getInt("hp") + ";" + rs.getInt("pDef") + ";" + rs.getInt("mDef"));

				L2DoorInstance door = DoorTable.parseLine(_doorDefault.get(_doorDefault.size() - 1));
				door.setCHDoor(true);
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				_doors.add(door);
				DoorTable.getInstance().putDoor(door);
				door.closeMe();
			}

			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Exception: loadCastleDoor(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private void loadSiegeClan()
	{
		Connection con = null;
		try
		{
			getRegisteredClans().clear();
			if (_clanhall.getOwnerId() > 0)
			{
				addAttacker(_clanhall.getOwnerId());
			}
			PreparedStatement statement = null;
			ResultSet rs = null;

			con = L2DatabaseFactory.getInstance().getConnection(con);

			statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans where castle_id=?");
			statement.setInt(1, _clanhall.getId());
			rs = statement.executeQuery();

			int typeId;
			int clanId;
			while (rs.next())
			{
				typeId = rs.getInt("type");
				clanId = rs.getInt("clan_id");
				if (typeId == 1)
				{
					addAttacker(clanId);
				}
			}

			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Exception: loadSiegeClan(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void registerClan(L2PcInstance player)
	{
		if (player.getClan() != null && checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan());
		}
	}

	public void registerSiegeZone(L2SiegeZone zone)
	{
		_zone = zone;
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
			statement.setInt(1, _clanhall.getId());
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();
			SiegeManager.getInstance().removeClan(_clanhall.getId(), clanId);
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

	public void removeSiegeClan(L2PcInstance player)
	{
		L2Clan clan = player.getClan();
		if (clan == null || clan == _clanhall.getOwnerClan() || !checkIsRegistered(clan))
			return;
		removeSiegeClan(clan.getClanId());
	}

	
	public void saveSiegeClan(L2Clan clan)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) VALUES (?,?,?,0)");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, _clanhall.getId());
			statement.setInt(3, 1);
			statement.execute();
			statement.close();
			SiegeManager.getInstance().registerClan(_clanhall.getId(), clan);
			addAttacker(clan.getClanId());
			announceToPlayer(clan.getName() + " registered to attack the clan Hall: " + _clanhall.getName());
		}
		catch (Exception e)
		{
			_log.error("Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void spawnDoor()
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			if (door.getStatus().getCurrentHp() <= 0)
			{
				door.decayMe();
				door = DoorTable.parseLine(_doorDefault.get(i));
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if (door.getOpen())
			{
				door.closeMe();
			}
		}
	}

	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (getRegisteredClans().size() <= 0)
			{
				Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED).addString(_clanhall.getName()));
				setNewSiegeDate(getSiegeDate().getTimeInMillis(), 64, 22);
				_startSiegeTask.schedule(1000);
				_isRegistrationOver = false;
				return;
			}
			setIsInProgress(true);
			_zone.updateSiegeStatus();
			_clanhall.setUnderSiege(true);
			announceToPlayer("Siege Clan Hall: " + _clanhall.getName() + " started.");
			_isRegistrationOver = true;
			updatePlayerSiegeStateFlags(false);
			spawnDoor();
			_siegeGuardManager.spawnSiegeGuard();

			L2NpcTemplate template = NpcTable.getInstance().getTemplate(35630);
			_questMob = new L2MonsterInstance(IdFactory.getInstance().getNextId(), template);
			_questMob.getStatus().setCurrentHpMp(_questMob.getMaxHp(), _questMob.getMaxMp());
			_questMob.spawnMe(58390, -27543, 573);
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, 60);
			_endSiegeTask.schedule(1000);
		}
	}

	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for (L2SiegeClan siegeClan : getRegisteredClans())
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
					member.setSiegeState((byte) 1);
				}
				member.sendPacket(new UserInfo(member));
				member.revalidateZone(true);
			}
		}
	}
}